package chugpuff.chugpuff.service;

import chugpuff.chugpuff.domain.Member;
import chugpuff.chugpuff.entity.EditSelfIntroduction;
import chugpuff.chugpuff.entity.EditSelfIntroductionDetails;
import chugpuff.chugpuff.repository.EditSelfIntroductionDetailsRepository;
import chugpuff.chugpuff.repository.EditSelfIntroductionRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class EditSelfIntroductionService {

    private static final Logger log = LoggerFactory.getLogger(EditSelfIntroductionService.class);

    private final EditSelfIntroductionRepository editSelfIntroductionRepository;
    private final EditSelfIntroductionDetailsRepository editSelfIntroductionDetailsRepository;
    private final ChatGPTService chatGPTService;
    private final MemberService memberService;

    @Autowired
    public EditSelfIntroductionService(EditSelfIntroductionRepository editSelfIntroductionRepository,
                                       EditSelfIntroductionDetailsRepository editSelfIntroductionDetailsRepository,
                                       ChatGPTService chatGPTService,
                                       MemberService memberService) {
        this.editSelfIntroductionRepository = editSelfIntroductionRepository;
        this.editSelfIntroductionDetailsRepository = editSelfIntroductionDetailsRepository;
        this.chatGPTService = chatGPTService;
        this.memberService = memberService;
    }

    public EditSelfIntroduction provideFeedbackAndSave(Member member, List<EditSelfIntroductionDetails> details) {
        log.info("피드백 제공 및 저장 시작");

        // details가 null이면 빈 리스트로 초기화
        if (details == null) {
            log.warn("provideFeedbackAndSave: 전달된 details 리스트가 null입니다. 빈 리스트로 초기화합니다.");
            details = new ArrayList<>();
        }

        // EditSelfIntroduction 객체 생성
        EditSelfIntroduction editSelfIntroduction = EditSelfIntroduction.builder()
                .member(member)
                .eS_date(LocalDate.now())
                .eS_feedback("")  // 초기 피드백 필드
                .build();

        EditSelfIntroduction savedSelfIntroduction = editSelfIntroductionRepository.save(editSelfIntroduction);

        for (EditSelfIntroductionDetails detail : details) {

            if (detail.getES_question() == null) {
                detail.setES_question("");
            }
            if (detail.getES_answer() == null) {
                detail.setES_answer("");
            }

            detail.setEditSelfIntroduction(savedSelfIntroduction);
            detail.setMember(member);

            editSelfIntroductionDetailsRepository.save(detail);
        }

        // GPT 호출 및 피드백 처리
        log.info("ChatGPT 호출 시작");
        String response = chatGPTService.callChatGPTForFeedback(details);
        String feedback = chatGPTService.extractChatGPTFeedback(response);
        log.info("피드백 추출: {}", feedback);

        savedSelfIntroduction.setES_feedback(feedback);
        savedSelfIntroduction.setES_date(LocalDate.now());

        return editSelfIntroductionRepository.save(savedSelfIntroduction);
    }

    //모든 첨삭된 자소서 조회
    public List<EditSelfIntroduction> getSelfIntroductionsByMember(Member member) {
        log.info("특정 멤버의 자기소개서 조회 시작: {}", member.getName());
        List<EditSelfIntroduction> introductions = editSelfIntroductionRepository.findByMember(member);
        return introductions;
    }


    //특정 첨삭된 자소서 조회
    public Optional<EditSelfIntroduction> getSelfIntroductionById(Long id) {
        return editSelfIntroductionRepository.findById(id);
    }

    //특정 첨삭된 자소서 삭제
    public void deleteSelfIntroductionById(Long id) {
        editSelfIntroductionRepository.deleteById(id);
    }

    //특정 자소서 저장
    public void saveSelectedSelfIntroduction(Long selfIntroductionId, String username) {
        // 현재 로그인된 사용자 가져오기
        Member member = memberService.getMemberByUsername(username)
                .orElseThrow(() -> new IllegalArgumentException("해당 멤버를 찾을 수 없습니다."));

        // 현재 save 값이 true인 자소서 false로 변경
        editSelfIntroductionRepository.findByMemberAndSaveTrue(member)
                .ifPresent(existingIntro -> {
                    existingIntro.setSave(false);
                    editSelfIntroductionRepository.save(existingIntro);
                });

        // 선택된 자소서의 save 값을 true로 변경
        EditSelfIntroduction selectedIntro = editSelfIntroductionRepository.findById(selfIntroductionId)
                .orElseThrow(() -> new IllegalArgumentException("해당 자기소개서를 찾을 수 없습니다."));

        selectedIntro.setSave(true);
        editSelfIntroductionRepository.save(selectedIntro);
    }
}
