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

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

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
    private AIInterviewFFBRepository aiInterviewFFBRepository;

    @Mock
    private MemberService memberService;

    @Mock
    private ExternalAPIService externalAPIService;

    @Mock
    private TimerService timerService;

    @Mock
    private Player mockPlayer;

    @Mock
    private FileInputStream mockFileInputStream;

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

        when(memberService.getMemberByUsername("testUser")).thenReturn(Optional.of(member));
        when(editSelfIntroductionRepository.findByMember(member)).thenReturn(Collections.singletonList(selfIntroduction));
        when(editSelfIntroductionDetailsRepository.findByEditSelfIntroduction(selfIntroduction))
                .thenReturn(Collections.singletonList(detail));

        // When
        String result = aiInterviewService.getSelfIntroductionContentForInterview(userDetails);

        // Then
        assertNotNull(result);
        assertTrue(result.contains("당신의 강점은 무엇입니까?"));
        assertTrue(result.contains("저는 매우 성실합니다."));
    }

    @Test
    void testGetSelfIntroductionContentForInterview_NoSelfIntroductionFound() {
        // Given
        when(memberService.getMemberByUsername("testUser")).thenReturn(Optional.of(member));
        when(editSelfIntroductionRepository.findByMember(member)).thenReturn(Collections.emptyList());

        // When / Then
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            aiInterviewService.getSelfIntroductionContentForInterview(userDetails);
        });
        assertEquals("저장된 자기소개서를 찾을 수 없습니다.", exception.getMessage());
    }

    @Test
    void testInitializeInterviewSession_ForSelfIntroductionInterview() {
        // Given
        AIInterview aiInterview = new AIInterview();
        aiInterview.setInterviewType("자기소개서 면접");

        // Mock user details and member
        when(memberService.getMemberByUsername("testUser")).thenReturn(Optional.of(member));

        // Create mock self introduction
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
        String result = aiInterviewService.initializeInterviewSession(aiInterview, userDetails);

        // Then
        assertNotNull(result);
        assertEquals("자기소개서 기반의 질문입니다.", result);

        // Verify the ChatGPT prompt includes the self-introduction content
        verify(externalAPIService, times(1)).callChatGPT(contains("당신의 강점은 무엇입니까?"));
        verify(externalAPIService, times(1)).callChatGPT(contains("저는 매우 성실합니다."));
    }

    @Test
    void testStartInterview_Success() {
        // Given
        AIInterview interview = new AIInterview();
        interview.setInterviewType("인성 면접");
        interview.setMember(member);

        when(aiInterviewRepository.findById(1L)).thenReturn(Optional.of(interview));
        when(externalAPIService.callChatGPT(anyString())).thenReturn("질문: 갈등을 어떻게 해결합니까?");
        when(externalAPIService.callTTS(anyString())).thenReturn("validAudioUrl.mp3");
        doNothing().when(timerService).startTimer(anyLong(), any());

        // When
        aiInterviewService.startInterview(1L, userDetails);

        // Then
        verify(timerService, times(1)).startTimer(anyLong(), any());
        verify(externalAPIService, times(1)).callChatGPT(anyString());
        assertEquals("갈등을 어떻게 해결합니까?", aiInterviewService.getCurrentQuestion());
    }

    @Test
    void testInitializeInterviewSession_WithInvalidInterviewType() {
        // Given
        AIInterview aiInterview = new AIInterview();
        aiInterview.setInterviewType("Invalid Type");

        // When & Then
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            aiInterviewService.initializeInterviewSession(aiInterview, userDetails);
        });
        assertEquals("Invalid interview type", exception.getMessage());
    }

    @Test
    void testHandleInterviewProcess_Success() throws Exception {
        // Given
        AIInterview aiInterview = new AIInterview();
        aiInterview.setAIInterviewNo(1L);
        aiInterview.setInterviewType("직무 면접");
        aiInterview.setMember(member);

        String generatedQuestion = "Java 관련 경험은 무엇입니까?";
        String ttsUrl = "validAudioUrl.mp3";

        ReflectionTestUtils.setField(aiInterviewService, "interviewInProgress", true);

        when(externalAPIService.callTTS(generatedQuestion)).thenReturn(ttsUrl);

        FileInputStream mockFileInputStream = mock(FileInputStream.class);
        Player mockPlayer = mock(Player.class);

        doNothing().when(aiInterviewService).playAudio(anyString());

        doNothing().when(mockPlayer).play();

        // When
        aiInterviewService.handleInterviewProcess(aiInterview, generatedQuestion);

        // Then
        verify(externalAPIService, times(1)).callTTS(generatedQuestion);
        verify(aiInterviewService, times(1)).playAudio(ttsUrl);
        verify(aiInterviewService, times(1)).captureUserAudio();
    }

    @Test
    void testGetChatGPTQuestion_Success() {
        // Given
        AIInterview interview = new AIInterview();
        interview.setInterviewType("직무 면접");
        interview.setMember(member);
        interview.getMember().setJob("소프트웨어 엔지니어");
        interview.getMember().setJobKeyword("Java");

        String lastQuestion = "Java에 대한 경험은 무엇입니까?";
        String lastResponse = "저는 Java 개발 경험이 5년 있습니다.";

        when(externalAPIService.callChatGPT(anyString())).thenReturn("Java 개발에서 겪었던 어려움은 무엇입니까?");

        // When
        String result = aiInterviewService.getChatGPTQuestion(interview, lastQuestion, lastResponse, null);

        // Then
        assertEquals("Java 개발에서 겪었던 어려움은 무엇입니까?", result);
    }

    @Test
    void testHandleFullFeedback_Success() throws Exception {
        // Given
        AIInterview aiInterview = new AIInterview();
        aiInterview.setAIInterviewNo(1L);
        aiInterview.setFeedbackType("전체 피드백");

        AIInterviewFF response1 = new AIInterviewFF();
        response1.setF_question("첫 번째 질문?");
        response1.setF_answer("첫 번째 답변.");

        AIInterviewFF response2 = new AIInterviewFF();
        response2.setF_question("두 번째 질문?");
        response2.setF_answer("두 번째 답변.");

        List<AIInterviewFF> responses = Arrays.asList(response1, response2);

        when(aiInterviewFFRepository.findByAiInterview(aiInterview)).thenReturn(responses);
        when(externalAPIService.callChatGPT(anyString())).thenReturn("전체 피드백 내용입니다.");

        Method handleFullFeedbackMethod = AIInterviewService.class.getDeclaredMethod("handleFullFeedback", AIInterview.class);
        handleFullFeedbackMethod.setAccessible(true);

        // When
        handleFullFeedbackMethod.invoke(aiInterviewService, aiInterview);

        // Then
        verify(externalAPIService, times(1)).callChatGPT(anyString());
        verify(aiInterviewFFBRepository, times(1)).save(any(AIInterviewFFB.class));
    }

    @Test
    void testSaveUserResponse_InterviewInProgress() {
        // Given
        AIInterview aiInterview = new AIInterview();
        aiInterview.setInterviewType("직무 면접");
        aiInterview.setFeedbackType("전체 피드백");

        String question = "당신의 기술 스택은 무엇입니까?";
        String response = "Java와 Spring Boot를 사용합니다.";

        ReflectionTestUtils.setField(aiInterviewService, "interviewInProgress", true);

        // When
        aiInterviewService.saveUserResponse(aiInterview, question, response);

        // Then
        verify(aiInterviewFFRepository, times(1)).save(any(AIInterviewFF.class));
    }

    @Test
    void testSaveImmediateFeedback_InterviewEnded() {
        // Given
        AIInterview aiInterview = new AIInterview();
        aiInterview.setInterviewType("직무 면접");

        String question = "당신의 기술 스택은 무엇입니까?";
        String response = "Java와 Spring Boot를 사용합니다.";
        String feedback = "좋은 기술 스택입니다.";

        ReflectionTestUtils.setField(aiInterviewService, "interviewInProgress", false);

        // When
        aiInterviewService.saveImmediateFeedback(aiInterview, question, response, feedback);

        // Then
        verify(aiInterviewIFRepository, times(0)).save(any(AIInterviewIF.class));
    }

    @Test
    void testStartInterview_WhileInterviewInProgress() {
        // Given
        AIInterview aiInterview = new AIInterview();
        aiInterview.setAIInterviewNo(1L);
        aiInterview.setInterviewType("인성 면접");

        when(aiInterviewRepository.findById(1L)).thenReturn(Optional.of(aiInterview));
        when(externalAPIService.callChatGPT(anyString())).thenReturn("질문: Example question");

        aiInterviewService.startInterview(1L, userDetails);

        // When & Then
        verify(timerService, times(1)).startTimer(anyLong(), any());
    }

    @Test
    void testEndInterview_Success() {
        // Given
        AIInterview interview = new AIInterview();
        interview.setFeedbackType("전체 피드백");

        ReflectionTestUtils.setField(aiInterviewService, "interviewInProgress", true);

        // When
        aiInterviewService.endInterview(interview);

        // Then
        verify(timerService, times(1)).stopTimer();
    }

    @Test
    void testGetInterviewById_Success() {
        // Given
        AIInterview interview = new AIInterview();
        interview.setAIInterviewNo(1L);
        when(aiInterviewRepository.findById(1L)).thenReturn(Optional.of(interview));

        // When
        AIInterview result = aiInterviewService.getInterviewById(1L);

        // Then
        assertNotNull(result);
        assertEquals(1L, result.getAIInterviewNo());
    }

    @Test
    void testDeleteInterviewById_Success() {
        // Given
        AIInterview interview = new AIInterview();
        interview.setAIInterviewNo(1L);
        when(aiInterviewRepository.findById(1L)).thenReturn(Optional.of(interview));

        // When
        aiInterviewService.deleteInterviewById(1L);

        // Then
        verify(aiInterviewRepository, times(1)).delete(interview);
    }

    @Test
    void testDeleteInterviewById_NotFound() {
        // Given
        when(aiInterviewRepository.findById(1L)).thenReturn(Optional.empty());

        // When & Then
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            aiInterviewService.deleteInterviewById(1L);
        });
        assertEquals("Interview not found with ID: 1", exception.getMessage());
    }
}
