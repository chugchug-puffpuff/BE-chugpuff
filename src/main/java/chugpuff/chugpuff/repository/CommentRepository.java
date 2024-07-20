package chugpuff.chugpuff.repository;


import chugpuff.chugpuff.entity.Comment;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CommentRepository extends JpaRepository<Comment, Integer> {
}