package chugpuff.chugpuff.service;

import chugpuff.chugpuff.domain.AIInterview;
import chugpuff.chugpuff.domain.AIInterviewFF;
import chugpuff.chugpuff.domain.AIInterviewIF;
import chugpuff.chugpuff.domain.Member;
import chugpuff.chugpuff.entity.EditSelfIntroduction;
import chugpuff.chugpuff.entity.EditSelfIntroductionDetails;
import chugpuff.chugpuff.repository.*;
import javazoom.jl.player.Player;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.test.util.ReflectionTestUtils;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.Collections;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class AIInterviewServiceTest {

    @InjectMocks
    private AIInterviewService aiInterviewService;

    @Mock
    private AIInterviewRepository aiInterviewRepository;

    @Mock
    private EditSelfIntroductionRepository editSelfIntroductionRepository;

    @Mock
    private EditSelfIntroductionDetailsRepository editSelfIntroductionDetailsRepository;

    @Mock
    private AIInterview aiInterview;

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
    void setUp() throws Exception {
        MockitoAnnotations.openMocks(this);
        userDetails = createTestUserDetails();
        member = createTestMember();
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
    void testGetSelfIntroductionContentForInterview() {
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
    void testGetSelfIntroductionContentForInterview_NoSelfIntroduction() {
        // Given
        when(memberService.getMemberByUsername("testUser")).thenReturn(Optional.of(member));
        when(editSelfIntroductionRepository.findByMember(member)).thenReturn(Collections.emptyList());

        // When / Then
        Exception exception = assertThrows(RuntimeException.class, () -> {
            aiInterviewService.getSelfIntroductionContentForInterview(userDetails);
        });
        assertEquals("저장된 자기소개서를 찾을 수 없습니다.", exception.getMessage());
    }

    @Test
    void testStartInterview() {
        // Given
        AIInterview interview = new AIInterview();
        interview.setInterviewType("인성 면접");
        interview.setMember(member);

        when(aiInterviewRepository.findById(1L)).thenReturn(Optional.of(interview));
        when(externalAPIService.callChatGPT(anyString())).thenReturn("질문: 갈등을 어떻게 해결합니까?");
        when(externalAPIService.callTTS(anyString())).thenReturn("validAudioUrl.mp3");  // 여기서 유효한 오디오 URL을 반환하도록 설정
        doNothing().when(timerService).startTimer(anyLong(), any());

        // When
        aiInterviewService.startInterview(1L, userDetails);

        // Then
        verify(timerService, times(1)).startTimer(anyLong(), any());
        verify(externalAPIService, times(1)).callChatGPT(anyString());
        assertEquals("갈등을 어떻게 해결합니까?", aiInterviewService.getCurrentQuestion());
    }

    @Test
    void testInitializeInterviewSession_InvalidInterviewType() {
        // Given
        AIInterview aiInterview = new AIInterview();
        aiInterview.setInterviewType("Invalid Type");

        UserDetails userDetails = User.builder()
                .username("testUser")
                .password("password")
                .roles("USER")
                .build();

        // When & Then
        assertThrows(RuntimeException.class, () -> {
            aiInterviewService.initializeInterviewSession(aiInterview, userDetails);
        });
    }


    @Test
    void testGetChatGPTQuestion() {
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
    void testStartInterview_InterviewInProgress() {
        // Given
        AIInterview aiInterview = new AIInterview();
        aiInterview.setAIInterviewNo(1L);
        aiInterview.setInterviewType("인성 면접");

        UserDetails userDetails = User.builder()
                .username("testUser")
                .password("password")
                .roles("USER")
                .build();

        when(aiInterviewRepository.findById(1L)).thenReturn(Optional.of(aiInterview));
        when(externalAPIService.callChatGPT(anyString())).thenReturn("질문: Example question");

        aiInterviewService.startInterview(1L, userDetails);

        // When & Then
        verify(timerService, times(1)).startTimer(anyLong(), any());
    }

    @Test
    void testEndInterview() {
        // Given
        AIInterview interview = new AIInterview();
        interview.setFeedbackType("전체 피드백");

        // Mock 인터뷰 진행 중 상태로 설정
        ReflectionTestUtils.setField(aiInterviewService, "interviewInProgress", true);

        // When
        aiInterviewService.endInterview(interview);

        // Then
        verify(timerService, times(1)).stopTimer();
    }
}
