package chugpuff.chugpuff.controller;

import chugpuff.chugpuff.domain.Member;
import chugpuff.chugpuff.entity.EditSelfIntroduction;
import chugpuff.chugpuff.entity.EditSelfIntroductionDetails;
import chugpuff.chugpuff.repository.EditSelfIntroductionRepository;
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
    private final EditSelfIntroductionRepository editSelfIntroductionRepository;

    @Autowired
    public EditSelfIntroductionController(EditSelfIntroductionService editSelfIntroductionService,
                                          MemberService memberService,
                                          EditSelfIntroductionRepository editSelfIntroductionRepository) {
        this.editSelfIntroductionService = editSelfIntroductionService;
        this.memberService = memberService;
        this.editSelfIntroductionRepository = editSelfIntroductionRepository;
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

    //모든 첨삭된 자기소개서 조회
    @GetMapping("/list")
    public List<EditSelfIntroduction> getSelfIntroductionsByMember(@AuthenticationPrincipal UserDetails userDetails) {
        // 현재 인증된 사용자의 username을 가져옴
        String username = userDetails.getUsername();

        // username을 이용하여 Member 조회
        Member member = memberService.getMemberByUsername(username)
                .orElseThrow(() -> new IllegalArgumentException("해당 멤버를 찾을 수 없습니다."));

        // 해당 멤버의 자기소개서 목록 반환
        return editSelfIntroductionService.getSelfIntroductionsByMember(member);
    }

    //특정 첨삭된 자기소개서 조회
    @GetMapping("/{id}")
    public ResponseEntity<EditSelfIntroduction> getSelfIntroductionById(@PathVariable Long id) {
        EditSelfIntroduction editSelfIntroduction = editSelfIntroductionService.getSelfIntroductionById(id)
                .orElseThrow(() -> new IllegalArgumentException("해당 자기소개서를 찾을 수 없습니다."));
        return ResponseEntity.ok(editSelfIntroduction);
    }

    // 특정 첨삭된 자기소개서 삭제
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteSelfIntroduction(@PathVariable Long id) {
        editSelfIntroductionService.deleteSelfIntroductionById(id);
        return ResponseEntity.noContent().build();
    }

    //특정 자소서 저장
    @PostMapping("/save/{id}")
    public ResponseEntity<String> saveSelectedSelfIntroduction(
            @PathVariable Long id,
            @AuthenticationPrincipal UserDetails userDetails) {
        String username = userDetails.getUsername();
        editSelfIntroductionService.saveSelectedSelfIntroduction(id, username);
        return ResponseEntity.ok("자기소개서가 저장되었습니다.");
    }

    //저장된 자소서 조회
    @GetMapping("/saved")
    public ResponseEntity<EditSelfIntroduction> getSavedSelfIntroduction(
            @AuthenticationPrincipal UserDetails userDetails) {
        String username = userDetails.getUsername();
        Member member = memberService.getMemberByUsername(username)
                .orElseThrow(() -> new IllegalArgumentException("해당 멤버를 찾을 수 없습니다."));

        EditSelfIntroduction savedIntro = editSelfIntroductionRepository.findByMemberAndSaveTrue(member)
                .orElseThrow(() -> new IllegalArgumentException("저장된 자기소개서가 없습니다."));

        return ResponseEntity.ok(savedIntro);
    }
}

