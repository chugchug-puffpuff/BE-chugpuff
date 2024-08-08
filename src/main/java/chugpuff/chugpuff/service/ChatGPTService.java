package chugpuff.chugpuff.service;

import chugpuff.chugpuff.entity.EditSelfIntroductionDetails;
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
import org.apache.hc.client5.http.ssl.SSLConnectionSocketFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import javax.annotation.PostConstruct;
import javax.net.ssl.SSLContext;
import java.security.KeyManagementException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.util.HashMap;
import java.util.Map;
import java.util.List;

@Service
public class ChatGPTService {
    private static final Logger log = LoggerFactory.getLogger(ChatGPTService.class);

    @Value("${openai.api.key}")
    private String openaiApiKey;

    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper = new ObjectMapper();

    public ChatGPTService() throws NoSuchAlgorithmException, KeyStoreException, KeyManagementException {
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

    public String callChatGPTForFeedback(List<EditSelfIntroductionDetails> details) {
        String apiUrl = "https://cesrv.hknu.ac.kr/srv/gpt";

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        StringBuilder promptBuilder = new StringBuilder();
        promptBuilder.append("다음은 사용자의 자기소개서 질문과 답변입니다.\n")
                .append("자기소개서 첨삭을 위해 각 답변에 대해 피드백을 주고, 필요한 경우 대체되면 좋은 단어나 맞춤법, 띄어쓰기 검사도 해주세요. 마지막으로 피드백 내용을 참고해서 수정된 자기소개서도 보내주세요.\n\n");

        for (int i = 0; i < details.size(); i++) {
            String question = details.get(i).getES_question();
            String answer = details.get(i).getES_answer();

            // Null 또는 빈 문자열 체크
            if (question == null || question.trim().isEmpty()) {
                log.warn("Question {} is null or empty!", i + 1);
                question = "질문 없음";
            }

            if (answer == null || answer.trim().isEmpty()) {
                log.warn("Answer {} is null or empty!", i + 1);
                answer = "답변 없음";
            }

            promptBuilder.append("질문 ").append(i + 1).append(": ").append(question).append("\n");
            promptBuilder.append("답변 ").append(i + 1).append(": ").append(answer).append("\n");
        }

        log.info("GPT에 보낼 프롬프트: \n{}", promptBuilder.toString());

        // 요청 바디 구성
        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("service", "gpt");
        requestBody.put("question", promptBuilder.toString());
        requestBody.put("hash", openaiApiKey);

        HttpEntity<Map<String, Object>> request = new HttpEntity<>(requestBody, headers);

        try {
            ResponseEntity<String> response = restTemplate.postForEntity(apiUrl, request, String.class);

            if (response.getStatusCode().is2xxSuccessful()) {
                String responseBody = response.getBody();
                log.info("GPT 응답: \n{}", responseBody);
                return responseBody;
            } else {
                throw new RuntimeException("ChatGPT API 호출 실패: " + response.getStatusCode());
            }
        } catch (HttpClientErrorException e) {
            log.error("GPT 호출 중 오류 발생: {}", e.getResponseBodyAsString());
            log.error("상태 코드: {}", e.getStatusCode());
            throw e;
        }
    }

    public String extractChatGPTFeedback(String responseBody) {
        try {
            JsonNode root = objectMapper.readTree(responseBody);

            // 'feedback' 또는 'revisedSelfIntroduction' 필드를 찾아 피드백과 수정된 자기소개서로 분리
            JsonNode feedbackNode = root.path("feedback");
            JsonNode revisedSelfIntroductionNode = root.path("revisedSelfIntroduction");

            if (feedbackNode.isMissingNode() || revisedSelfIntroductionNode.isMissingNode()) {
                JsonNode answerNode = root.path("answer");
                if (answerNode.isTextual()) {
                    String feedback = answerNode.asText().trim();
                    String revisedSelfIntroduction = generateRevisedSelfIntroduction(feedback); // 수정된 자기소개서를 생성하는 로직

                    return feedback + "\n\n" + revisedSelfIntroduction;
                }
            } else if (feedbackNode.isTextual() && revisedSelfIntroductionNode.isTextual()) {
                return feedbackNode.asText().trim() + "\n\n" + revisedSelfIntroductionNode.asText().trim();
            }

            throw new RuntimeException("Invalid response structure: " + responseBody);
        } catch (Exception e) {
            throw new RuntimeException("Failed to parse ChatGPT response: " + e.getMessage(), e);
        }
    }

    private String generateRevisedSelfIntroduction(String feedback) {
        // GPT가 제공한 피드백을 바탕으로 수정된 자기소개서를 생성하는 로직
        // 이 예시에서는 피드백을 그대로 수정된 자기소개서로 사용
        return feedback;
    }
}
