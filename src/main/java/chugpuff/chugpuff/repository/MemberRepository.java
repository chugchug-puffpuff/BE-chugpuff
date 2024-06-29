package chugpuff.chugpuff.repository;

import chugpuff.chugpuff.domain.Member;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MemberRepository extends JpaRepository<Member, Long> {
}
