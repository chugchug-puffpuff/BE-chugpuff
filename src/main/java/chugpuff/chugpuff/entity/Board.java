package chugpuff.chugpuff.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.List;

@Entity
@Getter
@Setter
public class Board {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int boardNo; //게시글 번호

    @ManyToOne
    @JoinColumn(name = "user_id") // User 엔티티의 기본키를 외래키로 설정
    private User user; // 작성자

    private String boardTitle; //게시글 제목

    @Column(columnDefinition = "TEXT")
    private String boardContent; //게시글 내용

    private LocalDateTime boardDate; //게시글 작성일
    private LocalDateTime boardmodifiedDate; //게시글 수정일
    private int likes; //게시글 좋아요 수

    @ManyToOne
    @JoinColumn(name = "category_id")
    private Category category; //카테고리 ID 외래키

    @Transient
    private String categoryName;

    @OneToMany(mappedBy = "board", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Comment> comments;

}
