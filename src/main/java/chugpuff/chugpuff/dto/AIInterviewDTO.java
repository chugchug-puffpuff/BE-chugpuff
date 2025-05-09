package chugpuff.chugpuff.dto;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class AIInterviewDTO {
    private Long AIInterviewNo;
    private Long user_id;
    private String interviewType;
    private String feedbackType;
    private List<AIInterviewIFDTO> immediateFeedbacks;
    private List<AIInterviewFFDTO> overallFeedbacks;
    private String f_feedback;
}
