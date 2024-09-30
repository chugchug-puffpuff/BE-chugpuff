package chugpuff.chugpuff.controller;

import chugpuff.chugpuff.domain.AIInterview;
import chugpuff.chugpuff.domain.Member;
import chugpuff.chugpuff.dto.AIInterviewDTO;
import chugpuff.chugpuff.service.AIInterviewService;
import chugpuff.chugpuff.service.ExternalAPIService;
import chugpuff.chugpuff.service.MemberService;
import chugpuff.chugpuff.service.TimerService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.FileSystemResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/aiinterview")
public class AIInterviewController {

    @Autowired
    private AIInterviewService aiInterviewService;

    @Autowired
    private MemberService memberService;

    @Autowired
    private ExternalAPIService externalAPIService;

    @Autowired
    private TimerService timerService;

    // AI 면접 생성
    @PostMapping
    public ResponseEntity<?> createInterview(@RequestBody AIInterviewDTO aiInterviewDTO) {
        return aiInterviewService.createInterview(aiInterviewDTO);
    }

    // 모의면접 세션 초기화 및 첫 질문 생성
    @PostMapping("/{AIInterviewNo}/start")
    public ResponseEntity<Map<String, String>> startInterview(@PathVariable Long AIInterviewNo) {
        AIInterview aiInterview = aiInterviewService.getInterviewById(AIInterviewNo);
        if (aiInterview == null) {
            return ResponseEntity.badRequest().body(null);
        }
        String firstQuestion = aiInterviewService.startInterview(aiInterview);
        String ttsAudioUrl = externalAPIService.callTTS(firstQuestion);

        Map<String, String> response = new HashMap<>();
        response.put("question", firstQuestion);
        response.put("ttsAudioUrl", ttsAudioUrl);

        return ResponseEntity.ok(response);
    }

    // 타이머 시작
    @PostMapping("/{AIInterviewNo}/start-timer")
    public ResponseEntity<Map<String, Object>> startInterviewTimer(@PathVariable Long AIInterviewNo) {
        AIInterview aiInterview = aiInterviewService.getInterviewById(AIInterviewNo);
        if (aiInterview == null) {
            return ResponseEntity.badRequest().body(null);
        }

        aiInterviewService.startInterviewTimer(aiInterview);

        Map<String, Object> remainingTime = timerService.getRemainingTime();
        return ResponseEntity.ok(remainingTime);
    }

    // 답변 녹음 시작
    @PostMapping("/{AIInterviewNo}/answer-start")
    public ResponseEntity<String> startAnswerRecording(@PathVariable Long AIInterviewNo) {
        AIInterview aiInterview = aiInterviewService.getInterviewById(AIInterviewNo);
        if (aiInterview == null) {
            return ResponseEntity.badRequest().body("Interview not found");
        }

        aiInterviewService.captureUserAudio();
        return ResponseEntity.ok("녹음 시작");
    }

    // 답변 녹음 완료
    @PostMapping("/{AIInterviewNo}/answer-complete")
    public ResponseEntity<Map<String, String>> completeAnswerRecording(@PathVariable Long AIInterviewNo) {
        AIInterview aiInterview = aiInterviewService.getInterviewById(AIInterviewNo);
        if (aiInterview == null) {
            return ResponseEntity.badRequest().body(null);
        }

        Map<String, String> response = aiInterviewService.completeAnswerRecordingWithAudioUrl(AIInterviewNo);
        return ResponseEntity.ok(response);
    }

    // 녹음된 파일을 STT로 변환하여 텍스트로 반환
    @PostMapping("/{AIInterviewNo}/convert-answer")
    public ResponseEntity<Map<String, String>> convertAnswerToText(@PathVariable Long AIInterviewNo, @RequestParam("audioFile") MultipartFile audioFile) {
        AIInterview aiInterview = aiInterviewService.getInterviewById(AIInterviewNo);
        if (aiInterview == null) {
            return ResponseEntity.badRequest().body(null);
        }

        String audioFilePath = "captured_audio_" + AIInterviewNo + ".wav";
        File file = new File(audioFilePath);
        try (OutputStream os = new FileOutputStream(file)) {
            os.write(audioFile.getBytes());
        } catch (IOException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }

        Map<String, String> response = aiInterviewService.convertAnswerToText(aiInterview, audioFilePath);
        return ResponseEntity.ok(response);
    }

    // 피드백 생성 및 TTS 변환
    @PostMapping("/{AIInterviewNo}/generate-feedback")
    public ResponseEntity<Map<String, String>> generateFeedback(@PathVariable Long AIInterviewNo) {
        AIInterview aiInterview = aiInterviewService.getInterviewById(AIInterviewNo);
        if (aiInterview == null) {
            return ResponseEntity.badRequest().body(null);
        }

        Map<String, String> response;
        if ("전체 피드백".equals(aiInterview.getFeedbackType())) {
            response = aiInterviewService.generateFullFeedback(aiInterview);
        } else {
            String lastUserResponse = aiInterviewService.userResponses.get(aiInterview.getAIInterviewNo());
            response = aiInterviewService.generateFeedback(aiInterview, lastUserResponse);
        }

        return ResponseEntity.ok(response);
    }

    // 다음 질문 생성 및 TTS 변환
    @PostMapping("/{AIInterviewNo}/next-question")
    public ResponseEntity<Map<String, String>> getNextQuestion(@PathVariable Long AIInterviewNo) {
        AIInterview aiInterview = aiInterviewService.getInterviewById(AIInterviewNo);
        if (aiInterview == null) {
            return ResponseEntity.badRequest().body(null);
        }

        Map<String, String> response = aiInterviewService.generateNextQuestion(aiInterview);
        return ResponseEntity.ok(response);
    }

    // AI 모의면접 종료
    @PostMapping("/{AIInterviewNo}/end")
    public ResponseEntity<Map<String, Object>> endInterview(@PathVariable Long AIInterviewNo) {
        AIInterview aiInterview = aiInterviewService.getInterviewById(AIInterviewNo);
        if (aiInterview == null) {
            return ResponseEntity.badRequest().build();
        }

        Map<String, String> feedbackResponse = aiInterviewService.endInterview(aiInterview);

        Map<String, Object> interviewSummary = aiInterviewService.getInterviewSummary(aiInterview);

        if ("전체 피드백".equals(aiInterview.getFeedbackType()) && feedbackResponse != null) {
            interviewSummary.put("overallFeedback", feedbackResponse.get("feedback"));
        }

        return ResponseEntity.ok(interviewSummary);
    }

    // AI 모의면접 삭제
    @DeleteMapping("/{AIInterviewNo}")
    public ResponseEntity<Void> deleteInterview(@PathVariable Long AIInterviewNo) {
        aiInterviewService.deleteInterviewById(AIInterviewNo);
        return ResponseEntity.noContent().build();
    }

    // AIInterviewNo로 면접 조회
    @GetMapping("/{AIInterviewNo}")
    public ResponseEntity<AIInterviewDTO> getInterviewById(@PathVariable Long AIInterviewNo) {
        AIInterview aiInterview = aiInterviewService.getInterviewById(AIInterviewNo);
        if (aiInterview == null) {
            return ResponseEntity.status(404).body(null);
        }
        AIInterviewDTO aiInterviewDTO = aiInterviewService.convertToDTO(aiInterview);
        return ResponseEntity.ok(aiInterviewDTO);
    }

    // id로 면접 조회
    @GetMapping("/id/{id}")
    public List<AIInterview> getInterviewsByMember(@PathVariable String id) {
        return aiInterviewService.findByMemberId(id);
    }
}
