package chugpuff.chugpuff.repository;

import chugpuff.chugpuff.entity.Board;
import chugpuff.chugpuff.entity.Like;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface LikeRepository extends JpaRepository<Like, Integer> {
    int countByBoard_BoardNo(int boardNo);  //Board 엔티티의 boardNo 필드 참조
    Optional<Like> findByBoardAndUserId(Board board, String userId);
}
