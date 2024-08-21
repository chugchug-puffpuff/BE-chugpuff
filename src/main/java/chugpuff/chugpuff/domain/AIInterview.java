package chugpuff.chugpuff.domain;

import com.fasterxml.jackson.annotation.JsonManagedReference;
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
    @JsonManagedReference
    private List<AIInterviewIF> immediateFeedbacks;

    @OneToMany(mappedBy = "aiInterview", cascade = CascadeType.ALL)
    @JsonManagedReference
    private List<AIInterviewFF> overallFeedbacks;

    private LocalDateTime aiInterviewDate;

    @OneToMany(mappedBy = "aiInterview", cascade = CascadeType.ALL)
    @JsonManagedReference
    private List<AIInterviewFFB> feedbacks;

    @PrePersist
    protected void onCreate() {
        this.aiInterviewDate = LocalDateTime.now();
    }
}
