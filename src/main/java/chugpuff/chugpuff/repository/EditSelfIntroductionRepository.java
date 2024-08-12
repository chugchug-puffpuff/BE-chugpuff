package chugpuff.chugpuff.repository;

import chugpuff.chugpuff.domain.Member;
import chugpuff.chugpuff.entity.EditSelfIntroduction;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface EditSelfIntroductionRepository extends JpaRepository<EditSelfIntroduction, Long> {
    //해당 멤버 자소서 피드백 내역 조회
    List<EditSelfIntroduction> findByMember(Member member);

    //save 값 true인 자소서 조회
    Optional<EditSelfIntroduction> findByMemberAndSaveTrue(Member member);
}
