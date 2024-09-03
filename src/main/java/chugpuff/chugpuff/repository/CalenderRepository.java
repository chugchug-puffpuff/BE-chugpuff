package chugpuff.chugpuff.repository;

import chugpuff.chugpuff.domain.Member;
import chugpuff.chugpuff.entity.Calender;
import chugpuff.chugpuff.entity.Scrap;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;

public interface CalenderRepository extends JpaRepository<Calender, Long> {
    List<Calender> findByMember(Member member);

    // Scrap을 기준으로 캘린더 항목 찾기
    List<Calender> findByScrap(Scrap scrap);

    //마감기한 D-1 스크랩 공고 조회
    List<Calender> findByMemberAndMemoDate(Member member, String memoDate);
}