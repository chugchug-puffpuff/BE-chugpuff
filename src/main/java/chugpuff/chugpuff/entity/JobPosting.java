package chugpuff.chugpuff.entity;

import jakarta.persistence.*;

@Entity
public class JobPosting {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long j_id; // 데이터베이스에서 자동으로 증가하는 기본 키

    private String jobId; //사람인 API의 공고 번호

    // Getters and setters
    public Long getJ_id() {
        return j_id;
    }

    public void setJ_id(Long j_id) {
        this.j_id = j_id;
    }

    public String getJobId() {
        return jobId;
    }

    public void setJobId(String jobId) {
        this.jobId = jobId;
    }
}
