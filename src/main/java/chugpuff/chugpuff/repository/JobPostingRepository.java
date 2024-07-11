package chugpuff.chugpuff.repository;

import chugpuff.chugpuff.entity.JobPosting;
import org.springframework.data.jpa.repository.JpaRepository;

public interface JobPostingRepository extends JpaRepository<JobPosting, Long> {
}
