package chugpuff.chugpuff.controller;

import chugpuff.chugpuff.domain.AIInterview;
import chugpuff.chugpuff.domain.Member;
import chugpuff.chugpuff.dto.AIInterviewDTO;
import chugpuff.chugpuff.service.AIInterviewService;
import chugpuff.chugpuff.service.MemberService;
import chugpuff.chugpuff.service.TimerService;
import lombok.Getter;
import lombok.Setter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/aiinterview")
public class AIInterviewController {

    @Autowired
    private AIInterviewService aiInterviewService;

    @Autowired
    private MemberService memberService;

    @Autowired
    private TimerService timerService;

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
    @PostMapping("/{id}/start")
    public void startInterview(@PathVariable Long id, @AuthenticationPrincipal UserDetails userDetails) {
        AIInterview aiInterview = aiInterviewService.getInterviewById(id);
        if (aiInterview == null) {
            throw new RuntimeException("Interview not found");
        }

        // 사용자 정보를 출력하여 디버깅
        System.out.println("Logged in user: " + userDetails.getUsername());
        System.out.println("Interview owner user_id: " + aiInterview.getMember().getUser_id());

        // 여기서는 검증을 하지 않지만 로그를 남깁니다.
        aiInterviewService.startInterview(id);
    }

    // AI 모의면접 중지
    @PostMapping("/{id}/stop")
    public void stopInterview(@AuthenticationPrincipal UserDetails userDetails) {
        System.out.println("Logged in user: " + userDetails.getUsername());
        timerService.stopTimer();
    }

    // 즉시 피드백 저장
    @PostMapping("/{id}/immediate-feedback")
    public void saveImmediateFeedback(@PathVariable Long id, @RequestBody FeedbackRequest feedbackRequest, @AuthenticationPrincipal UserDetails userDetails) {
        System.out.println("Logged in user: " + userDetails.getUsername());
        AIInterview aiInterview = aiInterviewService.getInterviewById(id);
        if (aiInterview != null) {
            aiInterviewService.saveImmediateFeedback(aiInterview, feedbackRequest.getQuestion(), feedbackRequest.getAnswer(), feedbackRequest.getFeedback());
        }
    }

    // 전체 피드백 저장
    @PostMapping("/{id}/overall-feedback")
    public void saveOverallFeedback(@PathVariable Long id, @RequestBody FeedbackRequest feedbackRequest, @AuthenticationPrincipal UserDetails userDetails) {
        System.out.println("Logged in user: " + userDetails.getUsername());
        AIInterview aiInterview = aiInterviewService.getInterviewById(id);
        if (aiInterview != null) {
            aiInterviewService.saveOverallFeedback(aiInterview, feedbackRequest.getQuestion(), feedbackRequest.getAnswer(), feedbackRequest.getFeedback());
        }
    }
}

@Getter
@Setter
class FeedbackRequest {
    private String question;
    private String answer;
    private String feedback;
}
