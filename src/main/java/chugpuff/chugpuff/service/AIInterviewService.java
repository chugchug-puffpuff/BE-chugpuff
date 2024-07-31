package chugpuff.chugpuff.service;

import chugpuff.chugpuff.domain.AIInterview;
import chugpuff.chugpuff.domain.AIInterviewFF;
import chugpuff.chugpuff.domain.AIInterviewIF;
import chugpuff.chugpuff.repository.AIInterviewRepository;
import chugpuff.chugpuff.repository.AIInterviewFFRepository;
import chugpuff.chugpuff.repository.AIInterviewIFRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import javax.sound.sampled.*;
import java.io.File;
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

    private boolean interviewInProgress = false;

    // AI 면접 저장
    public AIInterview saveInterview(AIInterview aiInterview) {
        return aiInterviewRepository.save(aiInterview);
    }

    // 인터뷰 시작 메서드
    @Async
    public void startInterview(Long id) {
        AIInterview aiInterview = aiInterviewRepository.findById(id).orElseThrow(() -> new RuntimeException("Interview not found"));
        initializeInterviewSession(aiInterview);
        interviewInProgress = true;

        timerService.startTimer(30 * 60 * 1000, () -> endInterview(aiInterview));

        while (interviewIsInProgress()) {
            handleInterviewProcess(aiInterview);
        }

        if ("overall".equals(aiInterview.getFeedbackType())) {
            handleOverallFeedback(aiInterview);
        }

        endInterview(aiInterview);
    }

    // 인터뷰 세션 초기화 및 시작 로직
    private void initializeInterviewSession(AIInterview aiInterview) {
        String chatPrompt = "Starting an interview of type: " + aiInterview.getInterviewType();
        externalAPIService.callChatGPT(chatPrompt);
        System.out.println("Interview session initialized for: " + aiInterview.getInterviewType());
    }

    // 인터뷰 진행 처리 메서드
    private void handleInterviewProcess(AIInterview aiInterview) {
        try {
            String question = getChatGPTQuestion(aiInterview);
            String ttsQuestion = externalAPIService.callTTS(question);
            playAudio(ttsQuestion);

            String userAudioResponse = captureUserAudio();
            String sttResponse = externalAPIService.callSTT(userAudioResponse);

            if ("immediate".equals(aiInterview.getFeedbackType())) {
                String immediateFeedback = getChatGPTFeedback(sttResponse);
                String ttsFeedback = externalAPIService.callTTS(immediateFeedback);
                playAudio(ttsFeedback);

                saveImmediateFeedback(aiInterview, question, sttResponse, immediateFeedback);
            }

            saveUserResponse(aiInterview, question, sttResponse);
        } catch (Exception e) {
            e.printStackTrace();
            interviewInProgress = false;
        }
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
    private boolean interviewIsInProgress() {
        return interviewInProgress;
    }

    // 음성 재생
    private void playAudio(String audioUrl) {
        System.out.println("Playing audio from URL: " + audioUrl);
    }

    // 사용자 음성 응답 캡처
    private String captureUserAudio() {
        String audioFilePath = "captured_audio.wav";
        // 마이크 입력을 캡처하여 오디오 파일로 저장하는 로직 추가
        // 예시: AudioSystem을 사용하여 마이크 입력 캡처
        try (TargetDataLine microphone = AudioSystem.getTargetDataLine(new AudioFormat(16000, 16, 1, true, true))) {
            microphone.open();
            microphone.start();
            AudioInputStream audioStream = new AudioInputStream(microphone);
            File audioFile = new File(audioFilePath);
            AudioSystem.write(audioStream, AudioFileFormat.Type.WAVE, audioFile);
            microphone.stop();
            microphone.close();
        } catch (Exception e) {
            throw new RuntimeException("Failed to capture audio", e);
        }
        return audioFilePath;
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
        interviewInProgress = false;
        System.out.println("Interview session ended.");
        // 인터뷰 종료 후 추가 처리 로직 (예: 상태 업데이트, 로그 기록 등)
    }
}
