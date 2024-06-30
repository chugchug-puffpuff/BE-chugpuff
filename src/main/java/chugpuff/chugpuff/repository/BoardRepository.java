package chugpuff.chugpuff.repository;

import chugpuff.chugpuff.entity.Board;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface BoardRepository extends JpaRepository<Board, Integer> {
    List<Board> findByCategory_CategoryId(int categoryId);

    // 좋아요 수 기준 정렬
    @Query("SELECT b FROM Board b ORDER BY b.likes DESC")
    List<Board> findAllByLikesDesc();
    // 게시글 작성일 기준 정렬
    @Query("SELECT b FROM Board b ORDER BY b.boardDate DESC")
    List<Board> findAllByBoardDateDesc();
    // 댓글 수 기준 정렬
    @Query("SELECT b FROM Board b LEFT JOIN Comment c ON b.boardNo = c.boardNo GROUP BY b.boardNo ORDER BY COUNT(c.bcNo) DESC")
    List<Board> findAllByCommentsCountDesc();
}