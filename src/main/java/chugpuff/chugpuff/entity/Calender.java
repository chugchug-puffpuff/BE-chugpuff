package chugpuff.chugpuff.entity;

import chugpuff.chugpuff.domain.Member;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Getter
@Setter
@Table(name = "Calender")
@NoArgsConstructor
public class Calender {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "memo_no")
    private Long memoNo;

    @Column(name = "memo_date", nullable = false)
    private String memoDate;

    @Column(name = "memo_content", nullable = false)
    private String memoContent;

    @ManyToOne
    @JoinColumn(name = "user_id", referencedColumnName = "user_id")
    private Member member;

    @ManyToOne
    @JoinColumn(name = "scrap_id", referencedColumnName = "id")
    private Scrap scrap;
}


