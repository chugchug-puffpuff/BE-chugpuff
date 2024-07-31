package chugpuff.chugpuff.repository;

import chugpuff.chugpuff.domain.Member;
import chugpuff.chugpuff.entity.Scrap;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ScrapRepository extends JpaRepository<Scrap, Long> {
    Optional<Scrap> findByMemberAndJobId(Member member, String jobId);
    List<Scrap> findByMember(Member member);
    @Query("SELECT COUNT(s) FROM Scrap s WHERE s.jobId = :jobId")
    Long countByJobId(String jobId);
    @Query("SELECT s.jobId, COUNT(s) AS scrapCount FROM Scrap s GROUP BY s.jobId ORDER BY scrapCount DESC")
    List<Object[]> findJobIdsOrderByScrapCount();
}