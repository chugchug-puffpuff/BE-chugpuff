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
    public void startInterview(Long AIInterviewNo) {
        AIInterview aiInterview = aiInterviewRepository.findById(AIInterviewNo).orElseThrow(() -> new RuntimeException("Interview not found"));

        // 이전 응답 데이터 초기화
        clearPreviousResponses(aiInterview);

        initializeInterviewSession(aiInterview);
        interviewInProgress = true;

        timerService.startTimer(30 * 60 * 1000, () -> endInterview(aiInterview));

        while (interviewInProgress) {
            handleInterviewProcess(aiInterview);
        }

        if ("전체 피드백".equals(aiInterview.getFeedbackType())) {
            handleFullFeedback(aiInterview);
        }

        endInterview(aiInterview);
    }

    // 이전 응답 데이터 초기화 메서드
    private void clearPreviousResponses(AIInterview aiInterview) {
        List<AIInterviewIF> previousResponses = aiInterviewIFRepository.findByAiInterview(aiInterview);
        for (AIInterviewIF response : previousResponses) {
            aiInterviewIFRepository.delete(response);
        }
        aiInterview.setImmediateFeedbacks(null);
        aiInterview.setOverallFeedback(null);
        aiInterviewRepository.save(aiInterview); // 변경 사항 저장
    }

    // 인터뷰 세션 초기화 및 시작 로직
    private void initializeInterviewSession(AIInterview aiInterview) {
        String chatPrompt;
        if ("인성 면접".equals(aiInterview.getInterviewType())) {
            chatPrompt = "인성 면접을 시작합니다. 한글로 해주세요.";
        } else if ("직무 면접".equals(aiInterview.getInterviewType())) {
            String job = aiInterview.getMember().getJob();
            String jobKeyword = aiInterview.getMember().getJobKeyword();
            chatPrompt = job + " 직무에 대한 면접을 " + jobKeyword + "에 중점을 두고 직무 면접을 시작합니다. 한글로 해주세요.";
        } else {
            throw new RuntimeException("Invalid interview type");
        }

        // 피드백 방식을 ChatGPT 프롬프트에 포함
        if ("즉시 피드백".equals(aiInterview.getFeedbackType())) {
            chatPrompt += " 질문에 대답한 후 즉시 피드백을 제공해주세요.";
        } else if ("전체 피드백".equals(aiInterview.getFeedbackType())) {
            chatPrompt += " 면접이 끝난 후 전체적인 피드백을 제공해주세요.";
        }

        System.out.println("Sending to ChatGPT: " + chatPrompt); // ChatGPT 프롬프트 로그 출력
        externalAPIService.callChatGPT(chatPrompt);
        System.out.println("Interview session initialized for: " + aiInterview.getInterviewType() + " with " + aiInterview.getFeedbackType() + " feedback.");
    }

    // 인터뷰 진행 처리 메서드
    private void handleInterviewProcess(AIInterview aiInterview) {
        try {
            String question = getChatGPTQuestion(aiInterview);
            System.out.println("Generated Question: " + question); // 질문 로그 출력
            String ttsQuestion = externalAPIService.callTTS(question);
            playAudio(ttsQuestion);

            String userAudioResponse = captureUserAudio();
            String sttResponse = externalAPIService.callSTT(userAudioResponse);

            saveUserResponse(aiInterview, question, sttResponse);

            if ("즉시 피드백".equals(aiInterview.getFeedbackType())) {
                String immediateFeedback = getChatGPTFeedback(sttResponse);
                System.out.println("Generated Feedback: " + immediateFeedback); // 피드백 로그 출력
                String ttsFeedback = externalAPIService.callTTS(immediateFeedback);
                playAudio(ttsFeedback);

                saveImmediateFeedback(aiInterview, question, sttResponse, immediateFeedback);
            }

        } catch (Exception e) {
            e.printStackTrace();
            interviewInProgress = false;
        }
    }

    // ChatGPT로부터 질문 생성
    private String getChatGPTQuestion(AIInterview aiInterview) {
        String chatPrompt;

        // 인터뷰 유형에 따라 프롬프트 수정
        if ("인성 면접".equals(aiInterview.getInterviewType())) {
            chatPrompt = "인성 면접을 위해 다음 질문을 생성해주세요. 질문은 하나씩 해주세요. ";
        } else if ("직무 면접".equals(aiInterview.getInterviewType())) {
            String job = aiInterview.getMember().getJob();
            String jobKeyword = aiInterview.getMember().getJobKeyword();
            chatPrompt = job + " 직무 면접을 위해 다음 질문을 생성해주세요. " + jobKeyword + "에 중점을 두고 있습니다. 질문은 하나씩 해주세요. ";
        } else {
            throw new RuntimeException("Invalid interview type");
        }

        System.out.println("Sending to ChatGPT: " + chatPrompt); // ChatGPT 프롬프트 로그 출력
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
        String chatPrompt = "다음 응답에 대한 피드백을 제공해주세요: " + userResponse;
        System.out.println("Sending to ChatGPT: " + chatPrompt); // ChatGPT 프롬프트 로그 출력
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
        AIInterviewIF aiInterviewIF = new AIInterviewIF();
        aiInterviewIF.setAiInterview(aiInterview);
        aiInterviewIF.setI_question(question);
        aiInterviewIF.setI_answer(response);
        aiInterviewIFRepository.save(aiInterviewIF);
    }

    // 전체 피드백 처리
    private void handleFullFeedback(AIInterview aiInterview) {
        List<AIInterviewIF> responses = aiInterviewIFRepository.findByAiInterview(aiInterview);
        StringBuilder questionText = new StringBuilder();
        StringBuilder answerText = new StringBuilder();

        for (AIInterviewIF response : responses) {
            questionText.append(response.getI_question()).append(" ");
            answerText.append(response.getI_answer()).append(" ");
        }

        String fullFeedback = getChatGPTFeedback(questionText.toString() + answerText.toString());
        String ttsFeedback = externalAPIService.callTTS(fullFeedback);
        playAudio(ttsFeedback);

        saveFullFeedback(aiInterview, questionText.toString(), answerText.toString(), fullFeedback);
    }

    // 전체 피드백 저장
    public void saveFullFeedback(AIInterview aiInterview, String questions, String answers, String feedback) {
        AIInterviewFF aiInterviewFF = new AIInterviewFF();
        aiInterviewFF.setAiInterview(aiInterview);
        aiInterviewFF.setF_question(questions);
        aiInterviewFF.setF_answer(answers);
        aiInterviewFF.setF_feedback(feedback);
        aiInterviewFFRepository.save(aiInterviewFF);
    }

    // 인터뷰 진행 여부 확인
    private boolean interviewInProgress() {
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
    public AIInterview getInterviewById(Long AIInterviewNo) {
        return aiInterviewRepository.findById(AIInterviewNo).orElse(null);
    }

    // 인터뷰 종료 처리
    private void endInterview(AIInterview aiInterview) {
        interviewInProgress = false;
        System.out.println("Interview session ended.");
        // 인터뷰 종료 후 추가 처리 로직 (예: 상태 업데이트, 로그 기록 등)
    }
}
