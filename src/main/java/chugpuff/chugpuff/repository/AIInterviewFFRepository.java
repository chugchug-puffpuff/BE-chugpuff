package chugpuff.chugpuff.repository;

import chugpuff.chugpuff.domain.AIInterviewFF;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface AIInterviewFFRepository extends JpaRepository<AIInterviewFF, Long> {
}
