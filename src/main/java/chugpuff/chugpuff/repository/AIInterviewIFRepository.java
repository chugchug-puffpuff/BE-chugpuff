package chugpuff.chugpuff.repository;

import chugpuff.chugpuff.domain.AIInterviewIF;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface AIInterviewIFRepository extends JpaRepository<AIInterviewIF, Long> {
}
