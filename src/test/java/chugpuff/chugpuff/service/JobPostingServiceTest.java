package chugpuff.chugpuff.service;

import chugpuff.chugpuff.domain.Member;
import chugpuff.chugpuff.entity.JobCode;
import chugpuff.chugpuff.entity.JobPostingComment;
import chugpuff.chugpuff.entity.LocationCode;
import chugpuff.chugpuff.entity.Scrap;
import chugpuff.chugpuff.repository.*;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.json.JSONObject;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.skyscreamer.jsonassert.JSONAssert;
import org.skyscreamer.jsonassert.JSONCompareMode;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.TestPropertySource;
import org.springframework.util.ReflectionUtils;
import org.springframework.web.client.RestTemplate;

import java.lang.reflect.Field;
import java.net.URI;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@SpringBootTest
@TestPropertySource(locations = "classpath:application-test.properties")
public class JobPostingServiceTest {

    @Mock
    private RestTemplate restTemplate;

    @MockBean
    private LocationCodeRepository locationCodeRepository;

    @MockBean
    private JobCodeRepository jobCodeRepository;

    @InjectMocks
    private JobPostingService jobPostingService;

    private ObjectMapper objectMapper = new ObjectMapper();

    @Mock
    private MemberRepository memberRepository;

    @Mock
    private MemberService memberService;

    @Mock
    private ScrapRepository scrapRepository;

    @Mock
    private JobPostingCommentRepository jobPostingCommentRepository;

