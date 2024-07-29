package chugpuff.chugpuff.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

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

    private final RestTemplate restTemplate = new RestTemplate();
    private final ObjectMapper objectMapper = new ObjectMapper();

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

    // STT API 호출 메서드
    public String callSTT(String audioUrl) {
        String apiUrl = "https://api.example.com/stt";  // 실제 STT API URL로 변경

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("Authorization", "Bearer " + sttApiKey);

        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("audio_url", audioUrl);

        HttpEntity<Map<String, Object>> request = new HttpEntity<>(requestBody, headers);

        ResponseEntity<String> response = restTemplate.exchange(apiUrl, HttpMethod.POST, request, String.class);

        if (response.getStatusCode() == HttpStatus.OK) {
            return extractSTTResponse(response.getBody());
        } else {
            throw new RuntimeException("STT API 호출 실패: " + response.getStatusCode());
        }
    }

    // TTS API 호출 메서드
    public String callTTS(String text) {
        String apiUrl = "https://polly.amazonaws.com/v1/speech";

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("x-amz-date", "YOUR_DATE");
        headers.set("Authorization", "AWS4-HMAC-SHA256 Credential=" + awsAccessKey + "/YOUR_CREDENTIAL_SCOPE, SignedHeaders=YOUR_SIGNED_HEADERS, Signature=YOUR_SIGNATURE");

        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("Text", text);
        requestBody.put("OutputFormat", "mp3");
        requestBody.put("VoiceId", "Joanna");

        HttpEntity<Map<String, Object>> request = new HttpEntity<>(requestBody, headers);

        ResponseEntity<String> response = restTemplate.exchange(apiUrl, HttpMethod.POST, request, String.class);

        if (response.getStatusCode() == HttpStatus.OK) {
            return extractTTSResponse(response.getBody());
        } else {
            throw new RuntimeException("TTS API 호출 실패: " + response.getStatusCode());
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

    // TTS 응답에서 오디오 URL 추출
    private String extractTTSResponse(String responseBody) {
        try {
            JsonNode root = objectMapper.readTree(responseBody);
            JsonNode audioUrlNode = root.path("audio_url");
            if (audioUrlNode.isTextual()) {
                return audioUrlNode.asText().trim();
            }
            throw new RuntimeException("Invalid response structure: " + responseBody);
        } catch (Exception e) {
            throw new RuntimeException("Failed to parse TTS response: " + e.getMessage(), e);
        }
    }
}
