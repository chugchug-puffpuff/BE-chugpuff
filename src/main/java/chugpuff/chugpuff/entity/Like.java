package chugpuff.chugpuff.entity;

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
    private Board board;

    private String userId;

}
