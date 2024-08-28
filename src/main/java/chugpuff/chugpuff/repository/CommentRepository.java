package chugpuff.chugpuff.repository;


import chugpuff.chugpuff.entity.Comment;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CommentRepository extends JpaRepository<Comment, Integer> {
    // 회원별 작성한 댓글 조회
    List<Comment> findByMember_Id(String memberId);

    //게시글 삭제 시 댓글 삭제
    void deleteByBoard_BoardNo(int boardNo);

    // 게시글 번호로 댓글 조회
    List<Comment> findByBoard_BoardNo(int boardNo);
}