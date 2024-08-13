package chugpuff.chugpuff.controller;

import chugpuff.chugpuff.domain.Member;
import chugpuff.chugpuff.entity.EditSelfIntroduction;
import chugpuff.chugpuff.entity.EditSelfIntroductionDetails;
import chugpuff.chugpuff.service.EditSelfIntroductionService;
import chugpuff.chugpuff.service.MemberService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class EditSelfIntroductionControllerTest {

    @Mock
    private EditSelfIntroductionService editSelfIntroductionService;

    @Mock
    private MemberService memberService;

    @Mock
    private UserDetails userDetails;

    @InjectMocks
    private EditSelfIntroductionController editSelfIntroductionController;

    private Member testMember;

    @BeforeEach
    public void setUp() {
        testMember = new Member();
        testMember.setId(String.valueOf(1L));
        testMember.setName("Test User");
        testMember.setName("testuser");
    }

    @Test
    public void testProvideFeedback() {
        List<EditSelfIntroductionDetails> details = new ArrayList<>();
        EditSelfIntroduction mockIntroduction = new EditSelfIntroduction();
        when(userDetails.getUsername()).thenReturn("testuser");
        when(memberService.getMemberByUsername("testuser")).thenReturn(Optional.of(testMember));
        when(editSelfIntroductionService.provideFeedbackAndSave(testMember, details)).thenReturn(mockIntroduction);

        ResponseEntity<EditSelfIntroduction> response = editSelfIntroductionController.provideFeedback(userDetails, details);

        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(mockIntroduction, response.getBody());

        verify(memberService, times(1)).getMemberByUsername("testuser");
        verify(editSelfIntroductionService, times(1)).provideFeedbackAndSave(testMember, details);
    }

    @Test
    public void testGetSelfIntroductionsByMember() {
        List<EditSelfIntroduction> introductions = new ArrayList<>();
        when(userDetails.getUsername()).thenReturn("testuser");
        when(memberService.getMemberByUsername("testuser")).thenReturn(Optional.of(testMember));
        when(editSelfIntroductionService.getSelfIntroductionsByMember(testMember)).thenReturn(introductions);

        List<EditSelfIntroduction> response = editSelfIntroductionController.getSelfIntroductionsByMember(userDetails);

        assertNotNull(response);
        assertEquals(introductions, response);

        verify(memberService, times(1)).getMemberByUsername("testuser");
        verify(editSelfIntroductionService, times(1)).getSelfIntroductionsByMember(testMember);
    }

    @Test
    public void testGetSelfIntroductionById() {
        EditSelfIntroduction mockIntroduction = new EditSelfIntroduction();
        when(editSelfIntroductionService.getSelfIntroductionById(1L)).thenReturn(Optional.of(mockIntroduction));

        ResponseEntity<EditSelfIntroduction> response = editSelfIntroductionController.getSelfIntroductionById(1L);

        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(mockIntroduction, response.getBody());

        verify(editSelfIntroductionService, times(1)).getSelfIntroductionById(1L);
    }

    @Test
    public void testDeleteSelfIntroduction() {
        doNothing().when(editSelfIntroductionService).deleteSelfIntroductionById(1L);

        ResponseEntity<Void> response = editSelfIntroductionController.deleteSelfIntroduction(1L);

        assertNotNull(response);
        assertEquals(HttpStatus.NO_CONTENT, response.getStatusCode());

        verify(editSelfIntroductionService, times(1)).deleteSelfIntroductionById(1L);
    }

    @Test
    public void testSaveSelectedSelfIntroduction() {
        when(userDetails.getUsername()).thenReturn("testuser");

        ResponseEntity<String> response = editSelfIntroductionController.saveSelectedSelfIntroduction(1L, userDetails);

        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("자기소개서가 저장되었습니다.", response.getBody());

        verify(userDetails, times(1)).getUsername();
    }
}
