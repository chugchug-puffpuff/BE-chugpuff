package chugpuff.chugpuff.repository;

import chugpuff.chugpuff.domain.Member;
import chugpuff.chugpuff.entity.EditSelfIntroduction;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface EditSelfIntroductionRepository extends JpaRepository<EditSelfIntroduction, Long> {
    List<EditSelfIntroduction> findByMember(Member member);
}
