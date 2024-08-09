package chugpuff.chugpuff.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.classic.HttpClients;
import org.apache.hc.client5.http.impl.io.PoolingHttpClientConnectionManager;
import org.apache.hc.client5.http.socket.ConnectionSocketFactory;
import org.apache.hc.client5.http.socket.PlainConnectionSocketFactory;
import org.apache.hc.core5.http.config.Registry;
import org.apache.hc.core5.http.config.RegistryBuilder;
import org.apache.hc.core5.ssl.SSLContextBuilder;
import org.apache.hc.core5.ssl.TrustStrategy;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.FileSystemResource;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.core.ResponseInputStream;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.polly.PollyClient;
import software.amazon.awssdk.services.polly.model.OutputFormat;
import software.amazon.awssdk.services.polly.model.SynthesizeSpeechRequest;
import software.amazon.awssdk.services.polly.model.SynthesizeSpeechResponse;
import org.apache.hc.client5.http.ssl.SSLConnectionSocketFactory;

import javax.annotation.PostConstruct;
import javax.net.ssl.SSLContext;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.security.KeyManagementException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.util.HashMap;
import java.util.Map;

import software.amazon.awssdk.core.ResponseInputStream;
import software.amazon.awssdk.services.polly.model.SynthesizeSpeechRequest;
import software.amazon.awssdk.services.polly.model.SynthesizeSpeechResponse;

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
    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper = new ObjectMapper();

    public ExternalAPIService() throws NoSuchAlgorithmException, KeyStoreException, KeyManagementException {
        this.restTemplate = createRestTemplate();
    }

    private RestTemplate createRestTemplate() throws NoSuchAlgorithmException, KeyStoreException, KeyManagementException {
        TrustStrategy acceptingTrustStrategy = (cert, authType) -> true;

        SSLContext sslContext = SSLContextBuilder.create()
                .loadTrustMaterial(null, acceptingTrustStrategy)
                .build();

        SSLConnectionSocketFactory sslSocketFactory = new SSLConnectionSocketFactory(sslContext);

        Registry<ConnectionSocketFactory> socketFactoryRegistry =
                RegistryBuilder.<ConnectionSocketFactory>create()
                        .register("http", PlainConnectionSocketFactory.getSocketFactory())
                        .register("https", sslSocketFactory)
                        .build();

        PoolingHttpClientConnectionManager connectionManager = new PoolingHttpClientConnectionManager(socketFactoryRegistry);

        CloseableHttpClient httpClient = HttpClients.custom()
                .setConnectionManager(connectionManager)
                .build();

        HttpComponentsClientHttpRequestFactory factory = new HttpComponentsClientHttpRequestFactory(httpClient);
        return new RestTemplate(factory);
    }

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
        String apiUrl = "https://cesrv.hknu.ac.kr/srv/gpt";

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("service", "gpt");
        requestBody.put("question", prompt);
        requestBody.put("hash", openaiApiKey);

        HttpEntity<Map<String, Object>> request = new HttpEntity<>(requestBody, headers);

        try {
            ResponseEntity<String> response = restTemplate.postForEntity(apiUrl, request, String.class);

            if (response.getStatusCode().is2xxSuccessful()) {
                return extractChatGPTResponse(response.getBody());
            } else {
                throw new RuntimeException("ChatGPT API 호출 실패: " + response.getStatusCode());
            }
        } catch (HttpClientErrorException e) {
            // 응답 내용 및 상태 코드 로깅
            System.err.println("Error response: " + e.getResponseBodyAsString());
            System.err.println("Status code: " + e.getStatusCode());
            throw e;
        }
    }

    // ChatGPT 응답에서 텍스트 추출
    private String extractChatGPTResponse(String responseBody) {
        try {
            JsonNode root = objectMapper.readTree(responseBody);
            JsonNode answerNode = root.path("answer");
            if (answerNode.isTextual()) {
                return answerNode.asText().trim();
            }
            throw new RuntimeException("Invalid response structure: " + responseBody);
        } catch (Exception e) {
            throw new RuntimeException("Failed to parse ChatGPT response: " + e.getMessage(), e);
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
        ResponseEntity<String> response = restTemplate.postForEntity(apiUrl, requestEntity, String.class);

        if (response.getStatusCode().is2xxSuccessful()) {
            return extractSTTResponse(response.getBody());
        } else {
            throw new RuntimeException("STT API 호출 실패: " + response.getStatusCode());
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

    // TTS API 호출 메서드
    public String callTTS(String text) {
        SynthesizeSpeechRequest synthesizeSpeechRequest = SynthesizeSpeechRequest.builder()
                .text(text)
                .outputFormat(OutputFormat.MP3) // MP3 형식으로 설정
                .voiceId("Seoyeon") // "Seoyeon"을 사용하여 한국어 음성을 생성
                .build();
        try (ResponseInputStream<SynthesizeSpeechResponse> synthesizeSpeechResponse = pollyClient.synthesizeSpeech(synthesizeSpeechRequest)) {
            InputStream audioStream = synthesizeSpeechResponse;
            String audioFilePath = "output.mp3"; // 확장자를 .mp3로 설정
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

}