    @Value("${saramin.access-key}")
    private String accessKey;

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this);

        // Access key를 Reflection을 이용하여 설정
        Field accessKeyField = ReflectionUtils.findField(JobPostingService.class, "accessKey");
        ReflectionUtils.makeAccessible(accessKeyField);
        ReflectionUtils.setField(accessKeyField, jobPostingService, "fXUtujznPIRqfBsGXXSxoeD2eOgUZx99aR7OMBW1b43WIasHMZFI");
    }

    @Test
    public void testGetJobPostings() {
        String regionName = "서울";
        String jobMidName = "개발";
        String jobName = "Java";
        String sortBy = "scrap-count";

        // LocationCode 객체 생성
        LocationCode locationCode = new LocationCode();
        locationCode.setRegionName(regionName);
        locationCode.setLocCd("101000");

        // JobCode 객체 생성
        JobCode jobCode = new JobCode();
        jobCode.setJobMidName(jobMidName);
        jobCode.setJobMidCd("02");
        jobCode.setJobCd("235");

        List<LocationCode> locationCodes = Arrays.asList(locationCode);
        List<JobCode> jobCodes = Arrays.asList(jobCode);

        when(locationCodeRepository.findByRegionName(regionName)).thenReturn(locationCodes);
        when(jobCodeRepository.findByJobMidName(jobMidName)).thenReturn(jobCodes);

        String expectedResponse = "{\"jobs\":{\"count\":10,\"start\":0,\"total\":\"1824\",\"job\":[{\"url\":\"http://www.saramin.co.kr/zf_user/jobs/relay/view?rec_idx=48698123&utm_source=job-search-api&utm_medium=api&utm_campaign=saramin-job-search-api\",\"active\":1,\"company\":{\"detail\":{\"href\":\"http://www.saramin.co.kr/company/detail?com_idx=12345\"}},\"id\":\"48698146\"}]}}";
        when(restTemplate.getForObject(anyString(), eq(String.class))).thenReturn(expectedResponse);

        String actualResponse = jobPostingService.getJobPostings(regionName, jobMidName, jobName, sortBy);

        // JSON 객체로 변환
        JSONObject expectedJson = new JSONObject(expectedResponse);
        JSONObject actualJson = new JSONObject(actualResponse);

        // 필요한 필드 비교
        assertEquals(expectedJson.getJSONObject("jobs").getString("total"), actualJson.getJSONObject("jobs").getString("total"));

        // 제일 처음 게시글의 id 필드 비교
        String expectedJobId = expectedJson.getJSONObject("jobs").getJSONArray("job").getJSONObject(0).getString("id");
        String actualJobId = actualJson.getJSONObject("jobs").getJSONArray("job").getJSONObject(0).getString("id");

        assertEquals(expectedJobId, actualJobId);
    }

    @Test
    public void testGetJobPostingsByKeywords() throws Exception {
        // RestTemplate 모의 응답 설정
        String mockedResponse = "{\"jobs\":{\"count\":1,\"start\":0,\"total\":\"19652\",\"job\":[{\"url\":\"http://www.saramin.co.kr/zf_user/jobs/relay/view?rec_idx=48641635&utm_source=job-search-api&utm_medium=api&utm_campaign=saramin-job-search-api\",\"active\":1,\"company\":{\"detail\":{\"href\":\"http://www.saramin.co.kr/zf_user/company-info/view?csn=5508603146&utm_source=job-search-api&utm_medium=api&utm_campaign=saramin-job-search-api\",\"name\":\"주식회사 곽본\"}},\"position\":{\"title\":\"뮤직펍 사운드랩 매장관리자 채용, 주 5-6일\",\"industry\":{\"code\":\"109\",\"name\":\"외식업·식음료\"},\"location\":{\"code\":\"101180\",\"name\":\"서울 &gt; 송파구\"},\"job-type\":{\"code\":\"1\",\"name\":\"정규직\"},\"job-mid-code\":{\"code\":\"8,10\",\"name\":\"영업·판매·무역,서비스\"},\"job-code\":{\"code\":\"756,767,871,875,876,985,2202\",\"name\":\"식품·푸드,음식료,프랜차이즈,가맹점관리,매장관리,매장매니저,바리스타,바텐더,카페\"},\"experience-level\":{\"code\":0,\"min\":0,\"max\":0,\"name\":\"경력무관\"},\"required-education-level\":{\"code\":\"0\",\"name\":\"학력무관\"}},\"keyword\":\"식품·푸드,음식료,프랜차이즈\",\"salary\":{\"code\":\"17\",\"name\":\"연봉 4,400만원\"},\"id\":\"48641635\",\"posting-timestamp\":\"1721308490\",\"modification-timestamp\":\"1721308908\",\"opening-timestamp\":\"1721307600\",\"expiration-timestamp\":\"1723906799\",\"close-type\":{\"code\":\"1\",\"name\":\"접수마감일\"}}]}}";
        when(restTemplate.getForEntity(any(String.class), any(Class.class)))
                .thenReturn(new ResponseEntity<>(mockedResponse, HttpStatus.OK));

        // 테스트 실행
        String result = jobPostingService.getJobPostingsByKeywords("마케팅", "posting-date");

        // JSON 비교
        JsonNode expectedJson = objectMapper.readTree(mockedResponse);
        JsonNode actualJson = objectMapper.readTree(result);

        // 필요한 필드만 비교
        assertEquals(expectedJson.get("jobs").get("total"), actualJson.get("jobs").get("total"));
    }

    @Test
    public void testGetJobPostingsSortedByScrapCount() {
        Object[] jobIdWithCount1 = new Object[]{"job1", 10L};
        Object[] jobIdWithCount2 = new Object[]{"job2", 5L};
        when(scrapRepository.findJobIdsOrderByScrapCount()).thenReturn(Arrays.asList(jobIdWithCount1, jobIdWithCount2));

        // spy를 사용하여 jobPostingService 객체를 감시
        JobPostingService spyJobPostingService = spy(jobPostingService);

        String jobDetails1 = "Job details for job1";
        String jobDetails2 = "Job details for job2";
        doReturn(jobDetails1).when(spyJobPostingService).getJobDetails("job1");
        doReturn(jobDetails2).when(spyJobPostingService).getJobDetails("job2");

        // spyJobPostingService를 사용하여 메서드 호출
        List<String> result = spyJobPostingService.getJobPostingsSortedByScrapCount();

        assertEquals(2, result.size());
        assertEquals(jobDetails1, result.get(0));
        assertEquals(jobDetails2, result.get(1));
    }

    @Test
    public void testGetJobScrapCount() {
        String jobId = "job1";
        when(scrapRepository.countByJobId(jobId)).thenReturn(10L);

        Long count = jobPostingService.getJobScrapCount(jobId);

        assertEquals(10L, count);
    }

    @Test
    public void testGetJobDetails() {
        String jobId = "48698146";
        String expectedResponse = "{\"jobs\":{\"count\":1,\"start\":0,\"total\":\"1\",\"job\":[{\"url\":\"http://www.saramin.co.kr/zf_user/jobs/relay/view?rec_idx=48698146&utm_source=job-search-api&utm_medium=api&utm_campaign=saramin-job-search-api\",\"active\":1,\"company\":{\"detail\":{\"href\":\"http://www ...";

        when(restTemplate.getForEntity(any(URI.class), eq(String.class)))
                .thenReturn(new ResponseEntity<>(expectedResponse, HttpStatus.OK));

        String actualResponse = jobPostingService.getJobDetails(jobId);

        try {

            JsonNode expectedJsonNode = objectMapper.readTree(expectedResponse);
            String expectedId = expectedJsonNode.path("jobs").path("job").get(0).path("id").asText();

            JsonNode actualJsonNode = objectMapper.readTree(actualResponse);
            String actualId = actualJsonNode.path("jobs").path("job").get(0).path("id").asText();

            //id 필드 비교
            assertEquals(expectedId, actualId);
        } catch (Exception e) {
            e.printStackTrace();

            throw new RuntimeException("JSON processing error", e);
        }
    }

    @Test
    public void testGetRecommendedJobPostingsForMember() {
        String memberId = "member1";
        Member member = new Member();
        member.setId(memberId);
        member.setJobKeyword("Java Developer");

        when(memberRepository.findById(memberId)).thenReturn(Optional.of(member));
        when(jobPostingService.getJobPostingsByKeywords(anyString(), anyString()))
                .thenReturn("Job postings for Java Developer");

        String result = jobPostingService.getRecommendedJobPostingsForMember(memberId);

        assertEquals("Job postings for Java Developer", result);
    }

    @Test
    public void testToggleScrap() {
        String memberId = "member1";
        String jobId = "job1";
        Member member = new Member();
        member.setId(memberId);

        when(memberService.getMemberByUsername(memberId)).thenReturn(Optional.of(member));

        Scrap scrap = new Scrap();
        scrap.setJobId(jobId);
        scrap.setMember(member);

        // 첫 번째 케이스: 스크랩이 존재할 때
        when(scrapRepository.findByMemberAndJobId(member, jobId)).thenReturn(Optional.of(scrap));
        doNothing().when(scrapRepository).delete(scrap);

        jobPostingService.toggleScrap(memberId, jobId);

        verify(scrapRepository).delete(scrap);

        // 두 번째 케이스: 스크랩이 존재하지 않을 때
        when(scrapRepository.findByMemberAndJobId(member, jobId)).thenReturn(Optional.empty());
        when(scrapRepository.save(any(Scrap.class))).thenReturn(scrap);

        jobPostingService.toggleScrap(memberId, jobId);

        verify(scrapRepository).save(any(Scrap.class));
    }

    @Test
    public void testGetScrappedJobPostings() {
        String memberId = "member1";
        Member member = new Member();
        member.setId(memberId);

        Scrap scrap1 = new Scrap();
        scrap1.setJobId("job1");

        Scrap scrap2 = new Scrap();
        scrap2.setJobId("job2");

        List<Scrap> scraps = Arrays.asList(scrap1, scrap2);

        when(memberService.getMemberByUsername(memberId)).thenReturn(Optional.of(member));
        when(scrapRepository.findByMember(member)).thenReturn(scraps);

        // Mock API call results
        JobPostingService spyJobPostingService = spy(jobPostingService);
        doReturn("Job details for job1").when(spyJobPostingService).getJobDetails("job1");
        doReturn("Job details for job2").when(spyJobPostingService).getJobDetails("job2");

        List<String> result = spyJobPostingService.getScrappedJobPostings(memberId);

        assertEquals(2, result.size());
        assertEquals("Job details for job1", result.get(0));
        assertEquals("Job details for job2", result.get(1));
    }

    @Test
    public void testAddComment() {
        String jobId = "123";
        String userId = "user1";
        String commentText = "This is a test comment.";

        Member member = new Member();
        member.setId(userId);

        JobPostingComment jobPostingComment = new JobPostingComment();
        jobPostingComment.setJobId(jobId);
        jobPostingComment.setMember(member);
        jobPostingComment.setComment(commentText);
        jobPostingComment.setCreatedAt(LocalDateTime.now());

        when(memberRepository.findById(userId)).thenReturn(Optional.of(member));
        when(jobPostingCommentRepository.save(any(JobPostingComment.class))).thenReturn(jobPostingComment);

        JobPostingComment createdComment = jobPostingService.addComment(jobId, userId, commentText);

        assertNotNull(createdComment);
        assertEquals(jobId, createdComment.getJobId());
        assertEquals(userId, createdComment.getMember().getId());
        assertEquals(commentText, createdComment.getComment());
        assertNotNull(createdComment.getCreatedAt());
    }

    @Test
    public void testUpdateComment() {
        Long commentId = 1L;
        String userId = "user1";
        String newContent = "Updated comment.";

        Member member = new Member();
        member.setId(userId);

        JobPostingComment existingComment = new JobPostingComment();
        existingComment.setId(commentId);
        existingComment.setComment("Original comment.");
        existingComment.setMember(member);

        when(jobPostingCommentRepository.findById(commentId)).thenReturn(Optional.of(existingComment));
        when(memberRepository.findById(userId)).thenReturn(Optional.of(member));
        when(jobPostingCommentRepository.save(any(JobPostingComment.class))).thenReturn(existingComment);

        JobPostingComment updatedComment = jobPostingService.updateComment(commentId, userId, newContent);

        assertNotNull(updatedComment);
        assertEquals(commentId, updatedComment.getId());
        assertEquals(newContent, updatedComment.getComment());
        assertEquals(userId, updatedComment.getMember().getId());
    }

    @Test
    public void testDeleteComment() {
        Long commentId = 1L;
        String userId = "user1";

        Member member = new Member();
        member.setId(userId);

        JobPostingComment existingComment = new JobPostingComment();
        existingComment.setId(commentId);
        existingComment.setMember(member);

        when(jobPostingCommentRepository.findById(commentId)).thenReturn(Optional.of(existingComment));

        jobPostingService.deleteComment(commentId, userId);

        verify(jobPostingCommentRepository, times(1)).delete(existingComment);
    }

}

