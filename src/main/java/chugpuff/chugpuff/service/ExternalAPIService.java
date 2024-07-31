package chugpuff.chugpuff.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.FileSystemResource;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.core.ResponseInputStream;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.polly.PollyClient;
import software.amazon.awssdk.services.polly.model.OutputFormat;
import software.amazon.awssdk.services.polly.model.SynthesizeSpeechRequest;
import software.amazon.awssdk.services.polly.model.SynthesizeSpeechResponse;

import javax.annotation.PostConstruct;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class ExternalAPIService {

    @Value("${openai.api.key}")
    private String openaiApiKey;

    @Value("${stt.api.key}")
    private String sttApiKey;

    @Value("${aws.access.key}")
    private String awsAccessKey;

    @Value("${aws.secret.key}")
    private String awsSecretKey;

    private PollyClient pollyClient;
    private final RestTemplate restTemplate = new RestTemplate();
    private final ObjectMapper objectMapper = new ObjectMapper();

    @PostConstruct
    public void init() {
        AwsBasicCredentials awsCreds = AwsBasicCredentials.create(awsAccessKey, awsSecretKey);
        this.pollyClient = PollyClient.builder()
                .region(Region.AP_NORTHEAST_2)
                .credentialsProvider(StaticCredentialsProvider.create(awsCreds))
                .build();
    }

    // ChatGPT API 호출 메서드
    public String callChatGPT(String prompt) {
        String apiUrl = "https://api.openai.com/v1/chat/completions";

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(openaiApiKey);

        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("model", "gpt-3.5-turbo");
        requestBody.put("messages", List.of(
                Map.of("role", "system", "content", "You are a helpful assistant."),
                Map.of("role", "user", "content", prompt)
        ));
        requestBody.put("max_tokens", 150);

        HttpEntity<Map<String, Object>> request = new HttpEntity<>(requestBody, headers);

        ResponseEntity<String> response = restTemplate.exchange(apiUrl, HttpMethod.POST, request, String.class);

        if (response.getStatusCode() == HttpStatus.OK) {
            return extractChatGPTResponse(response.getBody());
        } else {
            throw new RuntimeException("ChatGPT API 호출 실패: " + response.getStatusCode());
        }
    }

    // 리턴제로 STT API 호출 메서드
    public String callSTT(String audioFilePath) {
        String apiUrl = "https://api.rev.ai/speechtotext/v1/jobs";

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.MULTIPART_FORM_DATA);
        headers.set("Authorization", "Bearer " + sttApiKey);

        File audioFile = new File(audioFilePath);
        if (!audioFile.exists()) {
            throw new RuntimeException("File not found: " + audioFilePath);
        }

        MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
        body.add("media", new FileSystemResource(audioFile));

        HttpEntity<MultiValueMap<String, Object>> requestEntity = new HttpEntity<>(body, headers);

        ResponseEntity<String> response = restTemplate.exchange(apiUrl, HttpMethod.POST, requestEntity, String.class);

        if (response.getStatusCode() == HttpStatus.OK) {
            return extractSTTResponse(response.getBody());
        } else {
            throw new RuntimeException("STT API 호출 실패: " + response.getStatusCode());
        }
    }

    // TTS API 호출 메서드
    public String callTTS(String text) {
        SynthesizeSpeechRequest synthesizeSpeechRequest = SynthesizeSpeechRequest.builder()
                .text(text)
                .outputFormat(OutputFormat.MP3)
                .voiceId("Joanna")
                .build();

        try (ResponseInputStream<SynthesizeSpeechResponse> synthesizeSpeechResponse = pollyClient.synthesizeSpeech(synthesizeSpeechRequest)) {
            InputStream audioStream = synthesizeSpeechResponse;
            String audioFilePath = "output.mp3";
            File audioFile = new File(audioFilePath);
            try (FileOutputStream outputStream = new FileOutputStream(audioFile)) {
                byte[] buffer = new byte[1024];
                int bytesRead;
                while ((bytesRead = audioStream.read(buffer)) != -1) {
                    outputStream.write(buffer, 0, bytesRead);
                }
            }
            return audioFilePath;
        } catch (Exception e) {
            throw new RuntimeException("Failed to save audio stream to file", e);
        }
    }

    // ChatGPT 응답에서 텍스트 추출
    private String extractChatGPTResponse(String responseBody) {
        try {
            JsonNode root = objectMapper.readTree(responseBody);
            JsonNode choices = root.path("choices");
            if (choices.isArray() && choices.size() > 0) {
                JsonNode message = choices.get(0).path("message").path("content");
                return message.asText().trim();
            }
            throw new RuntimeException("Invalid response structure: " + responseBody);
        } catch (Exception e) {
            throw new RuntimeException("Failed to parse ChatGPT response: " + e.getMessage(), e);
        }
    }

    // STT 응답에서 텍스트 추출
    private String extractSTTResponse(String responseBody) {
        try {
            JsonNode root = objectMapper.readTree(responseBody);
            JsonNode textNode = root.path("text");
            if (textNode.isTextual()) {
                return textNode.asText().trim();
            }
            throw new RuntimeException("Invalid response structure: " + responseBody);
        } catch (Exception e) {
            throw new RuntimeException("Failed to parse STT response: " + e.getMessage(), e);
        }
    }
}
