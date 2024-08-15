package chugpuff.chugpuff.repository;
import chugpuff.chugpuff.entity.EditSelfIntroduction;
import chugpuff.chugpuff.entity.EditSelfIntroductionDetails;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface EditSelfIntroductionDetailsRepository extends JpaRepository<EditSelfIntroductionDetails, Long> {
    //자기소개서 번호에 맞는 문항과 대답 조회
    List<EditSelfIntroductionDetails> findByEditSelfIntroduction(EditSelfIntroduction editSelfIntroduction);
}

