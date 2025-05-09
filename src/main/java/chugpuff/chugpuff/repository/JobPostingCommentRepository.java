package chugpuff.chugpuff.repository;

import chugpuff.chugpuff.domain.Member;
import chugpuff.chugpuff.entity.JobPostingComment;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface JobPostingCommentRepository extends JpaRepository<JobPostingComment, Long> {
    List<JobPostingComment> findByJobId(String jobId);

    // 사용자가 작성한 댓글 조회
    List<JobPostingComment> findByMember(Member member);
}
