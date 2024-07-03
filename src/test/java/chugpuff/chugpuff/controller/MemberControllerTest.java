package chugpuff.chugpuff.controller;

import chugpuff.chugpuff.controller.MemberController;
import chugpuff.chugpuff.domain.Member;
import chugpuff.chugpuff.dto.MemberDTO;
import chugpuff.chugpuff.service.MemberService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public class MemberControllerTest {

    @Mock
    private MemberService memberService;

    @InjectMocks
    private MemberController memberController;

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    @DisplayName("새 회원 추가 성공")
    public void testAddMember() {
        MemberDTO memberDTO = new MemberDTO();
        memberDTO.setId("test1");
        memberDTO.setPassword("1234");

        Member savedMember = new Member();
        savedMember.setUser_id(1L);
        savedMember.setId("test1");
        savedMember.setPassword("1234");

        when(memberService.saveMember(any(Member.class))).thenReturn(savedMember);

        ResponseEntity<?> response = memberController.addMember(memberDTO);

        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertTrue(response.getBody() instanceof MemberDTO);
        MemberDTO responseDTO = (MemberDTO) response.getBody();
        assertEquals(savedMember.getUser_id(), responseDTO.getUser_id());

        System.out.println("새 회원 추가 성공");
    }

    @Test
    @DisplayName("회원 ID로 회원 조회 성공")
    public void testGetMemberById() {
        Long user_id = 1L;
        Member member = new Member();
        member.setUser_id(user_id);
        member.setId("test1");

        when(memberService.getMemberById(user_id)).thenReturn(Optional.of(member));

        ResponseEntity<MemberDTO> response = memberController.getMemberById(user_id);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(member.getUser_id(), response.getBody().getUser_id());

        System.out.println("회원 ID로 회원 조회 성공");
    }

    @Test
    @DisplayName("모든 회원 조회 성공")
    public void testGetAllMembers() {
        List<Member> members = Arrays.asList(
                new Member(1L, "test1", "1234", "name1", "IT개발·데이터", "백엔드/서버개발", "test1@test.com", true, true, true, true),
                new Member(2L, "test2", "5678", "name2", "IT개발·데이터", "웹개발", "test2@test.com", true, true, true, true)
        );

        when(memberService.getAllMembers()).thenReturn(members);

        ResponseEntity<List<MemberDTO>> response = memberController.getAllMembers();

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(members.size(), response.getBody().size());
        assertEquals(members.get(0).getUser_id(), response.getBody().get(0).getUser_id());
        assertEquals(members.get(1).getUser_id(), response.getBody().get(1).getUser_id());

        System.out.println("모든 회원 조회 성공");
    }

    @Test
    @DisplayName("회원 삭제 성공")
    public void testDeleteMember() {
        Long user_id = 1L;

        ResponseEntity<Void> response = memberController.deleteMember(user_id);

        verify(memberService, times(1)).deleteMember(user_id);

        assertEquals(HttpStatus.NO_CONTENT, response.getStatusCode());

        System.out.println("회원 삭제 성공");
    }

    @Test
    @DisplayName("회원 정보 업데이트 성공")
    public void testUpdateMember() {
        Long user_id = 1L;
        String password = "oldPassword";

        MemberDTO memberDTO = new MemberDTO();
        memberDTO.setId("test1");
        memberDTO.setPassword("4321");
        memberDTO.setName("name1");
        memberDTO.setJob("IT개발·데이터");
        memberDTO.setJobKeyword("백엔드/서버개발");
        memberDTO.setEmail("test1@test.com");
        memberDTO.setIsAbove15(true);
        memberDTO.setTermsAccepted(true);
        memberDTO.setPrivacyPolicyAccepted(true);
        memberDTO.setRecordingAccepted(true);

        Member updatedMember = new Member();
        updatedMember.setUser_id(user_id);
        updatedMember.setId("test1");
        updatedMember.setPassword("4321");
        updatedMember.setName("name1");
        updatedMember.setJob("IT개발·데이터");
        updatedMember.setJobKeyword("웹개발");
        updatedMember.setEmail("test1@test.com");
        updatedMember.setIsAbove15(true);
        updatedMember.setTermsAccepted(true);
        updatedMember.setPrivacyPolicyAccepted(true);
        updatedMember.setRecordingAccepted(true);

        when(memberService.isPasswordCorrect(user_id, password)).thenReturn(true);
        when(memberService.updateMember(eq(user_id), eq(password), any(Member.class))).thenReturn(updatedMember);

        ResponseEntity<Object> response = memberController.updateMember(user_id, password, memberDTO);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertTrue(response.getBody() instanceof MemberDTO);
        MemberDTO responseDTO = (MemberDTO) response.getBody();
        assertEquals(updatedMember.getUser_id(), responseDTO.getUser_id());
        assertEquals(updatedMember.getName(), responseDTO.getName());
        assertEquals(updatedMember.getEmail(), responseDTO.getEmail());

        System.out.println("회원 정보 업데이트 성공");
    }

    @Test
    @DisplayName("회원 ID 중복 체크 성공")
    public void testCheckUserIdDuplicate() {
        String user_id = "testId";

        when(memberService.checkUserIdDuplicate(user_id)).thenReturn(true);

        ResponseEntity<Boolean> response = memberController.checkUserIdDuplicate(user_id);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertTrue(response.getBody());

        System.out.println("회원 ID 중복 체크 성공");
    }
}
