package chugpuff.chugpuff.controller;

import chugpuff.chugpuff.domain.AIInterview;
import chugpuff.chugpuff.service.AIInterviewService;
import lombok.Getter;
import lombok.Setter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/interviews")
public class AIInterviewController {

    @Autowired
    private AIInterviewService aiInterviewService;

    @PostMapping
    public AIInterview saveInterview(@RequestBody AIInterview aiInterview) {
        return aiInterviewService.saveInterview(aiInterview);
    }

    @PostMapping("/{id}/immediate-feedback")
    public void saveImmediateFeedback(@PathVariable Long id, @RequestBody FeedbackRequest feedbackRequest) {
        AIInterview aiInterview = aiInterviewService.getInterviewById(id);
        if (aiInterview != null) {
            aiInterviewService.saveImmediateFeedback(aiInterview, feedbackRequest.getQuestion(), feedbackRequest.getAnswer(), feedbackRequest.getFeedback());
        }
    }

    @PostMapping("/{id}/overall-feedback")
    public void saveOverallFeedback(@PathVariable Long id, @RequestBody FeedbackRequest feedbackRequest) {
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
