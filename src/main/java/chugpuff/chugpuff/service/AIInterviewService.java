package chugpuff.chugpuff.service;

import chugpuff.chugpuff.domain.*;
import chugpuff.chugpuff.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class AIInterviewService {

    @Autowired
    private AIInterviewRepository aiInterviewRepository;

    @Autowired
    private AIInterviewIFRepository aiInterviewIFRepository;

    @Autowired
    private AIInterviewFFRepository aiInterviewFFRepository;

    public AIInterview saveInterview(AIInterview aiInterview) {
        return aiInterviewRepository.save(aiInterview);
    }

    public void saveImmediateFeedback(AIInterview aiInterview, String question, String answer, String feedback) {
        AIInterviewIF aiInterviewIF = new AIInterviewIF();
        aiInterviewIF.setAiInterview(aiInterview);
        aiInterviewIF.setI_question(question);
        aiInterviewIF.setI_answer(answer);
        aiInterviewIF.setI_feedback(feedback);
        aiInterviewIFRepository.save(aiInterviewIF);
    }

    public void saveOverallFeedback(AIInterview aiInterview, String question, String answer, String feedback) {
        AIInterviewFF aiInterviewFF = new AIInterviewFF();
        aiInterviewFF.setAiInterview(aiInterview);
        aiInterviewFF.setF_question(question);
        aiInterviewFF.setF_answer(answer);
        aiInterviewFF.setF_feedback(feedback);
        aiInterviewFFRepository.save(aiInterviewFF);
    }

    public AIInterview getInterviewById(Long id) {
        return aiInterviewRepository.findById(id).orElse(null);
    }

    public List<AIInterviewIF> getImmediateFeedbacks(AIInterview aiInterview) {
        return aiInterviewIFRepository.findByAiInterview(aiInterview);
    }
}
