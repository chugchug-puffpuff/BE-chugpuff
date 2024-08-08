package chugpuff.chugpuff.service;

import chugpuff.chugpuff.domain.Member;
import chugpuff.chugpuff.entity.EditSelfIntroduction;
import chugpuff.chugpuff.entity.EditSelfIntroductionDetails;
import chugpuff.chugpuff.repository.EditSelfIntroductionDetailsRepository;
import chugpuff.chugpuff.repository.EditSelfIntroductionRepository;
import com.fasterxml.jackson.databind.JsonNode;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

@Service
@Transactional
public class EditSelfIntroductionService {

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
        EditSelfIntroduction editSelfIntroduction = EditSelfIntroduction.builder()
                .member(member)
                .eS_date(LocalDate.now())
                .eS_feedback("")
                .revisedSelfIntroduction("")
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

        String response = chatGPTService.callChatGPTForFeedback(details);
        String feedbackAndRevised = chatGPTService.extractChatGPTFeedback(response).toString();

        // 피드백과 수정된 자기소개서를 분리
        String[] parts = feedbackAndRevised.split("\n\n", 2);
        String feedback = parts.length > 0 ? parts[0] : "";
        String revisedSelfIntroduction = parts.length > 1 ? parts[1] : "";

        savedSelfIntroduction.setES_feedback(feedback);
        savedSelfIntroduction.setRevisedSelfIntroduction(revisedSelfIntroduction);
        savedSelfIntroduction.setES_date(LocalDate.now());

        return editSelfIntroductionRepository.save(savedSelfIntroduction);
    }

    public List<EditSelfIntroduction> getAllSelfIntroductions() {
        return editSelfIntroductionRepository.findAll();
    }
}
