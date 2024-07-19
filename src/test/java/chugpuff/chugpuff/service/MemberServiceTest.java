package chugpuff.chugpuff.service;

import chugpuff.chugpuff.domain.Member;
import chugpuff.chugpuff.repository.MemberRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

public class MemberServiceTest {

    @Mock
    private MemberRepository memberRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private MemberService memberService;

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    @DisplayName("새 회원 저장 성공")
    public void testSaveMember() {
        Member member = new Member();
        member.setEmail("test1@example.com");
        member.setPassword("1234");
        member.setIsAbove15(true);
        member.setPrivacyPolicyAccepted(true);
        member.setRecordingAccepted(true);

        Member savedMember = new Member();
        savedMember.setUser_id(1L);
        savedMember.setEmail("test1@example.com");
        savedMember.setPassword("1234");

        when(memberRepository.save(any(Member.class))).thenReturn(savedMember);

        Member result = memberService.saveMember(member);

        assertNotNull(result);
        assertEquals(savedMember.getUser_id(), result.getUser_id());
        assertEquals(savedMember.getEmail(), result.getEmail());
        System.out.println("새 회원 저장 성공");
    }

    @Test
    @DisplayName("회원 ID로 회원 조회 성공")
    public void testGetMemberByUserId() {
        Long userId = 1L;
        Member member = new Member();
        member.setUser_id(userId);
        member.setEmail("test1@example.com");

        when(memberRepository.findById(userId)).thenReturn(Optional.of(member));

        Optional<Member> result = memberService.getMemberByUser_id(userId);

        assertTrue(result.isPresent());
        assertEquals(userId, result.get().getUser_id());
        System.out.println("회원 ID로 회원 조회 성공");
    }

    @Test
    @DisplayName("모든 회원 조회 성공")
    public void testGetAllMembers() {
        Member member1 = new Member();
        member1.setUser_id(1L);
        member1.setId("test1");
        member1.setPassword("1234");

        Member member2 = new Member();
        member2.setUser_id(2L);
        member2.setId("test2");
        member2.setPassword("5678");

        when(memberRepository.findAll()).thenReturn(Arrays.asList(member1, member2));

        Iterable<Member> result = memberService.getAllMembers();

        assertNotNull(result);
        assertEquals(2, ((List<Member>) result).size());
        System.out.println("모든 회원 조회 성공");
    }

    @Test
    @DisplayName("회원 삭제 성공")
    public void testDeleteMember() {
        Long userId = 1L;
        memberService.deleteMember(userId);
        verify(memberRepository, times(1)).deleteById(userId); // Verify that deleteById was called once with userId
        System.out.println("회원 삭제 성공");
    }

    @Test
    @DisplayName("회원 정보 업데이트 성공")
    public void testUpdateMember() {
        Long userId = 1L;
        Member updatedMember = new Member();
        updatedMember.setJob("Developer");
        updatedMember.setJobKeyword("Java");

        Member existingMember = new Member();
        existingMember.setUser_id(userId);
        existingMember.setJob("Engineer");
        existingMember.setJobKeyword("Python");

        when(memberRepository.findById(userId)).thenReturn(Optional.of(existingMember));
        when(memberRepository.save(any(Member.class))).thenReturn(existingMember);

        Member result = memberService.updateMember(userId, updatedMember);

        assertNotNull(result);
        assertEquals(updatedMember.getJob(), result.getJob());
        assertEquals(updatedMember.getJobKeyword(), result.getJobKeyword());
        System.out.println("회원 정보 업데이트 성공");
    }

    @Test
    @DisplayName("비밀번호 업데이트 성공")
    public void testUpdatePassword() {
        Long userId = 1L;
        String oldPassword = "1234";
        String newPassword = "5678";

        Member member = new Member();
        member.setUser_id(userId);
        member.setPassword(oldPassword);

        when(memberRepository.findById(userId)).thenReturn(Optional.of(member));
        when(passwordEncoder.matches(oldPassword, member.getPassword())).thenReturn(true);
        when(passwordEncoder.encode(newPassword)).thenReturn("encoded5678");
        when(memberRepository.save(any(Member.class))).thenReturn(member);

        Member result = memberService.updatePassword(userId, oldPassword, newPassword);

        assertNotNull(result);
        assertEquals("encoded5678", result.getPassword());
        verify(passwordEncoder, times(1)).encode(newPassword);
        System.out.println("비밀번호 업데이트 성공");
    }

    @Test
    @DisplayName("모든 필수 항목 동의 체크 - 동의하지 않은 경우")
    public void testValidateAllTermsAccepted_NotAllAgreed() {
        Member member = new Member();
        member.setIsAbove15(false);
        member.setPrivacyPolicyAccepted(true);
        member.setRecordingAccepted(true);

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            memberService.saveMember(member);
        });

        assertEquals("모든 필수 항목에 동의해야 회원가입이 가능합니다.", exception.getMessage());
    }

    @Test
    @DisplayName("모든 필수 항목 동의 체크 - 모두 동의한 경우")
    public void testValidateAllTermsAccepted_AllAgreed() {
        Member member = new Member();
        member.setIsAbove15(true);
        member.setPrivacyPolicyAccepted(true);
        member.setRecordingAccepted(true);

        Member savedMember = new Member();
        savedMember.setUser_id(1L);
        savedMember.setEmail("test1@example.com");
        savedMember.setPassword("1234");

        when(memberRepository.save(any(Member.class))).thenReturn(savedMember);

        Member result = memberService.saveMember(member);

        assertNotNull(result);
        assertEquals(savedMember.getUser_id(), result.getUser_id());
        assertEquals(savedMember.getEmail(), result.getEmail());
    }
}
