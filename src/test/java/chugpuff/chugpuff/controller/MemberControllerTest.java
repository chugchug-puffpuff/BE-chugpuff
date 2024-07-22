package chugpuff.chugpuff.controller;

import chugpuff.chugpuff.domain.Member;
import chugpuff.chugpuff.dto.MemberDTO;
import chugpuff.chugpuff.dto.PasswordUpdateDTO;
import chugpuff.chugpuff.service.MemberService;
import jakarta.mail.MessagingException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;

public class MemberControllerTest {

    @Mock
    private MemberService memberService;

    @InjectMocks
    private MemberController memberController;

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    private void setPrivateField(Object target, String fieldName, Object value) throws NoSuchFieldException, IllegalAccessException {
        Field field = target.getClass().getDeclaredField(fieldName);
        field.setAccessible(true);
        field.set(target, value);
    }

    @Test
    @DisplayName("새 회원 추가 성공")
    public void testAddMember() throws NoSuchFieldException, IllegalAccessException {
        MemberDTO memberDTO = new MemberDTO();
        memberDTO.setEmail("test1@example.com");
        memberDTO.setPassword("1234");

        Member savedMember = new Member();
        savedMember.setUser_id(1L);
        savedMember.setEmail("test1@example.com");
        savedMember.setPassword("1234");

        when(memberService.saveMember(any(Member.class))).thenReturn(savedMember);

        Map<String, Boolean> emailVerificationStatus = new ConcurrentHashMap<>();
        emailVerificationStatus.put(memberDTO.getEmail(), true);
        setPrivateField(memberController, "emailVerificationStatus", emailVerificationStatus);

        ResponseEntity<?> response = memberController.addMember(memberDTO);

        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertTrue(response.getBody() instanceof MemberDTO);
        MemberDTO responseDTO = (MemberDTO) response.getBody();
        assertEquals(savedMember.getUser_id(), responseDTO.getUser_id());

        System.out.println("새 회원 추가 성공");
    }






    @Test
    @DisplayName("회원 ID로 회원 조회 성공")
    public void testGetMemberByUserId() {
        Long userId = 1L;
        Member member = new Member();
        member.setUser_id(userId);
        member.setEmail("test1@example.com");

        when(memberService.getMemberByUser_id(userId)).thenReturn(Optional.of(member));

        ResponseEntity<MemberDTO> response = memberController.getMemberByUser_id(userId);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(userId, response.getBody().getUser_id());

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

        when(memberService.getAllMembers()).thenReturn(Arrays.asList(member1, member2));

        ResponseEntity<List<MemberDTO>> response = memberController.getAllMembers();

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertTrue(response.getBody() instanceof List);
        List<MemberDTO> memberDTOs = response.getBody();
        assertEquals(2, memberDTOs.size());
    }

    @Test
    @DisplayName("회원 삭제 성공")
    public void testDeleteMember() {
        Long userId = 1L;

        ResponseEntity<Void> response = memberController.deleteMember(userId);

        assertEquals(HttpStatus.NO_CONTENT, response.getStatusCode());

        System.out.println("회원 삭제 성공");
    }

    @Test
    @DisplayName("비밀번호 업데이트 성공")
    public void testUpdatePassword() {
        Long userId = 1L;
        String oldPassword = "1234";
        PasswordUpdateDTO passwordUpdateDTO = new PasswordUpdateDTO();
        passwordUpdateDTO.setNewPassword("5678");
        passwordUpdateDTO.setConfirmPassword("5678");

        Member member = new Member();
        member.setUser_id(userId);
        member.setPassword("5678");

        when(memberService.updatePassword(userId, oldPassword, passwordUpdateDTO.getNewPassword())).thenReturn(member);

        ResponseEntity<Object> response = memberController.updatePassword(userId, oldPassword, passwordUpdateDTO);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertTrue(response.getBody() instanceof MemberDTO);
        MemberDTO responseDTO = (MemberDTO) response.getBody();
        assertEquals(userId, responseDTO.getUser_id());

        System.out.println("비밀번호 업데이트 성공");
    }

    @Test
    @DisplayName("회원 정보 업데이트 성공")
    public void testUpdateMember() {
        Long user_id = 1L;
        MemberDTO memberDTO = new MemberDTO();
        memberDTO.setJob("Developer");
        memberDTO.setJobKeyword("Java");

        Member updatedMember = new Member();
        updatedMember.setUser_id(user_id);
        updatedMember.setJob("Developer");
        updatedMember.setJobKeyword("Java");

        when(memberService.updateMember(any(Long.class), any(Member.class))).thenReturn(updatedMember);

        ResponseEntity<Object> response = memberController.updateMember(user_id, memberDTO);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertTrue(response.getBody() instanceof MemberDTO);
        MemberDTO responseDTO = (MemberDTO) response.getBody();
        assertEquals(updatedMember.getJob(), responseDTO.getJob());
        assertEquals(updatedMember.getJobKeyword(), responseDTO.getJobKeyword());
    }

    @Test
    @DisplayName("회원 ID 중복 체크 성공")
    public void testCheckUserIdDuplicate() {
        String id = "test1";
        when(memberService.checkUserIdDuplicate(id)).thenReturn(true);

        ResponseEntity<Boolean> response = memberController.checkUserIdDuplicate(id);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertTrue(response.getBody());

        System.out.println("회원 ID 중복 체크 성공");
    }
}
