package chugpuff.chugpuff.service;

import chugpuff.chugpuff.entity.JobCode;
import chugpuff.chugpuff.entity.LocationCode;
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
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
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

}

