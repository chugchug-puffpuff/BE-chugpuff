package chugpuff.chugpuff.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Getter
@Setter
public class Like {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int likesNo; //좋아요 번호
    private int boardNo; //좋아요 누른 게시글 번호 참조하는 외래키
    private String userId; //좋아요 누른 사용자의 ID
}
