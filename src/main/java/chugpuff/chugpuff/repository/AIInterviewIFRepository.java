package chugpuff.chugpuff.repository;

import chugpuff.chugpuff.domain.AIInterview;
import chugpuff.chugpuff.domain.AIInterviewIF;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AIInterviewIFRepository extends JpaRepository<AIInterviewIF, Long> {
    List<AIInterviewIF> findByAiInterview(AIInterview aiInterview);
}
