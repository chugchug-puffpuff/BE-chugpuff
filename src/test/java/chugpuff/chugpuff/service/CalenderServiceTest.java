package chugpuff.chugpuff.service;

import chugpuff.chugpuff.domain.Member;
import chugpuff.chugpuff.entity.Calender;
import chugpuff.chugpuff.entity.Scrap;
import chugpuff.chugpuff.repository.CalenderRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class CalenderServiceTest {

    @Mock
    private CalenderRepository calenderRepository;

    @InjectMocks
    private CalenderService calenderService;

    @Mock
    JobPostingService jobPostingService;

    @BeforeEach
    public void setup() {
        MockitoAnnotations.openMocks(this);
    }

    //일정 저장
    @Test
    void testSaveCalender() {
        Calender calender = new Calender();
        calender.setMemoContent("Test Memo");

        when(calenderRepository.save(calender)).thenReturn(calender);

        Calender savedCalender = calenderService.saveCalender(calender);
        assertEquals("Test Memo", savedCalender.getMemoContent());
    }

    //일정 조회
    @Test
    void testGetCalenderById() {
        Calender calender = new Calender();
        calender.setMemoNo(1L);

        when(calenderRepository.findById(1L)).thenReturn(Optional.of(calender));

        Optional<Calender> foundCalender = calenderService.getCalenderById(1L);
        assertTrue(foundCalender.isPresent());
        assertEquals(1L, foundCalender.get().getMemoNo());
    }

    //일정 수정
    @Test
    void testUpdateCalender() {
        Calender existingCalender = new Calender();
        existingCalender.setMemoNo(1L);
        existingCalender.setMemoContent("Old Memo");

        Calender updatedCalender = new Calender();
        updatedCalender.setMemoContent("Updated Memo");

        when(calenderRepository.findById(1L)).thenReturn(Optional.of(existingCalender));
        when(calenderRepository.save(existingCalender)).thenReturn(existingCalender);

        Calender result = calenderService.updateCalender(1L, updatedCalender);
        assertEquals("Updated Memo", result.getMemoContent());
    }

    //마감기한 공고 조회
    @Test
    void testGetCalendersByMemberAndMemoDate() {
        Member member = new Member();
        member.setName("user");

        Calender calender1 = new Calender();
        calender1.setMemoDate(LocalDate.now().plusDays(1).toString());

        Calender calender2 = new Calender();
        calender2.setMemoDate(LocalDate.now().plusDays(3).toString());

        when(calenderRepository.findByMember(member)).thenReturn(Arrays.asList(calender1, calender2));

        List<Calender> result = calenderService.getCalendersByMemberAndMemoDate(member, LocalDate.now().plusDays(1));
        assertEquals(1, result.size());
        assertEquals(calender1, result.get(0));
    }

    //일정 모두 조회
    @Test
    void testGetCalendersByMember() {
        Member member = new Member();
        member.setName("user");

        Calender calender1 = new Calender();
        calender1.setMemoContent("Memo 1");

        Calender calender2 = new Calender();
        calender2.setMemoContent("Memo 2");

        when(calenderRepository.findByMember(member)).thenReturn(Arrays.asList(calender1, calender2));

        List<Calender> calenders = calenderService.getCalendersByMember(member);

        assertEquals(2, calenders.size());
        assertEquals("Memo 1", calenders.get(0).getMemoContent());
        assertEquals("Memo 2", calenders.get(1).getMemoContent());
    }

    //일정 삭제
    @Test
    void testDeleteCalender() {
        Long calenderId = 1L;
        Calender calender = new Calender();
        calender.setMemoNo(calenderId);

        when(calenderRepository.findById(calenderId)).thenReturn(Optional.of(calender));

        calenderService.deleteCalender(calenderId);

        verify(calenderRepository, times(1)).delete(calender);
    }

    //스크랩 공고 마감기한 추가
    @Test
    void testScrapExpirationDateToCalender() {
        Scrap scrap = new Scrap();
        scrap.setJobId("job123");
        scrap.setMember(new Member());

        String mockJobDetails = "{ \"jobs\": { \"job\": [{ \"position\": { \"title\": \"Developer\" }, \"expiration-date\": \"2024-09-10\" }] } }";

        when(jobPostingService.getJobDetails("job123")).thenReturn(mockJobDetails);

        calenderService.scrapExpirationDateToCalender(scrap);

        ArgumentCaptor<Calender> captor = ArgumentCaptor.forClass(Calender.class);
        verify(calenderRepository).save(captor.capture());

        Calender savedCalender = captor.getValue();
        assertEquals("Developer", savedCalender.getMemoContent());
        assertEquals("2024-09-10", savedCalender.getMemoDate());
        assertEquals(scrap.getMember(), savedCalender.getMember());
    }

    //스크랩 삭제될 시 연관된 캘린더 삭제
    @Test
    void testDeleteCalenderByScrap() {
        Scrap scrap = new Scrap();
        scrap.setId(1L);

        Calender calender1 = new Calender();
        calender1.setScrap(scrap);

        Calender calender2 = new Calender();
        calender2.setScrap(scrap);

        when(calenderRepository.findByScrap(scrap)).thenReturn(Arrays.asList(calender1, calender2));

        calenderService.deleteCalenderByScrap(scrap);

        verify(calenderRepository, times(1)).delete(calender1);
        verify(calenderRepository, times(1)).delete(calender2);
    }

}
