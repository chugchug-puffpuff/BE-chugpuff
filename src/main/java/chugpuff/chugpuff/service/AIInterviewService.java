package chugpuff.chugpuff.service;

import chugpuff.chugpuff.domain.AIInterview;
import chugpuff.chugpuff.domain.AIInterviewFF;
import chugpuff.chugpuff.domain.AIInterviewIF;
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
    private ExternalAPIService externalAPIService;

    @Autowired
    private TimerService timerService;

    private TargetDataLine microphone;

    private boolean interviewInProgress = false;

    // AI 면접 저장
    public AIInterview saveInterview(AIInterview aiInterview) {
        return aiInterviewRepository.save(aiInterview);
    }

    // 인터뷰 세션 초기화 및 질문 생성 메서드
    private String initializeInterviewSession(AIInterview aiInterview) {
        String chatPrompt;
        if ("인성 면접".equals(aiInterview.getInterviewType())) {
            chatPrompt = "인성 면접을 시작합니다. 면접의 주제는 '인성면접' 입니다. 한글로 해주세요.";
        } else if ("직무 면접".equals(aiInterview.getInterviewType())) {
            String job = aiInterview.getMember().getJob();
            String jobKeyword = aiInterview.getMember().getJobKeyword();
            chatPrompt = job + " 직무에 대한 면접을 " + jobKeyword + "에 중점을 두고 직무 면접을 시작합니다. " + "면접의 주제는 " + job + " 직무의 " + jobKeyword + "입니다. 한글로 해주세요.";
        } else {
            throw new RuntimeException("Invalid interview type");
        }

        // 피드백 방식을 ChatGPT 프롬프트에 포함
        if ("즉시 피드백".equals(aiInterview.getFeedbackType())) {
            chatPrompt += " 질문은 하나씩만 하고 질문에 대답한 후 즉시 피드백을 제공하고 다음 질문을 해주세요. 질문을 할 때는 \\\"질문 : \\\"이라 말하고 질문해주세요. 바로 질문해주세요.";
        } else if ("전체 피드백".equals(aiInterview.getFeedbackType())) {
            chatPrompt += " 질문은 하나씩만 하고 대답을 하면 다음 질문을 해주세요. 면접이 끝난 후 전체적인 피드백을 제공해주세요. 질문을 할 때는 \\\"질문 : \\\"이라 말하고 질문해주세요. 바로 질문해주세요.";
        }

        System.out.println("Sending to ChatGPT: " + chatPrompt); // ChatGPT 프롬프트 로그 출력
        String firstResponse = externalAPIService.callChatGPT(chatPrompt);

        // 응답에서 질문만 추출
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
                // 질문을 기대했지만 피드백이 반환된 경우에 대한 처리
                throw new RuntimeException("Expected a question, but received feedback instead.");
            }
        } else {
            // "질문: " 또는 "피드백: "으로 시작하지 않는 경우, 유연하게 처리하기 위해 예외를 발생시키지 않고 직접 반환
            return response.trim();
        }
    }

    // 인터뷰 시작 메서드
    @Async
    public void startInterview(Long AIInterviewNo) {
        AIInterview aiInterview = aiInterviewRepository.findById(AIInterviewNo)
                .orElseThrow(() -> new RuntimeException("Interview not found"));

        // 이전 응답 데이터 초기화
        clearPreviousResponses(aiInterview);

        // 인터뷰 세션 초기화 및 첫 번째 질문 생성
        String firstQuestion = initializeInterviewSession(aiInterview);
        interviewInProgress = true;

        timerService.startTimer(30 * 60 * 1000, () -> endInterview(aiInterview));

        // 첫 번째 질문 처리
        String lastResponse = "";
        lastResponse = handleInterviewProcess(aiInterview, firstQuestion, lastResponse);

        while (interviewInProgress) {
            String nextQuestion = getChatGPTQuestion(aiInterview, firstQuestion, lastResponse);
            lastResponse = handleInterviewProcess(aiInterview, nextQuestion, lastResponse);
            firstQuestion = nextQuestion;
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

    // 인터뷰 진행 처리 메서드
    private String handleInterviewProcess(AIInterview aiInterview, String question, String lastResponse) {
        try {
            System.out.println("Generated Question: " + question); // 질문 로그 출력
            String ttsQuestion = externalAPIService.callTTS(question);
            playAudio(ttsQuestion);

            String userAudioResponse = captureUserAudio();
            String sttResponse = externalAPIService.callSTT(userAudioResponse);

            if ("즉시 피드백".equals(aiInterview.getFeedbackType())) {
                // AIInterview 객체를 함께 전달하여 getChatGPTFeedback 호출
                String immediateFeedback = getChatGPTFeedback(sttResponse, aiInterview);
                System.out.println("Generated Feedback: " + immediateFeedback); // 피드백 로그 출력
                String ttsFeedback = externalAPIService.callTTS(immediateFeedback);
                playAudio(ttsFeedback);

                // 질문, 답변, 피드백을 한 번에 저장
                saveImmediateFeedback(aiInterview, question, sttResponse, immediateFeedback);
            } else {
                // 전체 피드백의 경우 피드백 없이 저장 (이 경우는 한 번만 저장될 것입니다)
                saveUserResponse(aiInterview, question, sttResponse);
            }

            return sttResponse; // 마지막 응답 반환

        } catch (Exception e) {
            e.printStackTrace();
            stopAudioCapture(); // 음성 캡처 중지
            interviewInProgress = false;
            return null;
        }
    }

    // ChatGPT로부터 질문 생성
    private String getChatGPTQuestion(AIInterview aiInterview, String lastQuestion, String lastResponse) {
        String chatPrompt;

        if ("직무 면접".equals(aiInterview.getInterviewType())) {
            chatPrompt = String.format(
                    "당신은 지금 %s 직무 면접을 진행 중입니다. 면접의 주제는 '%s 직무의 %s'입니다. "
                            + "이전 질문은: \"%s\" "
                            + "지원자의 대답은: \"%s\" "
                            + "이 정보를 바탕으로, 주제에 맞는 다음 질문을 '질문: '으로 시작하여 생성해 주세요. 주제에서 벗어나지 마세요.",
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
                            + "이 정보를 바탕으로, 주제에 맞는 다음 질문을 '질문: '으로 시작하여 생성해 주세요. 주제에서 벗어나지 마세요.",
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
        String chatPrompt = "다음 응답에 대해 '피드백: '으로 시작하는 피드백을 제공해주세요: " + userResponse;

        if ("직무 면접".equals(aiInterview.getInterviewType())) {
            chatPrompt += String.format(
                    " 이 피드백은 %s 직무 면접을 진행 중이며, 주제는 '%s 직무의 %s'입니다.",
                    aiInterview.getMember().getJob(),
                    aiInterview.getMember().getJob(),
                    aiInterview.getMember().getJobKeyword()
            );
        } else if ("인성 면접".equals(aiInterview.getInterviewType())) {
            chatPrompt += " 이 피드백은 인성 면접 중입니다.";
        }

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

        // AIInterview 객체를 함께 전달하여 getChatGPTFeedback 호출
        String fullFeedback = getChatGPTFeedback(questionText.toString() + answerText.toString(), aiInterview); // AIInterview 객체 추가
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
        try (FileInputStream fileInputStream = new FileInputStream(audioUrl)) {
            Player player = new Player(fileInputStream);
            player.play();
        } catch (FileNotFoundException e) {
            stopAudioCapture();
            throw new RuntimeException("File not found: " + audioUrl, e);
        } catch (JavaLayerException e) {
            stopAudioCapture();
            throw new RuntimeException("Failed to play audio", e);
        } catch (IOException e) {
            stopAudioCapture();
            throw new RuntimeException("IO exception while playing audio", e);
        }
    }

    // 사용자 음성 응답 캡처
    private String captureUserAudio() {
        String audioFilePath = "captured_audio.wav";
        File audioFile = new File(audioFilePath);

        try {
            // 샘플링 속도를 44100 Hz로 설정
            AudioFormat format = new AudioFormat(44100, 16, 1, true, true);
            DataLine.Info info = new DataLine.Info(TargetDataLine.class, format);

            // 마이크 초기화
            microphone = (TargetDataLine) AudioSystem.getLine(info);
            microphone.open(format);
            microphone.start();

            System.out.println("Microphone opened and audio capture started...");

            // AudioInputStream 생성
            AudioInputStream audioStream = new AudioInputStream(microphone);

            // 파일에 음성 데이터를 쓰기 위한 준비
            ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
            byte[] buffer = new byte[4096];
            int silenceThreshold = 1000; // 침묵 시간 기준 (밀리초)
            long silenceDuration = 0;
            boolean isSilent;
            boolean recording = true;

            while (recording) {
                int bytesRead = audioStream.read(buffer, 0, buffer.length);

                if (bytesRead == -1) {
                    break;
                }

                isSilent = true;
                for (int i = 0; i < bytesRead; i++) {
                    if (Math.abs(buffer[i]) > 10) { // 작은 값도 무시하지 않도록 수정
                        isSilent = false;
                        silenceDuration = 0;
                        break;
                    }
                }

                if (isSilent) {
                    silenceDuration += (bytesRead / format.getFrameSize()) / (float) format.getFrameRate() * 1000;
                    System.out.println("Silence duration: " + silenceDuration);
                    if (silenceDuration >= silenceThreshold) {
                        System.out.println("Silence detected. Stopping audio capture.");
                        recording = false;
                    }
                }

                byteArrayOutputStream.write(buffer, 0, bytesRead);
            }

            // ByteArrayOutputStream을 ByteArrayInputStream으로 변환
            byte[] audioData = byteArrayOutputStream.toByteArray();
            ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(audioData);

            // 새 AudioInputStream을 생성하여 파일로 저장
            try (AudioInputStream finalAudioStream = new AudioInputStream(byteArrayInputStream, format, audioData.length / format.getFrameSize())) {
                AudioSystem.write(finalAudioStream, AudioFileFormat.Type.WAVE, audioFile);
                System.out.println("Audio data written to file: " + audioFilePath);
            }

        } catch (LineUnavailableException e) {
            System.err.println("Microphone line is unavailable: " + e.getMessage());
            stopAudioCapture();
            throw new RuntimeException("Failed to open microphone line", e);
        } catch (IOException e) {
            System.err.println("Failed to write audio data to file: " + e.getMessage());
            stopAudioCapture();
            throw new RuntimeException("Failed to capture audio", e);
        } finally {
            stopAudioCapture();
        }

        return audioFilePath;
    }

    // 음성 캡처 중지
    private void stopAudioCapture() {
        if (microphone != null && microphone.isOpen()) {
            microphone.stop();
            microphone.close();
            System.out.println("Audio capture stopped.");
        }
    }

    // AI 면접 ID로 면접 조회
    public AIInterview getInterviewById(Long AIInterviewNo) {
        return aiInterviewRepository.findById(AIInterviewNo).orElse(null);
    }

    // 인터뷰 종료 처리
    private void endInterview(AIInterview aiInterview) {
        interviewInProgress = false;
        stopAudioCapture(); // 인터뷰 종료 시 음성 캡처 중지
        System.out.println("Interview session ended.");
        // 인터뷰 종료 후 추가 처리 로직 (예: 상태 업데이트, 로그 기록 등)
    }
}
