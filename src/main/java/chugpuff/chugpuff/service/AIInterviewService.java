package chugpuff.chugpuff.service;

import chugpuff.chugpuff.domain.AIInterview;
import chugpuff.chugpuff.domain.AIInterviewFF;
import chugpuff.chugpuff.domain.AIInterviewFFB;
import chugpuff.chugpuff.domain.AIInterviewIF;
import chugpuff.chugpuff.repository.AIInterviewFFBRepository;
import chugpuff.chugpuff.repository.AIInterviewRepository;
import chugpuff.chugpuff.repository.AIInterviewFFRepository;
import chugpuff.chugpuff.repository.AIInterviewIFRepository;
import javazoom.jl.decoder.JavaLayerException;
import javazoom.jl.player.Player;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import javax.sound.sampled.*;
import java.io.*;
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
    private AIInterviewFFBRepository aiInterviewFFBRepository;

    @Autowired
    private ExternalAPIService externalAPIService;

    @Autowired
    private TimerService timerService;

    private TargetDataLine microphone;

    private boolean interviewInProgress = false;

    private String currentQuestion;

    private Player player;

    // AI 면접 저장
    public AIInterview saveInterview(AIInterview aiInterview) {
        return aiInterviewRepository.save(aiInterview);
    }

    // 인터뷰 세션 초기화 및 질문 생성 메서드
    private String initializeInterviewSession(AIInterview aiInterview) {
        String chatPrompt;
        if ("인성 면접".equals(aiInterview.getInterviewType())) {
            chatPrompt = "인성 면접을 시작합니다. 각 요구사항에 맞게 면접을 진행해주세요. 1. 질문과 피드백만 해주세요. 2. 면접의 주제는 '인성면접' 입니다. 3. 한글로 해주세요. 4. 존댓말로 해주세요.";
        } else if ("직무 면접".equals(aiInterview.getInterviewType())) {
            String job = aiInterview.getMember().getJob();
            String jobKeyword = aiInterview.getMember().getJobKeyword();
            chatPrompt = job + " 직무에 대한 면접을 " + jobKeyword + "에 중점을 두고 직무 면접을 시작합니다. 각 요구사항에 맞게 면접을 진행해주세요. 1. 질문과 피드백만 해주세요. 2. 면접의 주제는 " + job + " 직무의 " + jobKeyword + "입니다. 3. 한글로 해주세요. 4. 존댓말로 해주세요.";
        } else {
            throw new RuntimeException("Invalid interview type");
        }

        if ("즉시 피드백".equals(aiInterview.getFeedbackType())) {
            chatPrompt += " 4. 질문은 하나씩만 합니다. 5. 사용자가 질문에 대답을 하면, 즉시 피드백을 제공하고 다음 질문을 해주세요. 존댓말로 해주세요.";
        } else if ("전체 피드백".equals(aiInterview.getFeedbackType())) {
            chatPrompt += " 4. 질문은 하나씩만 합니다. 5. 사용자가 질문에 대답을 하면, 다음 질문을 해주세요. 6. 면접이 끝난 후 전체적인 피드백을 제공해주세요. 존댓말로 해주세요.";
        }

        System.out.println("Sending to ChatGPT: " + chatPrompt);
        String firstResponse = externalAPIService.callChatGPT(chatPrompt);

        return extractQuestionOrFeedbackFromResponse(firstResponse, false);
    }

    // 응답에서 질문 또는 피드백을 추출하는 메서드
    private String extractQuestionOrFeedbackFromResponse(String response, boolean isFeedback) {
        if (response.startsWith("질문: ")) {
            return response.substring("질문: ".length()).trim();
        } else if (response.startsWith("피드백: ")) {
            if (isFeedback) {
                return response.substring("피드백: ".length()).trim();
            } else {
                throw new RuntimeException("Expected a question, but received feedback instead.");
            }
        } else {
            return response.trim();
        }
    }

    // 인터뷰 시작 메서드
    @Async
    public void startInterview(Long AIInterviewNo) {
        AIInterview aiInterview = aiInterviewRepository.findById(AIInterviewNo)
                .orElseThrow(() -> new RuntimeException("Interview not found"));

        if (interviewInProgress) {
            System.out.println("Another interview is already in progress. Ending the previous interview.");
            endInterview(aiInterview);
        }

        currentQuestion = initializeInterviewSession(aiInterview);
        interviewInProgress = true;

        timerService.startTimer(30 * 60 * 1000, () -> {
            endInterview(aiInterview);
        });

        handleInterviewProcess(aiInterview, currentQuestion);
    }

    // 현재 질문을 반환하는 메서드
    public String getCurrentQuestion() {
        return currentQuestion;
    }

    // 인터뷰 진행 처리 메서드
    public void handleInterviewProcess(AIInterview aiInterview, String question) {
        try {
            if (!interviewInProgress) { // 인터뷰가 진행 중인지 확인
                return; // 인터뷰가 종료된 경우 처리 중단
            }

            stopAudioCapture();

            currentQuestion = question;
            System.out.println("Generated Question: " + question);
            String ttsQuestion = externalAPIService.callTTS(question);

            if (!interviewInProgress) { // TTS 처리 후 다시 인터뷰가 진행 중인지 확인
                return; // 인터뷰가 종료된 경우 처리 중단
            }

            playAudio(ttsQuestion);

            // 타이머 종료 후 인터뷰가 종료되었는지 확인
            if (!interviewInProgress) {
                return;
            }

            captureUserAudio();
        } catch (Exception e) {
            e.printStackTrace();
            interviewInProgress = false;
        }
    }

    // ChatGPT로부터 질문 생성
    public String getChatGPTQuestion(AIInterview aiInterview, String lastQuestion, String lastResponse) {
        String chatPrompt;

        if ("직무 면접".equals(aiInterview.getInterviewType())) {
            chatPrompt = String.format(
                    "당신은 지금 %s 직무 면접을 진행 중입니다. 면접의 주제는 '%s 직무의 %s'입니다. "
                            + "이전 질문은: \"%s\" "
                            + "지원자의 대답은: \"%s\" "
                            + "주제에 맞는 다음 질문을 '질문: '으로 시작하여 생성해 주세요. 주제에서 벗어나지 마세요. 존댓말로 해주세요.",
                    aiInterview.getMember().getJob(),
                    aiInterview.getMember().getJob(),
                    aiInterview.getMember().getJobKeyword(),
                    lastQuestion,
                    lastResponse
            );
        } else if ("인성 면접".equals(aiInterview.getInterviewType())) {
            chatPrompt = String.format(
                    "당신은 지금 인성 면접을 진행 중입니다. 면접의 주제는 '인성면접' 입니다. "
                            + "이전 질문은: \"%s\" "
                            + "지원자의 대답은: \"%s\" "
                            + "주제에 맞는 다음 질문을 '질문: '으로 시작하여 생성해 주세요. 주제에서 벗어나지 마세요. 존댓말로 해주세요.",
                    lastQuestion,
                    lastResponse
            );
        } else {
            throw new RuntimeException("Invalid interview type");
        }

        return externalAPIService.callChatGPT(chatPrompt);
    }

    // ChatGPT로부터 피드백 생성
    public String getChatGPTFeedback(String userResponse, AIInterview aiInterview) {
        if (!interviewInProgress) {
            return null;
        }

        String question = currentQuestion;
        String chatPrompt = "다음 응답에 대해 '피드백: '으로 시작하는 피드백을 제공해주세요: " + userResponse + " " + question + "라는 질문에 대한 답변입니다.";

        if ("직무 면접".equals(aiInterview.getInterviewType())) {
            chatPrompt += String.format(
                    " 이 피드백은 %s 직무 면접에 대한 피드백을 주어야하며, 주제는 '%s 직무의 %s'입니다. 면접의 질문에 대한 사용자의 답변에 대해 피드백을 존댓말로 제공해주세요. 200자 내로 제공해주세요.",
                    aiInterview.getMember().getJob(),
                    aiInterview.getMember().getJob(),
                    aiInterview.getMember().getJobKeyword()
            );
        } else if ("인성 면접".equals(aiInterview.getInterviewType())) {
            chatPrompt += " 이 피드백은 인성 면접에 대한 피드백을 주어야합니다. 면접의 질문에 대한 사용자의 답변에 대해 피드백을 존댓말로 제공해주세요. 200자 내로 제공해주세요.";
        }

        System.out.println("Sending to ChatGPT: " + chatPrompt);
        String feedback = externalAPIService.callChatGPT(chatPrompt);

        if (!interviewInProgress) {
            return null;
        }

        String ttsFeedback = externalAPIService.callTTS(feedback);
        playAudio(ttsFeedback);

        return feedback;
    }

    // 즉시 피드백 저장
    public void saveImmediateFeedback(AIInterview aiInterview, String question, String response, String feedback) {
        if (!interviewInProgress) {
            System.out.println("Interview has ended, immediate feedback is not saved.");
            return;
        }
        AIInterviewIF aiInterviewIF = new AIInterviewIF();
        aiInterviewIF.setAiInterview(aiInterview);
        aiInterviewIF.setI_question(question);
        aiInterviewIF.setI_answer(response);
        aiInterviewIF.setI_feedback(feedback);
        aiInterviewIFRepository.save(aiInterviewIF);
    }

    // 사용자 응답 저장
    public void saveUserResponse(AIInterview aiInterview, String question, String response) {
        if (!interviewInProgress) {
            System.out.println("Interview has ended, user response is not saved.");
            return;
        }

        if ("전체 피드백".equals(aiInterview.getFeedbackType())) {
            AIInterviewFF aiInterviewFF = new AIInterviewFF();
            aiInterviewFF.setAiInterview(aiInterview);
            aiInterviewFF.setF_question(question);
            aiInterviewFF.setF_answer(response);
            aiInterviewFFRepository.save(aiInterviewFF);
        }
    }

    // 전체 피드백 저장
    public void saveFullFeedback(AIInterview aiInterview, String questions, String answers) {
        if (!interviewInProgress) {
            System.out.println("Interview has ended, full feedback is not saved.");
            return;
        }

        // AIInterviewFF에 질문과 답변 저장
        AIInterviewFF aiInterviewFF = new AIInterviewFF();
        aiInterviewFF.setAiInterview(aiInterview);
        aiInterviewFF.setF_question(questions);
        aiInterviewFF.setF_answer(answers);
        aiInterviewFFRepository.save(aiInterviewFF);
    }

    // 전체 피드백 처리
    private void handleFullFeedback(AIInterview aiInterview) {
        List<AIInterviewFF> responses = aiInterviewFFRepository.findByAiInterview(aiInterview);

        if (responses.isEmpty()) {
            System.out.println("No responses found for generating full feedback.");
            return;  // 이전 응답이 없으면 피드백 생성하지 않음
        }

        StringBuilder allQuestions = new StringBuilder();
        StringBuilder allResponses = new StringBuilder();

        for (AIInterviewFF response : responses) {
            allQuestions.append(response.getF_question()).append(" ");
            allResponses.append(response.getF_answer()).append(" ");
        }

        String fullFeedbackPrompt = "이 면접에서 다뤄진 모든 질문과 대답을 바탕으로 전체적인 피드백을 제공해주세요: " + allQuestions.toString() + allResponses.toString();
        String fullFeedback = externalAPIService.callChatGPT(fullFeedbackPrompt);

        AIInterviewFFB aiInterviewFFB = new AIInterviewFFB();
        aiInterviewFFB.setAiInterview(aiInterview);
        aiInterviewFFB.setF_feedback(fullFeedback);
        aiInterviewFFBRepository.save(aiInterviewFFB);
    }

    // 인터뷰 진행 여부 확인
    private boolean interviewInProgress() {
        return interviewInProgress;
    }

    // 음성 재생
    public void playAudio(String audioUrl) {
        if (!interviewInProgress) {
            return;
        }

        System.out.println("Playing audio from URL: " + audioUrl);
        try (FileInputStream fileInputStream = new FileInputStream(audioUrl)) {
            stopCurrentAudio(); // 현재 재생 중인 오디오가 있다면 중지
            player = new Player(fileInputStream);
            player.play();
        } catch (FileNotFoundException e) {
            throw new RuntimeException("File not found: " + audioUrl, e);
        } catch (JavaLayerException | IOException e) {
            throw new RuntimeException("Failed to play audio", e);
        }
    }

    // 사용자 음성 응답 캡처
    private void captureUserAudio() {
        if (!interviewInProgress) {
            return;
        }

        new Thread(() -> {
            String audioFilePath = "captured_audio.wav";
            File audioFile = new File(audioFilePath);

            try {
                AudioFormat format = new AudioFormat(44100, 16, 1, true, true);
                DataLine.Info info = new DataLine.Info(TargetDataLine.class, format);

                microphone = (TargetDataLine) AudioSystem.getLine(info);
                microphone.open(format);
                microphone.start();

                System.out.println("Microphone opened and audio capture started...");

                AudioInputStream audioStream = new AudioInputStream(microphone);
                AudioSystem.write(audioStream, AudioFileFormat.Type.WAVE, audioFile);
                System.out.println("Audio data written to file: " + audioFilePath);

            } catch (LineUnavailableException | IOException e) {
                System.err.println("Failed to capture audio: " + e.getMessage());
                stopAudioCapture();
            }
        }).start();
    }

    // 음성 캡처 중지
    public void stopAudioCapture() {
        if (microphone != null && microphone.isOpen()) {
            microphone.stop();
            microphone.close();
            System.out.println("Audio capture stopped.");
        }
    }

    // 현재 재생 중인 음성 중지 로직
    public void stopCurrentAudio() {
        if (player != null) {
            player.close();
            player = null;
            System.out.println("Audio playback stopped.");
        }
    }

    // AI 면접 ID로 면접 조회
    public AIInterview getInterviewById(Long AIInterviewNo) {
        return aiInterviewRepository.findById(AIInterviewNo).orElse(null);
    }

    // 인터뷰 종료 처리
    public void endInterview(AIInterview aiInterview) {
        if (!interviewInProgress) {
            return;
        }

        // 인터뷰를 즉시 종료 상태로 설정
        interviewInProgress = false;

        // 전체 피드백 생성
        if ("전체 피드백".equals(aiInterview.getFeedbackType())) {
            List<AIInterviewFF> responses = aiInterviewFFRepository.findByAiInterview(aiInterview);
            if (!responses.isEmpty()) {
                handleFullFeedback(aiInterview);  // 전체 피드백을 생성하고 저장
            } else {
                System.out.println("No previous responses found. Skipping feedback generation.");
            }
        }

        stopAudioCapture(); // 음성 캡처 중지
        stopCurrentAudio(); // 현재 재생 중인 오디오 중지
        timerService.stopTimer(); // 타이머 중지

        System.out.println("Interview session ended.");

        currentQuestion = null; // 다음 질문을 막기 위해 currentQuestion도 초기화
    }
}
