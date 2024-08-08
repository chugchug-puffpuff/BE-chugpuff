package chugpuff.chugpuff.controller;

import chugpuff.chugpuff.domain.Member;
import chugpuff.chugpuff.entity.EditSelfIntroduction;
import chugpuff.chugpuff.entity.EditSelfIntroductionDetails;
import chugpuff.chugpuff.service.EditSelfIntroductionService;
import chugpuff.chugpuff.service.MemberService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/selfIntroduction")
public class EditSelfIntroductionController {

    private final EditSelfIntroductionService editSelfIntroductionService;
    private final MemberService memberService;

    @Autowired
    public EditSelfIntroductionController(EditSelfIntroductionService editSelfIntroductionService, MemberService memberService) {
        this.editSelfIntroductionService = editSelfIntroductionService;
        this.memberService = memberService;
    }

    // 피드백 제공 및 저장
    @PostMapping("/feedback")
    public ResponseEntity<EditSelfIntroduction> provideFeedback(@AuthenticationPrincipal UserDetails userDetails,
                                                                @RequestBody List<EditSelfIntroductionDetails> details) {
        Member member = memberService.getMemberByUsername(userDetails.getUsername())
                .orElseThrow(() -> new IllegalArgumentException("해당 멤버를 찾을 수 없습니다."));

        EditSelfIntroduction editSelfIntroduction = editSelfIntroductionService.provideFeedbackAndSave(member, details);

        return ResponseEntity.ok(editSelfIntroduction);
    }

    // 자기소개서 조회
    @GetMapping("/list")
    public List<EditSelfIntroduction> getAllSelfIntroductions() {
        return editSelfIntroductionService.getAllSelfIntroductions();
    }
}

