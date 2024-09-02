package chugpuff.chugpuff.controller;

import chugpuff.chugpuff.domain.AIInterview;
import chugpuff.chugpuff.domain.Member;
import chugpuff.chugpuff.dto.AIInterviewDTO;
import chugpuff.chugpuff.service.AIInterviewService;
import chugpuff.chugpuff.service.ExternalAPIService;
import chugpuff.chugpuff.service.MemberService;
import chugpuff.chugpuff.service.TimerService;
import lombok.Getter;
import lombok.Setter;
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
import java.util.Map;

@RestController
@RequestMapping("/api/aiinterview")
public class AIInterviewController {

    @Autowired
    private AIInterviewService aiInterviewService;

    @Autowired
    private MemberService memberService;

    @Autowired
    private TimerService timerService;

    @Autowired
    private ExternalAPIService externalAPIService;

    // AI 모의면접 저장
    @PostMapping
    public AIInterview saveInterview(@RequestBody AIInterviewDTO aiInterviewDTO) {
        Member member = memberService.getMemberByUser_id(aiInterviewDTO.getUser_id())
                .orElseThrow(() -> new RuntimeException("Member not found"));

        AIInterview aiInterview = new AIInterview();
        aiInterview.setInterviewType(aiInterviewDTO.getInterviewType());
        aiInterview.setFeedbackType(aiInterviewDTO.getFeedbackType());
        aiInterview.setMember(member);

        return aiInterviewService.saveInterview(aiInterview);
    }

    // AI 모의면접 시작
    @PostMapping("/{AIInterviewNo}/start")
    public void startInterview(@PathVariable Long AIInterviewNo, @AuthenticationPrincipal UserDetails userDetails) {
        AIInterview aiInterview = aiInterviewService.getInterviewById(AIInterviewNo);
        if (aiInterview == null) {
            throw new RuntimeException("Interview not found");
        }

        System.out.println("Logged in user: " + userDetails.getUsername());
        System.out.println("Interview owner user_id: " + aiInterview.getMember().getUser_id());

        aiInterviewService.startInterview(AIInterviewNo);
    }

    // AI 모의면접 중지
    @PostMapping("/{AIInterviewNo}/stop")
    public void stopInterview(@AuthenticationPrincipal UserDetails userDetails) {
        System.out.println("Logged in user: " + userDetails.getUsername());
        timerService.stopTimer();
    }

    // 즉시 피드백 저장
    @PostMapping("/{AIInterviewNo}/immediate-feedback")
    public void saveImmediateFeedback(@PathVariable Long AIInterviewNo, @RequestBody FeedbackRequest feedbackRequest, @AuthenticationPrincipal UserDetails userDetails) {
        System.out.println("Logged in user: " + userDetails.getUsername());
        AIInterview aiInterview = aiInterviewService.getInterviewById(AIInterviewNo);
        if (aiInterview != null) {
            aiInterviewService.saveImmediateFeedback(aiInterview, feedbackRequest.getQuestion(), feedbackRequest.getAnswer(), feedbackRequest.getFeedback());
        }
    }

    // 전체 피드백 저장
    @PostMapping("/{AIInterviewNo}/full-feedback")
    public void saveFullFeedback(@PathVariable Long AIInterviewNo, @RequestBody FeedbackRequest feedbackRequest, @AuthenticationPrincipal UserDetails userDetails) {
        System.out.println("Logged in user: " + userDetails.getUsername());
        AIInterview aiInterview = aiInterviewService.getInterviewById(AIInterviewNo);
        if (aiInterview != null) {
            aiInterviewService.saveFullFeedback(aiInterview, feedbackRequest.getQuestion(), feedbackRequest.getAnswer(), feedbackRequest.getFeedback());
        }
    }

    // TTS 요청 처리
    @PostMapping("/tts")
    public ResponseEntity<FileSystemResource> convertTextToSpeech(@RequestBody Map<String, String> requestBody) {
        String text = requestBody.get("text");
        String audioFilePath = externalAPIService.callTTS(text);
        File audioFile = new File(audioFilePath);
        if (!audioFile.exists()) {
            return ResponseEntity.notFound().build();
        }

        FileSystemResource resource = new FileSystemResource(audioFile);
        HttpHeaders headers = new HttpHeaders();
        headers.add(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=output.mp3");

        return ResponseEntity.ok()
                .headers(headers)
                .contentLength(audioFile.length())
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .body(resource);
    }

    // 녹음을 중지하는 엔드포인트
    @PostMapping("/{AIInterviewNo}/stop-recording")
    public ResponseEntity<String> stopRecording(@PathVariable Long AIInterviewNo) {
        AIInterview aiInterview = aiInterviewService.getInterviewById(AIInterviewNo);
        if (aiInterview == null) {
            return ResponseEntity.badRequest().body("Interview not found");
        }

        aiInterviewService.stopAudioCapture();

        return ResponseEntity.ok("Recording stopped and saved.");
    }

    // 녹음된 파일을 STT로 변환하여 텍스트로 반환하고 ChatGPT로 보내기
    @PostMapping("/{AIInterviewNo}/process-audio-response")
    public ResponseEntity<String> processAudioResponse(@PathVariable Long AIInterviewNo, @RequestParam("audioFile") MultipartFile audioFile) {
        AIInterview aiInterview = aiInterviewService.getInterviewById(AIInterviewNo);
        if (aiInterview == null) {
            return ResponseEntity.badRequest().body("Interview not found");
        }

        String audioFilePath = "captured_audio_" + AIInterviewNo + ".wav";
        File file = new File(audioFilePath);
        try (OutputStream os = new FileOutputStream(file)) {
            os.write(audioFile.getBytes());
        } catch (IOException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Failed to save audio file");
        }

        String sttText = externalAPIService.callSTT(audioFilePath);

        String feedback = aiInterviewService.getChatGPTFeedback(sttText, aiInterview);

        if ("즉시 피드백".equals(aiInterview.getFeedbackType())) {
            aiInterviewService.saveImmediateFeedback(aiInterview, aiInterviewService.getCurrentQuestion(), sttText, feedback);
        }

        String nextQuestion = aiInterviewService.getChatGPTQuestion(aiInterview, aiInterviewService.getCurrentQuestion(), sttText);
        aiInterviewService.handleInterviewProcess(aiInterview, nextQuestion);

        return ResponseEntity.ok(feedback);
    }
}

@Getter
@Setter
class FeedbackRequest {
    private String question;
    private String answer;
    private String feedback;

}
