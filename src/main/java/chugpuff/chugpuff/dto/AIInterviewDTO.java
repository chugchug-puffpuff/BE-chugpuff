package chugpuff.chugpuff.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class AIInterviewDTO {
    private Long AIInterviewNo;
    private Long user_id;
    private String interviewType;
    private String feedbackType;
}
