package chugpuff.chugpuff.controller;

import chugpuff.chugpuff.entity.JobPostingComment;
import chugpuff.chugpuff.service.CustomUserDetails;
import chugpuff.chugpuff.service.JobPostingService;
import chugpuff.chugpuff.service.MemberService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.core.Authentication;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.Arrays;
import java.util.List;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@ActiveProfiles("test")
public class JobPostingControllerTest {

    private MockMvc mockMvc;

    @Mock
    private JobPostingService jobPostingService;

    @Mock
    private MemberService memberService;

    @Mock
    private Authentication authentication;

    @InjectMocks
    private JobPostingController jobPostingController;

    @BeforeEach
    public void setup() {
        MockitoAnnotations.openMocks(this);
        mockMvc = MockMvcBuilders.standaloneSetup(jobPostingController).build();
    }

    @Test
    public void testGetJobPostingsSortedByScrapCount() throws Exception {
        List<String> sortedJobDetails = Arrays.asList("Job details for job1", "Job details for job2");
        when(jobPostingService.getJobPostingsSortedByScrapCount()).thenReturn(sortedJobDetails);

        mockMvc.perform(get("/api/job-postings")
                        .param("sortBy", "scrap-count"))
                .andExpect(status().isOk())
                .andExpect(content().string(String.join(", ", sortedJobDetails)));
    }

    @Test
    public void testGetJobScrapCount() throws Exception {
        String jobId = "job1";
        Long scrapCount = 10L;
        when(jobPostingService.getJobScrapCount(jobId)).thenReturn(scrapCount);

        mockMvc.perform(get("/api/job-postings/" + jobId + "/scrap-count"))
                .andExpect(status().isOk())
                .andExpect(content().string(scrapCount.toString()));
    }

    // Other tests for other endpoints in JobPostingController
}
