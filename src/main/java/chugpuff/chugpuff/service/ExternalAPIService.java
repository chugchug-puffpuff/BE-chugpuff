package chugpuff.chugpuff.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
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

    public String callChatGPT(String prompt) {
        String apiUrl = "https://api.openai.com/v1/engines/davinci-codex/completions";

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(openaiApiKey);

        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("prompt", prompt);
        requestBody.put("max_tokens", 150);

        HttpEntity<Map<String, Object>> request = new HttpEntity<>(requestBody, headers);

        ResponseEntity<String> response = restTemplate.exchange(apiUrl, HttpMethod.POST, request, String.class);

        if (response.getStatusCode() == HttpStatus.OK) {
            return response.getBody();
        } else {
            throw new RuntimeException("ChatGPT API 호출 실패: " + response.getStatusCode());
        }
    }

    public String callSTT(String audioUrl) {
        String apiUrl = "https://api.returnzero.com/stt";

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("Authorization", "Bearer " + sttApiKey);

        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("audio_url", audioUrl);

        HttpEntity<Map<String, Object>> request = new HttpEntity<>(requestBody, headers);

        ResponseEntity<String> response = restTemplate.exchange(apiUrl, HttpMethod.POST, request, String.class);

        if (response.getStatusCode() == HttpStatus.OK) {
            return response.getBody();
        } else {
            throw new RuntimeException("STT API 호출 실패: " + response.getStatusCode());
        }
    }

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
            return response.getBody();
        } else {
            throw new RuntimeException("TTS API 호출 실패: " + response.getStatusCode());
        }
    }
}
