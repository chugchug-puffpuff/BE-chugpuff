package chugpuff.chugpuff.domain;

import com.fasterxml.jackson.annotation.JsonBackReference;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Getter
@Setter
public class AIInterviewFFB {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long AIInterviewFFBNo;

    @ManyToOne
    @JoinColumn(name = "AIInterviewNo")
    @JsonBackReference
    private AIInterview aiInterview;

    @Column(columnDefinition = "LONGTEXT")
    private String f_feedback;
}
