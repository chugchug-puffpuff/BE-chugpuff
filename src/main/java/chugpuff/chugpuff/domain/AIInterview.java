package chugpuff.chugpuff.domain;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.List;

@Entity
@Getter
@Setter
public class AIInterview {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long AIInterviewNo;

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private Member member;

    private String interviewType;
    private String feedbackType;

    @OneToMany(mappedBy = "aiInterview", cascade = CascadeType.ALL)
    private List<AIInterviewIF> immediateFeedbacks;

    @OneToOne(mappedBy = "aiInterview", cascade = CascadeType.ALL)
    private AIInterviewFF overallFeedback;

    private LocalDateTime aiInterviewDate;

    @PrePersist
    protected void onCreate() {
        this.aiInterviewDate = LocalDateTime.now();
    }
}
