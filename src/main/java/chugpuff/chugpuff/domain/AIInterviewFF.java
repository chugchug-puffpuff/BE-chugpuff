package chugpuff.chugpuff.domain;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Getter
@Setter
public class AIInterviewFF {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long AIInterviewFFNo;

    @OneToOne
    @JoinColumn(name = "AIInterviewNo")
    private AIInterview aiInterview;

    private String f_question;
    private String f_answer;

    @Column(columnDefinition = "LONGTEXT")
    private String f_feedback;
}
