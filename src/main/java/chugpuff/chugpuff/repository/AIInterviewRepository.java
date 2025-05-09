package chugpuff.chugpuff.repository;

import chugpuff.chugpuff.domain.AIInterview;
import chugpuff.chugpuff.domain.Member;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface AIInterviewRepository extends JpaRepository<AIInterview, Long> {
    List<AIInterview> findByMemberId(String id);
}
