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
    private int bcNo; //댓글 번호
    private int boardNo; //게시글 ID
    private String userId; //유저 ID
    private String bcContent; //댓글 내용
    private LocalDateTime bcDate; //댓글 작성일
    private LocalDateTime bcmodifiedDate; //댓글 수정일
}
