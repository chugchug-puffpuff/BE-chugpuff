package chugpuff.chugpuff.repository;

import chugpuff.chugpuff.domain.Member;
import chugpuff.chugpuff.entity.Board;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface BoardRepository extends JpaRepository<Board, Integer> {
    // 카테고리별 게시글 찾기 (카테고리만 있는 경우)
    List<Board> findByCategory_CategoryId(int categoryId);

    // 좋아요 수로 내림차순 정렬
    List<Board> findAllByOrderByLikesDesc();

    // 게시글 날짜로 내림차순 정렬 (최신순)
    List<Board> findAllByOrderByBoardDateDesc();

    // 댓글 수로 내림차순 정렬
    //LEFT JOIN을 사용하여 Board와 Comment 엔티티를 연결하고, GROUP BY 및 ORDER BY를 사용하여 댓글 수로 내림차순 정렬
    @Query("SELECT b FROM Board b LEFT JOIN b.comments c GROUP BY b.boardNo ORDER BY COUNT(c.bcNo) DESC")
    List<Board> findAllByCommentsCountDesc();

    // 제목이나 내용에 키워드가 포함된 게시글 찾기 (키워드만 있는 경우)
    List<Board> findByBoardTitleContainingOrBoardContentContaining(String titleKeyword, String contentKeyword);
    // 카테고리에서 키워드 포함된 게시글 찾기 (둘 다 일치하는 경우)
    List<Board> findByCategory_CategoryIdAndBoardTitleContainingOrCategory_CategoryIdAndBoardContentContaining(int categoryId, String titleKeyword, int categoryIdAgain, String contentKeyword);

    // 회원별 작성한 게시글 조회
    List<Board> findByMember_Id(String memberId);

    // 특정 사용자가 좋아요한 게시글 조회
    @Query("SELECT b FROM Board b JOIN Like l ON b.boardNo = l.board.boardNo WHERE l.member = :member")
    List<Board> findByMemberLikes(@Param("member") Member member);
}
