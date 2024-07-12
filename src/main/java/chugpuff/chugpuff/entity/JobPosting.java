package chugpuff.chugpuff.entity;

import jakarta.persistence.*;

@Entity
public class JobPosting {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long j_id; // 데이터베이스에서 자동으로 증가하는 기본 키

    @Column(unique = true) // 고유 제약 조건 설정
    private String jobNo; //사람인 API의 공고 번호
    private int scraps;

    // Getters and setters
    public Long getJ_id() {
        return j_id;
    }

    public void setJ_id(Long j_id) {
        this.j_id = j_id;
    }

    public String getJobNo() {
        return jobNo;
    }

    public void setJobNo(String jobNo) {
        this.jobNo = jobNo;
    }

    public int getScraps() {
        return scraps;
    }

    public void setScraps(int scraps) {
        this.scraps = scraps;
    }
}
