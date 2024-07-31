package chugpuff.chugpuff.entity;

import chugpuff.chugpuff.domain.Member;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Getter
@Setter
public class Comment {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int bcNo; //댓글 번호, 자동 생성되는 기본키

    @ManyToOne
    @JoinColumn(name = "board_no")
    @JsonIgnore
    private Board board; // 게시글 번호 외래키로 매핑

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private Member member; // 유저와의 관계, 외래키로 user_id 사용

    @Column(columnDefinition = "TEXT")
    private String bcContent; //댓글 내용

    private LocalDateTime bcDate; //댓글 작성일
    private LocalDateTime bcModifiedDate; //댓글 수정일
}
