package chugpuff.chugpuff.domain;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Getter
@Setter
public class AIInterviewIF {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long AIInterviewIFNo;

    @ManyToOne
    @JoinColumn(name = "AIInterviewNo")
    private AIInterview aiInterview;

    private String i_question;
    private String i_answer;

    @Column(columnDefinition = "LONGTEXT")
    private String i_feedback;
}
