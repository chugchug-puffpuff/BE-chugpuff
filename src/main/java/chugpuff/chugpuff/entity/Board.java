package chugpuff.chugpuff.entity;

import chugpuff.chugpuff.domain.Member;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
@Setter
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
public class Board {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int boardNo; //게시글 번호

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false) // Member 엔티티의 기본키를 외래키로 설정
    @JsonIgnore
    private Member member; // 작성자

    private String boardTitle; //게시글 제목

    @Column(columnDefinition = "TEXT")
    private String boardContent; //게시글 내용

    private LocalDateTime boardDate; //게시글 작성일
    private LocalDateTime boardModifiedDate; //게시글 수정일
    private int likes; //게시글 좋아요 수

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id", nullable = false)
    private Category category; //카테고리 ID 외래키


    @OneToMany(mappedBy = "board", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonIgnore
    private List<Comment> comments = new ArrayList<>();


}
