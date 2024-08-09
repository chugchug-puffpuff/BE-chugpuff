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

@Service
@Transactional
public class EditSelfIntroductionService {

    private static final Logger log = LoggerFactory.getLogger(EditSelfIntroductionService.class);

    private final EditSelfIntroductionRepository editSelfIntroductionRepository;
    private final EditSelfIntroductionDetailsRepository editSelfIntroductionDetailsRepository;
    private final ChatGPTService chatGPTService;

    @Autowired
    public EditSelfIntroductionService(EditSelfIntroductionRepository editSelfIntroductionRepository,
                                       EditSelfIntroductionDetailsRepository editSelfIntroductionDetailsRepository,
                                       ChatGPTService chatGPTService) {
        this.editSelfIntroductionRepository = editSelfIntroductionRepository;
        this.editSelfIntroductionDetailsRepository = editSelfIntroductionDetailsRepository;
        this.chatGPTService = chatGPTService;
    }

    public EditSelfIntroduction provideFeedbackAndSave(Member member, List<EditSelfIntroductionDetails> details) {
        log.info("피드백 제공 및 저장 시작");

        // details가 null이면 빈 리스트로 초기화
        if (details == null) {
            log.warn("provideFeedbackAndSave: 전달된 details 리스트가 null입니다. 빈 리스트로 초기화합니다.");
            details = new ArrayList<>();
        }

        // 디버깅: 전달된 details 리스트의 내용을 로그로 출력
        log.info("Received details: {}", details);

        // EditSelfIntroduction 객체 생성
        EditSelfIntroduction editSelfIntroduction = EditSelfIntroduction.builder()
                .member(member)
                .eS_date(LocalDate.now())
                .eS_feedback("")  // 초기 피드백 필드
                .build();

        EditSelfIntroduction savedSelfIntroduction = editSelfIntroductionRepository.save(editSelfIntroduction);

        for (EditSelfIntroductionDetails detail : details) {
            log.info("Before saving: Question: {}, Answer: {}", detail.getES_question(), detail.getES_answer());

            if (detail.getES_question() == null) {
                detail.setES_question("");
            }
            if (detail.getES_answer() == null) {
                detail.setES_answer("");
            }

            detail.setEditSelfIntroduction(savedSelfIntroduction);
            detail.setMember(member);

            editSelfIntroductionDetailsRepository.save(detail);

            log.info("Saved detail: {}", detail);
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

    public List<EditSelfIntroduction> getAllSelfIntroductions() {
        log.info("모든 자기소개서 조회 시작");
        List<EditSelfIntroduction> introductions = editSelfIntroductionRepository.findAll();
        log.info("모든 자기소개서 조회 완료: {}", introductions);
        return introductions;
    }
}
