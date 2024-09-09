package chugpuff.chugpuff.entity;

import chugpuff.chugpuff.domain.Member;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "scrap")
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
public class Scrap {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private Member member; // 유저 ID


    private String jobId;  // 공고 ID

    // Getters and Setters
}
