package chugpuff.chugpuff.service;

import chugpuff.chugpuff.domain.Member;
import chugpuff.chugpuff.entity.EditSelfIntroduction;
import chugpuff.chugpuff.entity.EditSelfIntroductionDetails;
import chugpuff.chugpuff.repository.EditSelfIntroductionDetailsRepository;
import chugpuff.chugpuff.repository.EditSelfIntroductionRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class EditSelfIntroductionServiceTest {

    @Mock
    private EditSelfIntroductionRepository editSelfIntroductionRepository;

    @Mock
    private EditSelfIntroductionDetailsRepository editSelfIntroductionDetailsRepository;

    @Mock
    private ChatGPTService chatGPTService;

    @Mock
    private MemberService memberService;

    @InjectMocks
    private EditSelfIntroductionService editSelfIntroductionService;

    private Member testMember;
    private List<EditSelfIntroductionDetails> details;


    @BeforeEach
    public void setUp() {
        testMember = new Member();
        testMember.setId(String.valueOf(1L));
        testMember.setName("Test User");

        details = new ArrayList<>();
        EditSelfIntroductionDetails detail1 = new EditSelfIntroductionDetails();
        detail1.setES_question("본인이 끝까지 파고들어 본 가장 의미있었던 개발 경험 또는 개발 활동에 대해 얘기해 주세요");
        detail1.setES_answer("올해 여름 진행했던 팀 프로젝트 경험에 대해 말씀드리겠습니다. 저는 웹 개발 공부를 하며 여러 개인 프로젝트를 수행하며 실력을 쌓아갔습니다. 하지만 혼자 강의를 통해 공부를 진행하다 보니 개인 프로젝트만 진행하였고 팀 프로젝트도 경험해보고 싶었습니다");
        details.add(detail1);

        EditSelfIntroductionDetails detail2 = new EditSelfIntroductionDetails();
        detail2.setES_question("지원하신 포지션과 연관지어, 학교 수업 또는 대외활동 등을 통해 습득한 'CS 지식'이나 '기술적 역량'에 대해 설명해 주세요.");
        detail2.setES_answer("개발자에게 에러는 항상 생각해야 할 문제입니다. 저는 에러를 해결할 줄 아는 능력이 개발자에게 중요하다고 생각합니다. 이런 에러 해결 능력의 기본이 되는 것이 컴퓨터에 대한 기초지식입니다.");
        details.add(detail2);
    }

    @Test
    public void testProvideFeedbackAndSave() {
        /// ChatGPT 응답 Mock 설정
        String mockResponse = "Mock GPT Response";
        String mockFeedback = "Mock Feedback";
        when(chatGPTService.callChatGPTForFeedback(details)).thenReturn(mockResponse);
        when(chatGPTService.extractChatGPTFeedback(mockResponse)).thenReturn(mockFeedback);

        // 저장 호출에 대한 Mock 설정
        when(editSelfIntroductionRepository.save(any(EditSelfIntroduction.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(editSelfIntroductionDetailsRepository.save(any(EditSelfIntroductionDetails.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // 서비스 메서드 호출
        EditSelfIntroduction result = editSelfIntroductionService.provideFeedbackAndSave(testMember, details);

        // 검증
        assertNotNull(result);
        assertEquals(mockFeedback, result.getES_feedback());
        assertEquals(testMember, result.getMember());
        assertEquals(LocalDate.now(), result.getES_date());

        // 상호작용 확인
        verify(chatGPTService, times(1)).callChatGPTForFeedback(details);
        verify(chatGPTService, times(1)).extractChatGPTFeedback(mockResponse);
        verify(editSelfIntroductionRepository, times(2)).save(any(EditSelfIntroduction.class));
        verify(editSelfIntroductionDetailsRepository, times(2)).save(any(EditSelfIntroductionDetails.class));
    }

    @Test
    public void testGetSelfIntroductionsByMember() {
        List<EditSelfIntroduction> mockIntroductions = new ArrayList<>();
        mockIntroductions.add(new EditSelfIntroduction());
        when(editSelfIntroductionRepository.findByMember(testMember)).thenReturn(mockIntroductions);

        List<EditSelfIntroduction> introductions = editSelfIntroductionService.getSelfIntroductionsByMember(testMember);
        assertNotNull(introductions);
        assertEquals(1, introductions.size());

        verify(editSelfIntroductionRepository, times(1)).findByMember(testMember);
    }

    @Test
    public void testGetSelfIntroductionById() {
        EditSelfIntroduction mockIntroduction = new EditSelfIntroduction();
        when(editSelfIntroductionRepository.findById(1L)).thenReturn(Optional.of(mockIntroduction));

        Optional<EditSelfIntroduction> introduction = editSelfIntroductionService.getSelfIntroductionById(1L);
        assertTrue(introduction.isPresent());
        assertEquals(mockIntroduction, introduction.get());

        verify(editSelfIntroductionRepository, times(1)).findById(1L);
    }

    @Test
    public void testDeleteSelfIntroductionById() {
        doNothing().when(editSelfIntroductionRepository).deleteById(1L);
        editSelfIntroductionService.deleteSelfIntroductionById(1L);
        verify(editSelfIntroductionRepository, times(1)).deleteById(1L);
    }

    @Test
    void testSaveSelectedSelfIntroduction() {
        Long selfIntroductionId = 1L;
        String username = "Test User";

        EditSelfIntroduction existingIntro = new EditSelfIntroduction();
        existingIntro.setES_no(2L);
        existingIntro.setMember(testMember);
        existingIntro.setSave(true); // 이미 저장된 상태

        EditSelfIntroduction selectedIntro = new EditSelfIntroduction();
        selectedIntro.setES_no(selfIntroductionId);
        selectedIntro.setMember(testMember);
        selectedIntro.setSave(false); // 아직 저장되지 않은 상태

        // Mock 리턴 값 설정
        when(memberService.getMemberByUsername(username)).thenReturn(Optional.of(testMember));
        when(editSelfIntroductionRepository.findByMemberAndSaveTrue(testMember)).thenReturn(Optional.of(existingIntro));
        when(editSelfIntroductionRepository.findById(selfIntroductionId)).thenReturn(Optional.of(selectedIntro));

        // When: 메서드 호출
        editSelfIntroductionService.saveSelectedSelfIntroduction(selfIntroductionId, username);

        // Then: 기존의 저장된 자기소개서가 false로 변경되었는지 확인
        assertFalse(existingIntro.isSave());

        // 선택된 자기소개서가 true로 변경되었는지 확인
        assertTrue(selectedIntro.isSave());

        // Verify: 메서드 호출 여부 확인
        verify(memberService, times(1)).getMemberByUsername(username);
        verify(editSelfIntroductionRepository, times(1)).findByMemberAndSaveTrue(testMember);
        verify(editSelfIntroductionRepository, times(1)).findById(selfIntroductionId);
        verify(editSelfIntroductionRepository, times(2)).save(any(EditSelfIntroduction.class));
    }
}