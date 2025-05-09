package chugpuff.chugpuff.repository;

import chugpuff.chugpuff.domain.Member;
import chugpuff.chugpuff.entity.Board;
import chugpuff.chugpuff.entity.Like;
import jakarta.transaction.Transactional;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface LikeRepository extends JpaRepository<Like, Integer> {
    int countByBoard_BoardNo(int boardNo);  //Board 엔티티의 boardNo 필드 참조 => 좋아요 수 계산

    //특정 게시글에 대한 특정 사용자의 좋아요 정보
    Optional<Like> findByBoardAndMember(Board board, Member member);

    @Transactional
    void deleteByBoard_BoardNo(int boardNo);
}
