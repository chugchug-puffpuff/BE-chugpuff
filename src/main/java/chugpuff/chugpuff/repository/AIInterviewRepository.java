package chugpuff.chugpuff.repository;

import chugpuff.chugpuff.domain.AIInterview;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface AIInterviewRepository extends JpaRepository<AIInterview, Long> {
}
