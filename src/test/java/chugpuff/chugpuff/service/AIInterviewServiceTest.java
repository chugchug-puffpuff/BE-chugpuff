package chugpuff.chugpuff.service;

import chugpuff.chugpuff.domain.*;
import chugpuff.chugpuff.entity.EditSelfIntroduction;
import chugpuff.chugpuff.entity.EditSelfIntroductionDetails;
import chugpuff.chugpuff.repository.*;
import javazoom.jl.player.Player;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.Spy;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class AIInterviewServiceTest {

    @InjectMocks
    @Spy
    private AIInterviewService aiInterviewService;

    @Mock
    private AIInterviewRepository aiInterviewRepository;

    @Mock
    private EditSelfIntroductionRepository editSelfIntroductionRepository;

    @Mock
    private EditSelfIntroductionDetailsRepository editSelfIntroductionDetailsRepository;

    @Mock
    private AIInterviewIFRepository aiInterviewIFRepository;

    @Mock
    private AIInterviewFFRepository aiInterviewFFRepository;

    @Mock
    private MemberService memberService;

    @Mock
    private ExternalAPIService externalAPIService;

    @Mock
    private TimerService timerService;

    @Mock
    private Player mockPlayer;

    private UserDetails userDetails;
    private Member member;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        userDetails = createTestUserDetails();
        member = createTestMember();


        ReflectionTestUtils.setField(aiInterviewService, "interviewInProgress", true);
    }

    private UserDetails createTestUserDetails() {
        return User.builder()
                .username("testUser")
                .password("password")
                .roles("USER")
                .build();
    }

    private Member createTestMember() {
        Member member = new Member();
        member.setUser_id(1L);
        member.setName("Test User");
        return member;
    }

    @Test
    void testGetSelfIntroductionContentForInterview_Success() {
        // Given
        EditSelfIntroduction selfIntroduction = new EditSelfIntroduction();
        selfIntroduction.setSave(true);

        EditSelfIntroductionDetails detail = new EditSelfIntroductionDetails();
        detail.setES_question("당신의 강점은 무엇입니까?");
        detail.setES_answer("저는 매우 성실합니다.");

        when(editSelfIntroductionRepository.findByMember(member)).thenReturn(Collections.singletonList(selfIntroduction));
        when(editSelfIntroductionDetailsRepository.findByEditSelfIntroduction(selfIntroduction))
                .thenReturn(Collections.singletonList(detail));

        // When
        String result = aiInterviewService.getSelfIntroductionContentForInterview(member);

        // Then
        assertNotNull(result);
        assertTrue(result.contains("당신의 강점은 무엇입니까?"));
        assertTrue(result.contains("저는 매우 성실합니다."));
    }

    @Test
    void testStartInterview_Success() {
        // Given
        AIInterview interview = new AIInterview();
        interview.setInterviewType("인성 면접");
        interview.setMember(member);

        when(externalAPIService.callChatGPT(anyString())).thenReturn("질문: 갈등을 어떻게 해결합니까?");
        when(externalAPIService.callTTS(anyString())).thenReturn("validAudioUrl.mp3");

        doNothing().when(timerService).startTimer(anyLong(), any());

        // When
        String result = aiInterviewService.startInterview(interview);

        // Then
        assertNotNull(result);
        assertEquals("갈등을 어떻게 해결합니까?", result.replace("질문: ", ""));
    }

    @Test
    void testInitializeInterviewSession_ForSelfIntroductionInterview() {
        // Given
        AIInterview aiInterview = new AIInterview();
        aiInterview.setInterviewType("자기소개서 면접");

        EditSelfIntroduction selfIntroduction = new EditSelfIntroduction();
        selfIntroduction.setSave(true);

        EditSelfIntroductionDetails detail = new EditSelfIntroductionDetails();
        detail.setES_question("당신의 강점은 무엇입니까?");
        detail.setES_answer("저는 매우 성실합니다.");

        when(editSelfIntroductionRepository.findByMember(member)).thenReturn(Collections.singletonList(selfIntroduction));
        when(editSelfIntroductionDetailsRepository.findByEditSelfIntroduction(selfIntroduction))
                .thenReturn(Collections.singletonList(detail));

        when(externalAPIService.callChatGPT(anyString())).thenReturn("질문: 자기소개서 기반의 질문입니다.");

        // When
        String result = aiInterviewService.startInterview(aiInterview);

        // Then
        assertNotNull(result);
        assertEquals("자기소개서 기반의 질문입니다.", result.replace("질문: ", ""));
    }

    @Test
    void testGenerateFeedback_Success() {
        // Given
        AIInterview aiInterview = new AIInterview();
        aiInterview.setFeedbackType("즉시 피드백");
        aiInterview.setInterviewType("직무 면접");
        aiInterview.setMember(member);

        String userResponse = "저는 팀 내에서 갈등이 발생했을 때, 논리적으로 해결합니다.";
        String expectedFeedback = "피드백: 좋은 답변입니다.";

        when(externalAPIService.callChatGPT(anyString())).thenReturn(expectedFeedback);
        when(externalAPIService.callTTS(anyString())).thenReturn("validAudioUrl.mp3");

        // When
        Map<String, String> feedbackMap = aiInterviewService.generateFeedback(aiInterview, userResponse);

        // Then
        assertEquals(expectedFeedback, feedbackMap.get("feedback"));
        verify(aiInterviewIFRepository, times(1)).save(any(AIInterviewIF.class));
    }

    @Test
    void testGetInterviewSummary_Success() {
        // Given
        AIInterview aiInterview = new AIInterview();
        aiInterview.setFeedbackType("즉시 피드백");

        AIInterviewIF feedback = new AIInterviewIF();
        feedback.setI_question("질문");
        feedback.setI_answer("답변");

        when(aiInterviewIFRepository.findByAiInterview(any(AIInterview.class)))
                .thenReturn(Collections.singletonList(feedback));

        // When
        Map<String, Object> result = aiInterviewService.getInterviewSummary(aiInterview);

        // Then
        List<Map<String, String>> qaList = (List<Map<String, String>>) result.get("questionsAndAnswers");
        assertEquals(1, qaList.size());
        assertEquals("질문", qaList.get(0).get("question"));
        assertEquals("답변", qaList.get(0).get("answer"));
    }

    @Test
    void testEndInterview_Processes() {
        // Given
        AIInterview aiInterview = new AIInterview();
        aiInterview.setFeedbackType("전체 피드백");

        ReflectionTestUtils.setField(aiInterviewService, "interviewInProgress", true);

        // When
        aiInterviewService.endInterview(aiInterview);

        // Then
        verify(timerService, times(1)).stopTimer();
        verify(aiInterviewService, times(1)).stopAudioCapture();
        verify(aiInterviewService, times(1)).stopCurrentAudio();
        assertFalse((Boolean) ReflectionTestUtils.getField(aiInterviewService, "interviewInProgress"));
    }
}
