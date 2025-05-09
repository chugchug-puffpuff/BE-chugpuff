package chugpuff.chugpuff.entity;

import chugpuff.chugpuff.domain.Member;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Getter
@Setter
@Table(name = "board_likes")
public class Like {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int likesNo; //좋아요 번호

    @ManyToOne
    @JoinColumn(name = "board_no")
    private Board board; //게시글과의 관계, 외래키로 board_no 사용

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private Member member; // 유저와의 관계, 외래키로 user_id 사용

    //기본 생성자
    public Like() {}

    // 팔드 초기화를 위한 생성자
    public Like(Board board, Member member) {
        this.board = board;
        this.member = member;
    }

}
