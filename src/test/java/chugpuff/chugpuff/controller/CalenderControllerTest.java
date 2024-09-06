package chugpuff.chugpuff.controller;

import chugpuff.chugpuff.domain.Member;
import chugpuff.chugpuff.entity.Calender;
import chugpuff.chugpuff.service.CalenderService;
import chugpuff.chugpuff.service.MemberService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureWebMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.core.userdetails.User;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.Optional;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(CalenderController.class)
@AutoConfigureMockMvc
@AutoConfigureWebMvc
class CalenderControllerTest {

    @MockBean
    private CalenderService calenderService;

    @Mock
    private MemberService memberService;

    @InjectMocks
    private CalenderController calenderController;

    @Autowired
    private MockMvc mockMvc;

    @BeforeEach
    public void setup() {
        MockitoAnnotations.openMocks(this);
        mockMvc = MockMvcBuilders.standaloneSetup(calenderController).build();
    }


    //일정 조회
    @Test
    void testGetCalenderById() throws Exception {
        Calender calender = new Calender();
        calender.setMemoNo(1L);
        calender.setMemoContent("Test Memo");

        when(calenderService.getCalenderById(1L)).thenReturn(Optional.of(calender));

        mockMvc.perform(get("/api/calenders/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.memoContent").value("Test Memo"));
    }

    //일정 생성
    @Test
    void testCreateCalender() throws Exception {
        Calender calender = new Calender();
        calender.setMemoContent("Test Memo");

        Member member = new Member();
        member.setName("user");

        when(memberService.getMemberByUsername("user")).thenReturn(Optional.of(member));
        when(calenderService.saveCalender(any(Calender.class))).thenReturn(calender);

        mockMvc.perform(post("/api/calenders")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"memoContent\":\"Test Memo\"}")
                        .principal(() -> "user"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.memoContent").value("Test Memo"));
    }

    //모든 일정 조회
    @Test
    void testGetAllCalenders() throws Exception {
        Member member = new Member();
        member.setName("user");

        Calender calender1 = new Calender();
        calender1.setMemoContent("Test Memo 1");

        Calender calender2 = new Calender();
        calender2.setMemoContent("Test Memo 2");

        when(memberService.getMemberByUsername("user")).thenReturn(Optional.of(member));
        when(calenderService.getCalendersByMember(member)).thenReturn(Arrays.asList(calender1, calender2));

        mockMvc.perform(get("/api/calenders").principal(() -> "user"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].memoContent").value("Test Memo 1"))
                .andExpect(jsonPath("$[1].memoContent").value("Test Memo 2"));
    }

    //일정 수정
    @Test
    void testUpdateCalender() throws Exception {
        Long calenderId = 1L;

        Calender updatedCalender = new Calender();
        updatedCalender.setMemoNo(calenderId);
        updatedCalender.setMemoContent("Updated Memo Content");
        updatedCalender.setMemoDate("2023-09-10");

        when(calenderService.updateCalender(eq(calenderId), any(Calender.class)))
                .thenReturn(updatedCalender);

        mockMvc.perform(put("/api/calenders/{id}", calenderId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"memoContent\":\"Updated Memo Content\", \"memoDate\":\"2023-09-10\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.memoContent").value("Updated Memo Content"))
                .andExpect(jsonPath("$.memoDate").value("2023-09-10"));
    }


    //일정 삭제
    @Test
    void testDeleteCalender() throws Exception {
        Long calenderId = 1L;

        doNothing().when(calenderService).deleteCalender(calenderId);

        mockMvc.perform(delete("/api/calenders/{id}", calenderId))
                .andExpect(status().isNoContent());

        verify(calenderService, times(1)).deleteCalender(calenderId);
    }


    //마감기한 푸시 알림
    @Test
    void testGetD1DeadlineNotifications() throws Exception {
        String username = "user";
        Member member = new Member();
        member.setName(username);

        Calender calender1 = new Calender();
        calender1.setMemoContent("Test Memo 1");
        calender1.setMemoDate(LocalDate.now().plusDays(1).toString());

        when(memberService.getMemberByUsername(username)).thenReturn(Optional.of(member));
        when(calenderService.getCalendersByMemberAndMemoDate(eq(member), any(LocalDate.class)))
                .thenReturn(Arrays.asList(calender1));

        mockMvc.perform(get("/api/calenders/notifications").principal(() -> username))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0]").value("스크랩한 공고 'Test Memo 1'의 마감 기한이 D-1입니다. 지금 바로 지원해 보세요!"));

        verify(calenderService, times(1)).getCalendersByMemberAndMemoDate(eq(member), any(LocalDate.class));
    }

}
