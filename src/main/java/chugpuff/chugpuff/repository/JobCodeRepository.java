package chugpuff.chugpuff.repository;

import chugpuff.chugpuff.entity.JobCode;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface JobCodeRepository extends JpaRepository<JobCode, Long> {

    List<JobCode> findByJobMidName(String jobMidName);
    JobCode findByJobName(String jobName);
}
