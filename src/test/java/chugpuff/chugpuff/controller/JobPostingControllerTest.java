package chugpuff.chugpuff.controller;

import chugpuff.chugpuff.domain.Member;
import chugpuff.chugpuff.entity.JobPostingComment;
import chugpuff.chugpuff.repository.MemberRepository;
import chugpuff.chugpuff.service.CustomUserDetails;
import chugpuff.chugpuff.service.JobPostingService;
import chugpuff.chugpuff.service.MemberService;
import jakarta.transaction.Transactional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockitoAnnotations;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.security.test.context.support.WithUserDetails;
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultMatcher;

import java.util.Arrays;
import java.util.Collections;

import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.content;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
public class JobPostingControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private JobPostingService jobPostingService;

    @MockBean
    private MemberService memberService;

    @Autowired
    private MemberRepository memberRepository;

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this);

        Member member = new Member();
        member.setId("test7");
        member.setPassword("7777");
        member.setEmail("test7@test.com");
        member.setIsAbove15(true);
        member.setJob("IT개발·데이터");
        member.setJobKeyword("풀스택");
        member.setRecordingAccepted(true);
        member.setPrivacyPolicyAccepted(true);
        member.setName("name7");


        memberRepository.save(member);
    }

    @Test
    @WithMockUser(username = "user", roles = {"USER"})
    public void testGetJobPostings() throws Exception {
        when(jobPostingService.getJobPostings(anyString(), anyString(), anyString())).thenReturn("test response");

        mockMvc.perform(get("/api/job-postings")
                        .param("regionName", "서울")
                        .param("jobName", "풀스택")
                        .param("sortBy", "scrap-count"))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(username = "user", roles = {"USER"})
    public void testGetJobPostingsByKeywords() throws Exception {
        String keywords = "Java Developer";
        String regionName = "Seoul";
        String jobName = "Software Engineer";
        String sortBy = "date";

        String mockResponse = "[{\"title\":\"Java Developer\", \"location\":\"Seoul\", \"jobName\":\"Software Engineer\"}]";

        // JobPostingService의 메서드가 네 개의 매개변수를 받도록 호출을 수정
        when(jobPostingService.getJobPostingsByKeywords(keywords, regionName, jobName, sortBy))
                .thenReturn(mockResponse);

        mockMvc.perform(get("/api/job-postings/search")
                        .param("keywords", keywords)
                        .param("regionName", regionName)
                        .param("jobName", jobName)
                        .param("sortBy", sortBy))
                .andExpect(status().isOk())
                .andExpect((ResultMatcher) content().json(mockResponse));

        verify(jobPostingService, times(1)).getJobPostingsByKeywords(keywords, regionName, jobName, sortBy);
    }


    @Test
    @WithMockUser(username = "user", roles = {"USER"})
    public void testGetJobDetails() throws Exception {
        when(jobPostingService.getJobDetails(anyString())).thenReturn("test response");

        mockMvc.perform(get("/api/job-postings/{jobId}", "48698146"))
                .andExpect(status().isOk());
    }

    @Test
    @WithUserDetails(value = "test1", userDetailsServiceBeanName = "customUserDetailsService")
    public void testGetRecommendedJobPostingsForMember() throws Exception {
        // Define behavior for the service
        when(jobPostingService.getRecommendedJobPostingsForMember("test1")).thenReturn("test response");

        // Perform the request
        mockMvc.perform(get("/api/job-postings/recommendations"))
                .andExpect(status().isOk());
    }


    @Test
    @WithUserDetails(value = "test1", userDetailsServiceBeanName = "customUserDetailsService")
    public void testToggleScrap() throws Exception {
        mockMvc.perform(post("/api/job-postings/48698146/scrap"))
                .andExpect(status().isOk());
    }

    @Test
    @WithUserDetails(value = "test1", userDetailsServiceBeanName = "customUserDetailsService")
    public void testGetScrappedJobPostings() throws Exception {
        when(jobPostingService.getScrappedJobPostings(anyString())).thenReturn(Arrays.asList("48698146"));

        mockMvc.perform(get("/api/job-postings/scraps"))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(username = "user", roles = {"USER"})
    public void testGetJobScrapCount() throws Exception {
        when(jobPostingService.getJobScrapCount(anyString())).thenReturn(100L);

        mockMvc.perform(get("/api/job-postings/48698146/scrap-count"))
                .andExpect(status().isOk());
    }

    @Test
    @WithUserDetails(value = "test1", userDetailsServiceBeanName = "customUserDetailsService")
    public void testAddComment() throws Exception {
        JobPostingComment comment = new JobPostingComment();
        comment.setComment("Test comment");

        when(jobPostingService.addComment(anyString(), anyString(), anyString())).thenReturn(comment);

        mockMvc.perform(post("/api/job-postings/48698146/comments")
                        .contentType("application/json")
                        .content("{\"comment\": \"Test comment\"}"))
                .andExpect(status().isOk());
    }

    @Test
    @WithUserDetails(value = "test1", userDetailsServiceBeanName = "customUserDetailsService")
    public void testUpdateComment() throws Exception {
        JobPostingComment updatedComment = new JobPostingComment();
        updatedComment.setComment("Updated comment");

        when(jobPostingService.updateComment(anyLong(), anyString(), anyString())).thenReturn(updatedComment);

        mockMvc.perform(put("/api/job-postings/comments/1")
                        .contentType("application/json")
                        .content("{\"comment\": \"Updated comment\"}"))
                .andExpect(status().isOk());
    }

    @Test
    @WithUserDetails(value = "test1", userDetailsServiceBeanName = "customUserDetailsService")
    public void testDeleteComment() throws Exception {
        mockMvc.perform(delete("/api/job-postings/comments/1"))
                .andExpect(status().isNoContent());
    }
}
