package chugpuff.chugpuff.entity;

import chugpuff.chugpuff.domain.Member;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.util.List;

@Entity
@Data
@Builder
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@ToString(exclude = "details")
@Table(name = "editSelfIntroduction")
public class EditSelfIntroduction {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long eS_no;

    @Column(nullable = false, columnDefinition = "LONGTEXT")
    private String eS_feedback = "";

    @Column(nullable = false)
    private LocalDate eS_date;

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private Member member;

    @OneToMany(mappedBy = "editSelfIntroduction", cascade = CascadeType.ALL)
    private List<EditSelfIntroductionDetails> details;

    private boolean save;

}
