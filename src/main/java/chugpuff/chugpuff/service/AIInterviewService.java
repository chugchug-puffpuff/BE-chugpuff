package chugpuff.chugpuff.service;

import chugpuff.chugpuff.domain.AIInterview;
import chugpuff.chugpuff.domain.AIInterviewFF;
import chugpuff.chugpuff.domain.AIInterviewIF;
import chugpuff.chugpuff.repository.AIInterviewRepository;
import chugpuff.chugpuff.repository.AIInterviewFFRepository;
import chugpuff.chugpuff.repository.AIInterviewIFRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class AIInterviewService {

    @Autowired
    private AIInterviewRepository aiInterviewRepository;

    @Autowired
    private AIInterviewIFRepository aiInterviewIFRepository;

    @Autowired
    private AIInterviewFFRepository aiInterviewFFRepository;

    @Autowired
    private ExternalAPIService externalAPIService;

    @Autowired
    private TimerService timerService;

    // AI 면접 저장
    public AIInterview saveInterview(AIInterview aiInterview) {
        return aiInterviewRepository.save(aiInterview);
    }

    // 인터뷰 시작 메서드
    public void startInterview(Long id) {
        AIInterview aiInterview = aiInterviewRepository.findById(id).orElseThrow(() -> new RuntimeException("Interview not found"));
        // 인터뷰 세션 초기화 및 시작 로직
        initializeInterviewSession(aiInterview);

        // 인터뷰 진행 로직 처리
        timerService.startTimer(30 * 60 * 1000, () -> endInterview(aiInterview));
        while (interviewIsInProgress(aiInterview)) {
            handleInterviewProcess(aiInterview);
        }

        // 인터뷰 종료 및 최종 피드백 처리
        if ("overall".equals(aiInterview.getFeedbackType())) {
            handleOverallFeedback(aiInterview);
        }
    }

    // 인터뷰 세션 초기화 및 시작 로직
    private void initializeInterviewSession(AIInterview aiInterview) {
        String chatPrompt = "Starting an interview of type: " + aiInterview.getInterviewType();
        externalAPIService.callChatGPT(chatPrompt);
        // 초기화 로직 추가 (예: 인터뷰 시작 로그 기록)
        System.out.println("Interview session initialized for: " + aiInterview.getInterviewType());
    }

    // 인터뷰 진행 처리 메서드
    private void handleInterviewProcess(AIInterview aiInterview) {
        // ChatGPT로부터 질문 생성
        String question = getChatGPTQuestion(aiInterview);
        // TTS API 호출하여 질문을 음성으로 변환
        String ttsQuestion = externalAPIService.callTTS(question);
        // 음성 재생
        playAudio(ttsQuestion);

        // 사용자 음성 응답 캡처
        String userAudioResponse = captureUserAudio();
        // STT API 호출하여 음성을 텍스트로 변환
        String sttResponse = externalAPIService.callSTT(userAudioResponse);

        // 즉시 피드백인 경우 피드백 생성 및 저장
        if ("immediate".equals(aiInterview.getFeedbackType())) {
            String immediateFeedback = getChatGPTFeedback(sttResponse);
            String ttsFeedback = externalAPIService.callTTS(immediateFeedback);
            playAudio(ttsFeedback);

            saveImmediateFeedback(aiInterview, question, sttResponse, immediateFeedback);
        }

        // 사용자 응답 저장
        saveUserResponse(aiInterview, question, sttResponse);
    }

    // ChatGPT로부터 질문 생성
    private String getChatGPTQuestion(AIInterview aiInterview) {
        String previousResponses = getPreviousResponses(aiInterview);
        String chatPrompt = "Generate a question for a " + aiInterview.getInterviewType() + " interview. Previous responses: " + previousResponses;
        return externalAPIService.callChatGPT(chatPrompt);
    }

    // 이전 응답 가져오기
    private String getPreviousResponses(AIInterview aiInterview) {
        List<AIInterviewIF> responses = aiInterviewIFRepository.findByAiInterview(aiInterview);
        StringBuilder responseText = new StringBuilder();
        for (AIInterviewIF response : responses) {
            responseText.append(response.getI_answer()).append(" ");
        }
        return responseText.toString();
    }

    // ChatGPT로부터 피드백 생성
    private String getChatGPTFeedback(String userResponse) {
        String chatPrompt = "Provide feedback for the following response: " + userResponse;
        return externalAPIService.callChatGPT(chatPrompt);
    }

    // 즉시 피드백 저장
    public void saveImmediateFeedback(AIInterview aiInterview, String question, String response, String feedback) {
        AIInterviewIF aiInterviewIF = new AIInterviewIF();
        aiInterviewIF.setAiInterview(aiInterview);
        aiInterviewIF.setI_question(question);
        aiInterviewIF.setI_answer(response);
        aiInterviewIF.setI_feedback(feedback);
        aiInterviewIFRepository.save(aiInterviewIF);
    }

    // 사용자 응답 저장
    public void saveUserResponse(AIInterview aiInterview, String question, String response) {
        if (!"immediate".equals(aiInterview.getFeedbackType())) {
            AIInterviewIF aiInterviewIF = new AIInterviewIF();
            aiInterviewIF.setAiInterview(aiInterview);
            aiInterviewIF.setI_question(question);
            aiInterviewIF.setI_answer(response);
            aiInterviewIFRepository.save(aiInterviewIF);
        }
    }

    // 전체 피드백 처리
    private void handleOverallFeedback(AIInterview aiInterview) {
        List<AIInterviewIF> responses = aiInterviewIFRepository.findByAiInterview(aiInterview);
        StringBuilder feedbackText = new StringBuilder();
        for (AIInterviewIF response : responses) {
            feedbackText.append(response.getI_question()).append(" ").append(response.getI_answer()).append(" ");
        }
        String overallFeedback = getChatGPTFeedback(feedbackText.toString());
        String ttsFeedback = externalAPIService.callTTS(overallFeedback);
        playAudio(ttsFeedback);

        // 예시로 첫 번째 질문과 답변을 전달합니다.
        if (!responses.isEmpty()) {
            String firstQuestion = responses.get(0).getI_question();
            String firstAnswer = responses.get(0).getI_answer();
            saveOverallFeedback(aiInterview, firstQuestion, firstAnswer, overallFeedback);
        } else {
            saveOverallFeedback(aiInterview, "", "", overallFeedback);
        }
    }

    // 전체 피드백 저장
    public void saveOverallFeedback(AIInterview aiInterview, String question, String answer, String feedback) {
        AIInterviewFF aiInterviewFF = new AIInterviewFF();
        aiInterviewFF.setAiInterview(aiInterview);
        aiInterviewFF.setF_question(question);
        aiInterviewFF.setF_answer(answer);
        aiInterviewFF.setF_feedback(feedback);
        aiInterviewFFRepository.save(aiInterviewFF);
    }

    // 인터뷰 진행 여부 확인
    private boolean interviewIsInProgress(AIInterview aiInterview) {
        // 인터뷰가 진행 중인지 확인하는 로직 (예: 타이머, 인터뷰 상태 등)
        return true; // 실제 조건으로 변경 필요
    }

    // 음성 재생
    private void playAudio(String audioUrl) {
        // 적절한 라이브러리나 프레임워크를 사용하여 음성을 재생하는 로직 추가
        System.out.println("Playing audio from URL: " + audioUrl);
    }

    // 사용자 음성 응답 캡처
    private String captureUserAudio() {
        // 사용자의 음성 응답을 캡처하는 로직 추가 (예: 마이크 입력, 파일 저장 등)
        return "Captured Audio URL";
    }

    // AI 면접 ID로 면접 조회
    public AIInterview getInterviewById(Long id) {
        return aiInterviewRepository.findById(id).orElse(null);
    }

    // 즉시 피드백 목록 조회
    public List<AIInterviewIF> getImmediateFeedbacks(AIInterview aiInterview) {
        return aiInterviewIFRepository.findByAiInterview(aiInterview);
    }

    // 인터뷰 종료 처리
    private void endInterview(AIInterview aiInterview) {
        System.out.println("Interview session ended.");
        // 인터뷰 종료 후 추가 처리 로직 (예: 상태 업데이트, 로그 기록 등)
    }
}
