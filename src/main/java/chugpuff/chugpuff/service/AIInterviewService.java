package chugpuff.chugpuff.service;

import chugpuff.chugpuff.domain.*;
import chugpuff.chugpuff.dto.AIInterviewDTO;
import chugpuff.chugpuff.dto.AIInterviewFFDTO;
import chugpuff.chugpuff.dto.AIInterviewIFDTO;
import chugpuff.chugpuff.entity.EditSelfIntroduction;
import chugpuff.chugpuff.entity.EditSelfIntroductionDetails;
import chugpuff.chugpuff.repository.*;
import javazoom.jl.decoder.JavaLayerException;
import javazoom.jl.player.Player;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import javax.sound.sampled.*;
import java.io.*;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

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
    private EditSelfIntroductionRepository editSelfIntroductionRepository;

    @Autowired
    private EditSelfIntroductionDetailsRepository editSelfIntroductionDetailsRepository;

    @Autowired
    private MemberService memberService;

    @Autowired
    private ExternalAPIService externalAPIService;

    @Autowired
    private TimerService timerService;

    private boolean interviewInProgress = false;
    private String currentQuestion;
    private Player player;
    private TargetDataLine microphone;

    // AI 면접 생성 메서드
    public AIInterview createInterview(AIInterview aiInterview) {
        // JWT 토큰에서 사용자 정보 추출
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String username = authentication.getName();

        // 사용자 정보 확인
        Member member = memberService.getMemberByUsername(username)
                .orElseThrow(() -> new RuntimeException("Member not found"));

        // 면접에 사용자 정보 저장
        aiInterview.setMember(member);

        // AI 면접 생성
        return aiInterviewRepository.save(aiInterview);
    }

    // 인터뷰 세션 초기화 및 첫 질문 생성 메서드
    public String startInterview(AIInterview aiInterview) {
        Member member = aiInterview.getMember(); // 면접에 저장된 사용자 정보 사용

        String chatPrompt = generateChatPrompt(aiInterview, member);
        String firstResponse = externalAPIService.callChatGPT(chatPrompt);
        currentQuestion = extractQuestionOrFeedbackFromResponse(firstResponse, false);
        interviewInProgress = true;

        return currentQuestion;
    }

    // 타이머 시작 메서드
    public long startInterviewTimer(AIInterview aiInterview) {
        if (!interviewInProgress) {
            throw new RuntimeException("Interview is not in progress.");
        }

        long duration = 30 * 60 * 1000;
        timerService.startTimer(duration, () -> endInterview(aiInterview));

        return duration;
    }

    // ChatGPT를 통해 면접 질문을 생성하고 TTS를 함께 호출하여 반환하는 메서드
    public Map<String, String> generateNextQuestion(AIInterview aiInterview) {
        String lastQuestion = getCurrentQuestion();
        String lastResponse = getLastUserResponse(aiInterview);

        Member member = aiInterview.getMember(); // 면접에 저장된 사용자 정보 사용
        String selfIntroduction = "";

        if ("자기소개서 면접".equals(aiInterview.getInterviewType())) {
            selfIntroduction = getSelfIntroductionContentForInterview(member);
        }

        String nextQuestionPrompt = createQuestionPrompt(aiInterview, lastQuestion, lastResponse, selfIntroduction);

        String nextQuestionResponse = externalAPIService.callChatGPT(nextQuestionPrompt);
        String nextQuestion = extractQuestionOrFeedbackFromResponse(nextQuestionResponse, false);

        String ttsAudioUrl = externalAPIService.callTTS(nextQuestion);

        Map<String, String> response = new HashMap<>();
        response.put("question", nextQuestion);
        response.put("ttsAudioUrl", ttsAudioUrl);

        this.currentQuestion = nextQuestion;

        return response;
    }

    // 인터뷰 진행 처리 메서드
    public void handleInterviewProcess(AIInterview aiInterview, String question) {
        try {
            if (!interviewInProgress) {
                return;
            }

            stopAudioCapture();

            currentQuestion = question;
            System.out.println("Generated Question: " + question);
            String ttsQuestion = externalAPIService.callTTS(question);

            if (!interviewInProgress) {
                return;
            }

            playAudio(ttsQuestion);

            if (!interviewInProgress) {
                return;
            }

            captureUserAudio();
        } catch (Exception e) {
            e.printStackTrace();
            interviewInProgress = false;
        }
    }

    // 음성 재생 메서드
    public void playAudio(String audioUrl) {
        if (!interviewInProgress) {
            return;
        }

        System.out.println("Playing audio from URL: " + audioUrl);
        try (FileInputStream fileInputStream = new FileInputStream(audioUrl)) {
            stopCurrentAudio();
            player = new Player(fileInputStream);
            player.play();
        } catch (FileNotFoundException e) {
            throw new RuntimeException("File not found: " + audioUrl, e);
        } catch (JavaLayerException | IOException e) {
            throw new RuntimeException("Failed to play audio", e);
        }
    }

    // 사용자 음성 응답 캡처 매서드
    public void captureUserAudio() {
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

    // 즉시 피드백 생성 및 저장 메서드
    public Map<String, String> generateFeedback(AIInterview aiInterview, String userResponse) {
        String question = currentQuestion;

        System.out.println("Generating feedback for Answer: " + userResponse);

        if (userResponse == null || userResponse.trim().isEmpty()) {
            throw new IllegalArgumentException("User response is empty or null.");
        }

        String feedbackPrompt = "다음 응답에 대해 '피드백: '으로 시작하는 피드백을 제공해주세요: " + userResponse + " " + question + "라는 질문에 대한 답변입니다.";

        if ("직무 면접".equals(aiInterview.getInterviewType())) {
            feedbackPrompt += String.format(
                    " 이 피드백은 %s 직무 면접에 대한 피드백을 주어야하며, 주제는 '%s 직무의 %s'입니다. 면접의 질문에 대한 사용자의 답변에 대해 피드백을 존댓말로 제공해주세요. 200자 내로 제공해주세요.",
                    aiInterview.getMember().getJob(),
                    aiInterview.getMember().getJob(),
                    aiInterview.getMember().getJobKeyword()
            );
        } else if ("인성 면접".equals(aiInterview.getInterviewType())) {
            feedbackPrompt += " 이 피드백은 인성 면접에 대한 피드백을 주어야합니다. 면접의 질문에 대한 사용자의 답변에 대해 피드백을 존댓말로 제공해주세요. 200자 내로 제공해주세요.";
        } else if ("자기소개서 면접".equals(aiInterview.getInterviewType())) {
            feedbackPrompt += " 이 피드백은 자기소개서 면접에 대한 피드백을 주어야합니다. 면접의 질문에 대한 사용자의 답변에 대해 피드백을 존댓말로 제공해주세요. 200자 내로 제공해주세요.";
        }

        String feedback = externalAPIService.callChatGPT(feedbackPrompt);

        String ttsAudioUrl = externalAPIService.callTTS(feedback);

        if ("즉시 피드백".equals(aiInterview.getFeedbackType())) {
            AIInterviewIF aiInterviewIF = new AIInterviewIF();
            aiInterviewIF.setAiInterview(aiInterview);
            aiInterviewIF.setI_question(question);
            aiInterviewIF.setI_answer(userResponse);
            aiInterviewIF.setI_feedback(feedback);

            System.out.println("Saving AIInterviewIF - Question: " + question + ", Answer: " + userResponse + ", Feedback: " + feedback);

            aiInterviewIFRepository.save(aiInterviewIF);
        }

        Map<String, String> response = new HashMap<>();
        response.put("feedback", feedback);
        response.put("ttsAudioUrl", ttsAudioUrl);

        return response;
    }

    // 사용자의 마지막 답변을 서버 상태에서 가져오는 메서드
    public String getLastUserResponse(AIInterview aiInterview) {
        List<AIInterviewIF> responses = aiInterviewIFRepository.findByAiInterview(aiInterview);
        if (responses.isEmpty()) {
            return "";
        }
        return responses.get(responses.size() - 1).getI_answer();
    }

    // 현재 질문을 반환하는 메서드
    public String getCurrentQuestion() {
        return currentQuestion;
    }

    // 질문 및 피드백을 위한 ChatGPT 프롬프트 생성 매서드
    private String generateChatPrompt(AIInterview aiInterview, Member member) {
        String chatPrompt;

        if ("인성 면접".equals(aiInterview.getInterviewType())) {
            chatPrompt = "인성 면접을 시작합니다. 각 요구사항에 맞게 면접을 진행해주세요. 1. 질문을 하나씩 해주세요. 2. 면접의 주제는 '인성면접' 입니다. 3. 한글로 해주세요. 4. 존댓말로 해주세요.";
        } else if ("직무 면접".equals(aiInterview.getInterviewType())) {
            String job = member.getJob();
            String jobKeyword = member.getJobKeyword();
            chatPrompt = job + " 직무에 대한 면접을 " + jobKeyword + "에 중점을 두고 직무 면접을 시작합니다. 각 요구사항에 맞게 면접을 진행해주세요. 1. 질문을 하나씩 해주세요. 2. 면접의 주제는 " + job + " 직무의 " + jobKeyword + "입니다. 3. 한글로 해주세요. 4. 존댓말로 해주세요.";
        } else if ("자기소개서 면접".equals(aiInterview.getInterviewType())) {
            String selfIntroductionContent = getSelfIntroductionContentForInterview(member);
            chatPrompt = selfIntroductionContent + " 이 자기소개서를 기반으로 자기소개서 면접을 시작합니다. 각 요구사항에 맞게 면접을 진행해주세요. 1. 질문을 하나씩 해주세요. 2. 면접의 주제는 '자기소개서 면접' 입니다. 3. 한글로 해주세요. 4. 존댓말로 해주세요.";
        } else {
            throw new RuntimeException("Invalid interview type");
        }

        return chatPrompt;
    }

    // 다음 질문 생성 메서드
    private String createQuestionPrompt(AIInterview aiInterview, String lastQuestion, String lastResponse, String selfIntroduction) {
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
        } else if ("자기소개서 면접".equals(aiInterview.getInterviewType())) {
            chatPrompt = String.format(
                    "당신은 지금 자기소개서 면접을 진행 중입니다. "
                            + "이전 질문은: \"%s\" "
                            + "지원자의 대답은: \"%s\" "
                            + "지원자의 자기소개서 내용은 다음과 같습니다:\n%s\n"
                            + "자기소개서 기반으로 주제에 맞는 다음 질문을 '질문: '으로 시작하여 생성해 주세요. 주제에서 벗어나지 마세요. 존댓말로 해주세요.",
                    lastQuestion,
                    lastResponse,
                    selfIntroduction
            );
        } else {
            throw new RuntimeException("Invalid interview type");
        }

        return chatPrompt;
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

    // 자기소개서 내용을 불러오는 메서드
    public String getSelfIntroductionContentForInterview(Member member) {
        EditSelfIntroduction selfIntroduction = editSelfIntroductionRepository.findByMember(member).stream()
                .filter(EditSelfIntroduction::isSave)
                .findFirst()
                .orElseThrow(() -> new RuntimeException("저장된 자기소개서를 찾을 수 없습니다."));

        List<EditSelfIntroductionDetails> detailsList = editSelfIntroductionDetailsRepository.findByEditSelfIntroduction(selfIntroduction);

        StringBuilder selfIntroductionContent = new StringBuilder();
        selfIntroductionContent.append("다음은 사용자 ").append(member.getName()).append("의 자기소개서입니다:\n");
        for (EditSelfIntroductionDetails detail : detailsList) {
            selfIntroductionContent.append("질문: ").append(detail.getES_question()).append("\n");
            selfIntroductionContent.append("답변: ").append(detail.getES_answer()).append("\n");
        }

        return selfIntroductionContent.toString();
    }

    // 면접 종료 메서드
    public Map<String, String> endInterview(AIInterview aiInterview) {
        interviewInProgress = false;
        Map<String, String> feedbackResponse = null;

        if ("전체 피드백".equals(aiInterview.getFeedbackType())) {
            feedbackResponse = generateFullFeedback(aiInterview);
        }

        stopAudioCapture();
        stopCurrentAudio();
        timerService.stopTimer();
        currentQuestion = null;

        return feedbackResponse;
    }

    // 음성 캡처 중지 메서드
    public void stopAudioCapture() {
        if (microphone != null && microphone.isOpen()) {
            microphone.stop();
            microphone.close();
            System.out.println("Audio capture stopped.");
        }
    }

    // 현재 재생 중인 음성 중지 메서드
    public void stopCurrentAudio() {
        if (player != null) {
            player.close();
            player = null;
            System.out.println("Audio playback stopped.");
        }
    }

    // 전체 피드백 생성 및 저장 메서드
    public Map<String, String> generateFullFeedback(AIInterview aiInterview) {
        List<AIInterviewFF> responses = aiInterviewFFRepository.findByAiInterview(aiInterview);

        if (responses.isEmpty()) {
            Map<String, String> response = new HashMap<>();
            response.put("message", "No responses found for generating full feedback.");
            response.put("ttsAudioUrl", null);
            return response;
        }

        StringBuilder allQuestions = new StringBuilder();
        StringBuilder allResponses = new StringBuilder();

        for (AIInterviewFF response : responses) {
            allQuestions.append(response.getF_question()).append(" ");
            allResponses.append(response.getF_answer()).append(" ");
        }

        String fullFeedbackPrompt = "이 면접에서 다뤄진 모든 질문과 대답을 바탕으로 전체적인 피드백을 제공해주세요: " + allQuestions.toString() + allResponses.toString();

        String fullFeedback = externalAPIService.callChatGPT(fullFeedbackPrompt);

        String ttsAudioUrl = externalAPIService.callTTS(fullFeedback);

        AIInterviewFFB aiInterviewFFB = new AIInterviewFFB();
        aiInterviewFFB.setAiInterview(aiInterview);
        aiInterviewFFB.setF_feedback(fullFeedback);
        aiInterviewFFBRepository.save(aiInterviewFFB);

        Map<String, String> response = new HashMap<>();
        response.put("feedback", fullFeedback);
        response.put("ttsAudioUrl", ttsAudioUrl);

        return response;
    }

    // 즉시 피드백 저장 메서드
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

    public Map<Long, String> userResponses = new HashMap<>();

    // STT 변환 및 응답 저장 메서드
    public Map<String, String> convertAnswerToText(AIInterview aiInterview, String audioFilePath) {
        String sttText = externalAPIService.callSTT(audioFilePath);

        System.out.println("STT Result: " + sttText);

        saveUserResponse(aiInterview, currentQuestion, sttText);

        userResponses.put(aiInterview.getAIInterviewNo(), sttText);

        Map<String, String> response = new HashMap<>();
        response.put("answer", sttText);
        return response;
    }

    // 전체 패드백 응답 저장 메서드
    public void saveUserResponse(AIInterview aiInterview, String question, String response) {
        if ("전체 피드백".equals(aiInterview.getFeedbackType())) {
            AIInterviewFF aiInterviewFF = new AIInterviewFF();
            aiInterviewFF.setAiInterview(aiInterview);
            aiInterviewFF.setF_question(question);
            aiInterviewFF.setF_answer(response);
            aiInterviewFFRepository.save(aiInterviewFF);
        }
    }

    // 면접 요약 가져오기
    public Map<String, Object> getInterviewSummary(AIInterview aiInterview) {
        Map<String, Object> summary = new HashMap<>();

        if ("즉시 피드백".equals(aiInterview.getFeedbackType())) {
            List<AIInterviewIF> immediateFeedbacks = aiInterviewIFRepository.findByAiInterview(aiInterview);
            summary.put("interviewType", "즉시 피드백");
            summary.put("questionsAndAnswers", immediateFeedbacks.stream().map(ifFeedback -> {
                Map<String, String> qa = new HashMap<>();
                qa.put("question", ifFeedback.getI_question());
                qa.put("answer", ifFeedback.getI_answer());
                qa.put("feedback", ifFeedback.getI_feedback());
                return qa;
            }).collect(Collectors.toList()));
        }
        else if ("전체 피드백".equals(aiInterview.getFeedbackType())) {
            List<AIInterviewFF> fullFeedbacks = aiInterviewFFRepository.findByAiInterview(aiInterview);
            summary.put("interviewType", "전체 피드백");
            summary.put("questionsAndAnswers", fullFeedbacks.stream().map(ffFeedback -> {
                Map<String, String> qa = new HashMap<>();
                qa.put("question", ffFeedback.getF_question());
                qa.put("answer", ffFeedback.getF_answer());
                return qa;
            }).collect(Collectors.toList()));
        }

        return summary;
    }

    // AI 면접 저장 메서드
    public AIInterview saveInterview(AIInterview aiInterview) {
        return aiInterviewRepository.save(aiInterview);
    }

    // AIInterviewNo로 면접 조회 메서드
    public AIInterview getInterviewById(Long AIInterviewNo) {
        return aiInterviewRepository.findById(AIInterviewNo).orElse(null);
    }

    // id로 면접 조회 메서드
    public List<AIInterview> findByMemberId(String id) {
        return aiInterviewRepository.findByMemberId(id);
    }

    // AIInterviewNo로 면접 삭제 메서드
    public void deleteInterviewById(Long AIInterviewNo) {
        AIInterview aiInterview = aiInterviewRepository.findById(AIInterviewNo)
                .orElseThrow(() -> new RuntimeException("Interview not found with ID: " + AIInterviewNo));
        aiInterviewRepository.delete(aiInterview);
    }

    public AIInterviewDTO convertToDTO(AIInterview aiInterview) {
        AIInterviewDTO dto = new AIInterviewDTO();
        dto.setAIInterviewNo(aiInterview.getAIInterviewNo());
        dto.setUser_id(aiInterview.getMember().getUser_id());
        dto.setInterviewType(aiInterview.getInterviewType());
        dto.setFeedbackType(aiInterview.getFeedbackType());
        dto.setImmediateFeedbacks(
                aiInterview.getImmediateFeedbacks().stream().map(this::convertToIFDTO).collect(Collectors.toList())
        );
        dto.setOverallFeedbacks(
                aiInterview.getOverallFeedbacks().stream().map(this::convertToFFDTO).collect(Collectors.toList())
        );
        if (aiInterview.getFeedbacks() != null && !aiInterview.getFeedbacks().isEmpty()) {
            dto.setF_feedback(aiInterview.getFeedbacks().get(0).getF_feedback());
        }
        return dto;
    }

    private AIInterviewIFDTO convertToIFDTO(AIInterviewIF aiInterviewIF) {
        AIInterviewIFDTO dto = new AIInterviewIFDTO();
        dto.setAIInterviewIFNo(aiInterviewIF.getAIInterviewIFNo());
        dto.setI_question(aiInterviewIF.getI_question());
        dto.setI_answer(aiInterviewIF.getI_answer());
        dto.setI_feedback(aiInterviewIF.getI_feedback());
        return dto;
    }

    private AIInterviewFFDTO convertToFFDTO(AIInterviewFF aiInterviewFF) {
        AIInterviewFFDTO dto = new AIInterviewFFDTO();
        dto.setAIInterviewFFNo(aiInterviewFF.getAIInterviewFFNo());
        dto.setF_question(aiInterviewFF.getF_question());
        dto.setF_answer(aiInterviewFF.getF_answer());
        return dto;
    }
}
