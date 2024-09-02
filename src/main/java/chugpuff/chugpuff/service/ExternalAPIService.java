package chugpuff.chugpuff.service;

import com.fasterxml.jackson.core.JsonProcessingException;
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
import org.springframework.http.*;
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

    @Value("${rtzr.client.id}")
    private String rtzrClientId;

    @Value("${rtzr.client.secret}")
    private String rtzrClientSecret;

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

    // 리턴제로 API에서 액세스 토큰을 가져오는 메서드
    public String getRtzrAccessToken() {
        String tokenUrl = "https://openapi.vito.ai/v1/authenticate";
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

        MultiValueMap<String, String> body = new LinkedMultiValueMap<>();
        body.add("client_id", rtzrClientId);
        body.add("client_secret", rtzrClientSecret);

        HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<>(body, headers);

        try {
            ResponseEntity<String> response = restTemplate.postForEntity(tokenUrl, request, String.class);
            if (response.getStatusCode().is2xxSuccessful()) {
                JsonNode root = objectMapper.readTree(response.getBody());
                return root.path("access_token").asText();
            } else {
                throw new RuntimeException("Failed to get access token: " + response.getStatusCode());
            }
        } catch (Exception e) {
            throw new RuntimeException("Error while fetching access token", e);
        }
    }

    // 리턴제로 STT API 호출 메서드
    public String callSTT(String audioFilePath) {
        String apiUrl = "https://openapi.vito.ai/v1/transcribe";
        String accessToken = getRtzrAccessToken();

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.MULTIPART_FORM_DATA);
        headers.set("Authorization", "Bearer " + accessToken);

        File audioFile = new File(audioFilePath);
        if (!audioFile.exists()) {
            throw new RuntimeException("File not found: " + audioFilePath);
        }

        Map<String, Object> config = new HashMap<>();
        config.put("use_diarization", true);
        Map<String, Object> diarization = new HashMap<>();
        diarization.put("spk_count", 2); // 화자 수 설정
        config.put("diarization", diarization);
        config.put("use_itn", true); // 영어/숫자/단위 변환 여부

        MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
        body.add("file", new FileSystemResource(audioFile));

        try {
            String configJson = new ObjectMapper().writeValueAsString(config);
            body.add("config", configJson);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Failed to convert config to JSON", e);
        }

        HttpEntity<MultiValueMap<String, Object>> requestEntity = new HttpEntity<>(body, headers);

        System.out.println("Calling STT API at: " + apiUrl);
        System.out.println("Authorization Header: " + headers.get("Authorization"));

        try {
            ResponseEntity<String> response = restTemplate.postForEntity(apiUrl, requestEntity, String.class);

            if (response.getStatusCode().is2xxSuccessful()) {
                JsonNode root = objectMapper.readTree(response.getBody());
                String taskId = root.path("id").asText();
                if (taskId != null && !taskId.isEmpty()) {
                    return pollForSTTResult(taskId);
                } else {
                    throw new RuntimeException("Failed to obtain task ID from STT API response");
                }
            } else {
                System.err.println("Error response from STT API: " + response.getStatusCode());
                throw new RuntimeException("STT API 호출 실패: " + response.getStatusCode());
            }
        } catch (HttpClientErrorException e) {
            System.err.println("HTTP Client Error: " + e.getResponseBodyAsString());
            throw e;
        } catch (Exception e) {
            throw new RuntimeException("Error while calling STT API", e);
        }
    }

    // STT 응답에서 작업 ID 추출
    private String extractTaskIdFromResponse(String responseBody) {
        try {
            JsonNode root = objectMapper.readTree(responseBody);
            JsonNode idNode = root.path("id");
            if (idNode.isTextual()) {
                return idNode.asText().trim();
            }
            throw new RuntimeException("Invalid response structure: " + responseBody);
        } catch (Exception e) {
            throw new RuntimeException("Failed to parse STT response: " + e.getMessage(), e);
        }
    }

    // STT 작업 결과를 폴링하여 조회
    private String pollForSTTResult(String taskId) {
        String apiUrl = "https://openapi.vito.ai/v1/transcribe/" + taskId;
        String accessToken = getRtzrAccessToken();
        int maxRetries = 180;  // 최대 180회 시도 (약 3분)
        int retries = 0;
        int pollingInterval = 5000; // 5초 간격으로 폴링

        try {
            while (retries < maxRetries) {
                try {
                    HttpHeaders headers = new HttpHeaders();
                    headers.set("Authorization", "Bearer " + accessToken);

                    HttpEntity<String> requestEntity = new HttpEntity<>(headers);
                    ResponseEntity<String> response = restTemplate.exchange(apiUrl, HttpMethod.GET, requestEntity, String.class);

                    if (response.getStatusCode().is2xxSuccessful()) {
                        String result = extractSTTResponse(response.getBody());
                        if (result != null) {
                            System.out.println("STT Result: " + result);
                            return result;
                        } else {
                            System.out.println("STT processing not complete. Status: " + response.getBody());
                        }
                    }

                    // 아직 결과가 준비되지 않은 경우 일정 시간 후 재시도
                    Thread.sleep(pollingInterval);
                    retries++;

                } catch (HttpClientErrorException e) {
                    if (e.getStatusCode() == HttpStatus.UNAUTHORIZED) {
                        System.out.println("Access token expired, obtaining a new one.");
                        accessToken = getRtzrAccessToken();  // 토큰 갱신
                    } else {
                        System.err.println("HTTP Client Error during polling: " + e.getResponseBodyAsString());
                        throw e;
                    }
                } catch (InterruptedException e) {
                    System.err.println("Polling interrupted, exiting: " + e.getMessage());
                    Thread.currentThread().interrupt();
                    break;
                }
            }

            throw new RuntimeException("STT result polling timed out after max retries");
        } catch (Exception e) {
            throw new RuntimeException("Error while polling for STT result", e);
        }
    }

    // STT 응답에서 텍스트 추출
    private String extractSTTResponse(String responseBody) {
        try {
            JsonNode root = objectMapper.readTree(responseBody);
            String status = root.path("status").asText();
            if ("completed".equals(status)) {
                StringBuilder resultText = new StringBuilder();
                JsonNode utterances = root.path("results").path("utterances");
                for (JsonNode utterance : utterances) {
                    resultText.append(utterance.path("msg").asText()).append(" ");
                }
                return resultText.toString().trim();
            } else if ("failed".equals(status)) {
                throw new RuntimeException("STT processing failed");
            }
            return null;  // 작업이 아직 완료되지 않은 경우 null 반환
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
