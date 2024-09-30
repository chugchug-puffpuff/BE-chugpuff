package chugpuff.chugpuff.repository;

import chugpuff.chugpuff.domain.AIInterviewFFB;
import chugpuff.chugpuff.domain.AIInterview;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AIInterviewFFBRepository extends JpaRepository<AIInterviewFFB, Long> {
    List<AIInterviewFFB> findByAiInterview(AIInterview aiInterview);
}
