package chugpuff.chugpuff.repository;

import chugpuff.chugpuff.entity.JobPostingComment;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface JobPostingCommentRepository extends JpaRepository<JobPostingComment, Long> {
    List<JobPostingComment> findByJobId(String jobId);
}
