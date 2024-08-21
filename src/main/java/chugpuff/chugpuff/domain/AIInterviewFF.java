package chugpuff.chugpuff.domain;

import com.fasterxml.jackson.annotation.JsonBackReference;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Getter
@Setter
public class AIInterviewFF {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long AIInterviewFFNo;

    @ManyToOne
    @JoinColumn(name = "AIInterviewNo")
    @JsonBackReference
    private AIInterview aiInterview;

    private String f_question;
    private String f_answer;
}
