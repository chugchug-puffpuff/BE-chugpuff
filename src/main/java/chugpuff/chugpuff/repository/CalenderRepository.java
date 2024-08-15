package chugpuff.chugpuff.repository;

import chugpuff.chugpuff.domain.Member;
import chugpuff.chugpuff.entity.Calender;
import chugpuff.chugpuff.entity.Scrap;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CalenderRepository extends JpaRepository<Calender, Long> {
    List<Calender> findByMember(Member member);

    // Scrap을 기준으로 캘린더 항목을 찾는 메서드 추가
    List<Calender> findByScrap(Scrap scrap);
}
