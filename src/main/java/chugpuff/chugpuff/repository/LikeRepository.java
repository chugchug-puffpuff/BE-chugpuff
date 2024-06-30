package chugpuff.chugpuff.repository;

import chugpuff.chugpuff.entity.Like;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface LikeRepository extends JpaRepository<Like, Integer> {
    int countByBoardNo(int boardNo);
    Optional<Like> findByBoardNoAndUserId(int boardNo, String userId);
}
