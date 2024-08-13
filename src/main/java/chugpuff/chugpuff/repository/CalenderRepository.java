package chugpuff.chugpuff.repository;

import chugpuff.chugpuff.domain.Member;
import chugpuff.chugpuff.entity.Calender;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CalenderRepository extends JpaRepository<Calender, Long> {
    List<Calender> findByMember(Member member);
}
