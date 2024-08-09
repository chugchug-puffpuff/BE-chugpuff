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
                .eS_feedback("")
                .revisedSelfIntroduction("")
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
        String feedbackAndRevised = chatGPTService.extractChatGPTFeedback(response);
        log.info("피드백과 수정된 자기소개서 추출: {}", feedbackAndRevised);

        String[] parts = feedbackAndRevised.split("\n\n", 2);
        String feedback = parts.length > 0 ? parts[0] : "";
        String revisedSelfIntroduction = parts.length > 1 ? parts[1] : "";

        log.info("피드백: {}", feedback);
        log.info("수정된 자기소개서: {}", revisedSelfIntroduction);

        savedSelfIntroduction.setES_feedback(feedback);
        savedSelfIntroduction.setRevisedSelfIntroduction(revisedSelfIntroduction);
        savedSelfIntroduction.setES_date(LocalDate.now());

        return editSelfIntroductionRepository.save(savedSelfIntroduction);

    }

    public List<EditSelfIntroduction> getAllSelfIntroductions() {
        log.info("모든 자기소개서 조회 시작");
        List<EditSelfIntroduction> introductions = editSelfIntroductionRepository.findAll();
        log.info("모든 자기소개서 조회 완료: {}", introductions);
        return introductions;
    }


    public void checkFeedbackFlow(EditSelfIntroduction esi) {
        log.info("Checklist: 피드백 플로우 체크 시작");

        // 1. 사용자 식별 및 저장된 자기소개서 정보 로드
        if (esi.getMember() != null && esi.getMember().getUser_id() != null) {
            log.info("Checklist: 사용자 식별 성공 - User ID: " + esi.getMember().getUser_id());
        } else {
            log.error("Checklist: 사용자 식별 실패 - Member 객체 또는 User ID 없음");
        }

        // 2. 자기소개서 질문 및 답변 유효성 확인
        List<EditSelfIntroductionDetails> details = esi.getDetails();
        if (details != null && !details.isEmpty()) {
            log.info("Checklist: 자기소개서 질문 및 답변 유효성 확인 - " + details.size() + "개의 질문/답변 확인");
            for (EditSelfIntroductionDetails detail : details) {
                if (detail.getES_question() == null || detail.getES_question().isEmpty()) {
                    log.warn("Checklist: 질문이 비어 있음 - eSD_no: " + detail.getESD_no());
                }
                if (detail.getES_answer() == null || detail.getES_answer().isEmpty()) {
                    log.warn("Checklist: 답변이 비어 있음 - eSD_no: " + detail.getESD_no());
                }
            }
        } else {
            log.warn("Checklist: 자기소개서 질문 및 답변이 비어 있음");
        }

        // 3. ChatGPT 서비스 호출 및 응답 유효성 확인
        log.info("Checklist: ChatGPT 서비스 호출 시작");
        String gptResponse = chatGPTService.callChatGPTForFeedback(details); // 가정: ChatGPT 호출 메서드
        if (gptResponse == null || gptResponse.isEmpty()) {
            log.error("Checklist: ChatGPT 응답 없음");
        } else {
            log.info("Checklist: ChatGPT 응답 수신 - 응답 내용: " + gptResponse);
        }

        // 4. 피드백 및 수정된 자기소개서 저장 유효성 확인
        if (esi.getES_feedback() != null && !esi.getES_feedback().isEmpty()) {
            log.info("Checklist: 피드백 저장 성공 - Feedback: " + esi.getES_feedback());
        } else {
            log.error("Checklist: 피드백 저장 실패 - Feedback 비어 있음");
        }

        if (esi.getRevisedSelfIntroduction() != null && !esi.getRevisedSelfIntroduction().isEmpty()) {
            log.info("Checklist: 수정된 자기소개서 저장 성공 - 수정본: " + esi.getRevisedSelfIntroduction());
        } else {
            log.error("Checklist: 수정된 자기소개서 저장 실패 - 수정본 비어 있음");
        }

        log.info("Checklist: 피드백 플로우 체크 완료");
    }

}
