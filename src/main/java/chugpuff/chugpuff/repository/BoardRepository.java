package chugpuff.chugpuff.repository;

import chugpuff.chugpuff.entity.Board;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface BoardRepository extends JpaRepository<Board, Integer> {
    // 카테고리별 게시글 찾기
    List<Board> findByCategory_CategoryId(int categoryId);

    // 좋아요 수로 내림차순 정렬
    List<Board> findAllByOrderByLikesDesc();

    // 게시글 날짜로 내림차순 정렬 (최신순)
    List<Board> findAllByOrderByBoardDateDesc();

    // 댓글 수로 내림차순 정렬
    //LEFT JOIN을 사용하여 Board와 Comment 엔티티를 연결하고, GROUP BY 및 ORDER BY를 사용하여 댓글 수로 내림차순 정렬
    @Query("SELECT b FROM Board b LEFT JOIN b.comments c GROUP BY b.boardNo ORDER BY COUNT(c.bcNo) DESC")
    List<Board> findAllByCommentsCountDesc();

    // 제목이나 내용에 키워드가 포함된 게시글 찾기
    List<Board> findByBoardTitleContainingOrBoardContentContaining(String titleKeyword, String contentKeyword);
}
