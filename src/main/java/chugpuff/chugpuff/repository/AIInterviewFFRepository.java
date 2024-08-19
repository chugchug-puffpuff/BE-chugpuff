package chugpuff.chugpuff.repository;

import chugpuff.chugpuff.domain.AIInterviewFF;
import chugpuff.chugpuff.domain.AIInterview;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AIInterviewFFRepository extends JpaRepository<AIInterviewFF, Long> {

    List<AIInterviewFF> findByAiInterviewFF(AIInterview aiInterview);
}
