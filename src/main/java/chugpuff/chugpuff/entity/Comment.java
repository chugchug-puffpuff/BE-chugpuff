package chugpuff.chugpuff.entity;

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
    private Board board; // 게시글 번호 외래키로 매핑

    private String userId; //유저 ID (댓글 작성자)

    @Column(columnDefinition = "TEXT")
    private String bcContent; //댓글 내용

    private LocalDateTime bcDate; //댓글 작성일
    private LocalDateTime bcmodifiedDate; //댓글 수정일
}
