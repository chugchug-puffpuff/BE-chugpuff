package chugpuff.chugpuff.entity;

import chugpuff.chugpuff.domain.Member;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Data
@Builder
@Getter
@Setter
@ToString
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "editSelfIntroductionDetails")
public class EditSelfIntroductionDetails {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long eSD_no;

    @ManyToOne
    @JoinColumn(name = "eS_no", nullable = false)
    private EditSelfIntroduction editSelfIntroduction;

    @JsonProperty("eS_question")
    @Column(nullable = false, columnDefinition = "TEXT")
    private String eS_question;

    @JsonProperty("eS_answer")
    @Column(nullable = false, columnDefinition = "LONGTEXT")
    private String eS_answer;

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private Member member;
}