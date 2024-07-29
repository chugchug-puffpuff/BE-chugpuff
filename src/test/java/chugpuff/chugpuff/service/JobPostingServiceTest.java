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
import org.springframework.web.util.UriComponentsBuilder;

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

    private static final String API_URL = "http://api.example.com";

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
        String expectedResponse = "{\"jobs\":{\"count\":1,\"start\":0,\"total\":\"1\",\"job\":[{\"url\":\"http://www.saramin.co.kr/zf_user/jobs/relay/view?rec_idx=48698146&utm_source=job-search-api&utm_medium=api&utm_campaign=saramin-job-search-api\",\"active\":1,\"company\":{\"detail\":{\"href\":\"http://www.saramin.co.kr/zf_user/company-info/view?csn=1209133458&utm_source=job-search-api&utm_medium=api&utm_campaign=saramin-job-search-api\",\"name\":\"한스컨설팅\"}},\"position\":{\"title\":\"송파구 문정동 ERP프로세스 자산화기업 웹프론트 UI개발 대리급\",\"industry\":{\"code\":\"111\",\"name\":\"시설관리·경비·용역\"},\"location\":{\"code\":\"101180\",\"name\":\"서울 &gt; 송파구\"},\"job-type\":{\"code\":\"1\",\"name\":\"정규직\"},\"job-mid-code\":{\"code\":\"2\",\"name\":\"IT개발·데이터\"},\"job-code\":{\"code\":\"92,149,168,235,118,149,168\",\"name\":\"헤드헌팅,프론트엔드,ERP,SAP,Java,솔루션,그룹웨어,자바,프론트\"},\"experience-level\":{\"code\":2,\"min\":3,\"max\":7,\"name\":\"경력 3~7년\"},\"required-education-level\":{\"code\":\"8\",\"name\":\"대학교졸업(4년)이상\"}},\"keyword\":\"헤드헌팅\",\"salary\":{\"code\":\"17\",\"name\":\"연봉 4,500만원\"},\"id\":\"48698146\",\"posting-timestamp\":\"1722169408\",\"modification-timestamp\":\"1722169408\",\"opening-timestamp\":\"1722168000\",\"expiration-timestamp\":\"1988118000\",\"close-type\":{\"code\":\"2\",\"name\":\"채용시\"}}]}}";

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
        member.setJobKeyword("SpringBoot");

        JobCode jobCode = new JobCode();
        jobCode.setJobName("SpringBoot");
        jobCode.setJobCd("292");

        String expectedUrl = UriComponentsBuilder.fromHttpUrl(API_URL)
                .queryParam("access-key", "dummyAccessKey")
                .queryParam("job_cd", "292")
                .toUriString();

        String expectedResponse = "{\"jobs\":{\"count\":10,\"start\":0,\"total\":\"751\",\"job\":[{\"url\":\"http://www.saramin.co.kr/zf_user/jobs/relay/view?rec_idx=48703284&utm_source=job-search-api&utm_medium=api&utm_campaign=saramin-job-search-api\",\"active\":1,\"company\":{\"detail\":{\"href\":\"http://www.saramin.co.kr/zf_user/company-info/view?csn=7808700034&utm_source=job-search-api&utm_medium=api&utm_campaign=saramin-job-search-api\",\"name\":\"메가스터디교육(주)\"}},\"position\":{\"title\":\"러셀 웹개발자 채용\",\"industry\":{\"code\":\"604\",\"name\":\"교재·학습지\"},\"location\":{\"code\":\"101000,101150\",\"name\":\"서울 &gt; 서울전체,서울 &gt; 서초구\"},\"job-type\":{\"code\":\"1\",\"name\":\"정규직\"},\"job-mid-code\":{\"code\":\"2\",\"name\":\"IT개발·데이터\"},\"job-code\":{\"code\":\"84,87,91,92,95,199,235,255,257,291,292,293,110,199,235,255,257,291,293\",\"name\":\"통신교육,백엔드/서버개발,웹개발,퍼블리셔,프론트엔드,DBA,ASP,Java,MSSQL,MySQL,Spring,SpringBoot,SQL,모델링\"},\"experience-level\":{\"code\":2,\"min\":3,\"max\":8,\"name\":\"경력 3~8년\"},\"required-education-level\":{\"code\":\"0\",\"name\":\"학력무관\"}},\"keyword\":\"통신교육\",\"salary\":{\"code\":\"0\",\"name\":\"회사내규에 따름\"},\"id\":\"48703284\",\"posting-timestamp\":\"1722227008\",\"modification-timestamp\":\"1722227008\",\"opening-timestamp\":\"1722178800\",\"expiration-timestamp\":\"1723388399\",\"close-type\":{\"code\":\"1\",\"name\":\"접수마감일\"}},{\"url\":\"http://www.saramin.co.kr/zf_user/jobs/relay/view?rec_idx=48701944&utm_source=job-search-api&utm_medium=api&utm_campaign=saramin-job-search-api\",\"active\":1,\"company\":{\"detail\":{\"href\":\"http://www.saramin.co.kr/zf_user/company-info/view?csn=1148682265&utm_source=job-search-api&utm_medium=api&utm_campaign=saramin-job-search-api\",\"name\":\"알서포트(주)\"}},\"position\":{\"title\":\"[코스닥 상장사] 웹 백엔드 주니어 개발자 채용\",\"industry\":{\"code\":\"301\",\"name\":\"솔루션·SI·ERP·CRM\"},\"location\":{\"code\":\"101000,101020,102000\",\"name\":\"서울 &gt; 서울전체,서울 &gt; 강동구,경기 &gt; 경기전체\"},\"job-type\":{\"code\":\"1\",\"name\":\"정규직\"},\"job-mid-code\":{\"code\":\"2\",\"name\":\"IT개발·데이터\"},\"job-code\":{\"code\":\"84,87,113,118,124,144,166,167,178,184,199,200,235,238,243,250,256,257,263,282,284,286,291,292,293,304,118,167,235,238,276,284,291\",\"name\":\"소프트웨어개발,백엔드/서버개발,웹개발,반응형웹,솔루션,웹표준·웹접근성,AR(증강현실),RTOS,S/W,VR(가상현실),.NET,ASP,ASP.NET,Java,JPA,Kotlin,MariaDB,MyBatis,MySQL,OracleDB,RestAPI,SaaS,Scala,Spring,SpringBoot,SQL,Unity,R\"},\"experience-level\":{\"code\":2,\"min\":1,\"max\":3,\"name\":\"경력 1~3년\"},\"required-education-level\":{\"code\":\"0\",\"name\":\"학력무관\"}},\"keyword\":\"소프트웨어개발\",\"salary\":{\"code\":\"99\",\"name\":\"면접후 결정\"},\"id\":\"48701944\",\"posting-timestamp\":\"1722219483\",\"modification-timestamp\":\"1722219483\",\"opening-timestamp\":\"1722218400\",\"expiration-timestamp\":\"1723417200\",\"close-type\":{\"code\":\"1\",\"name\":\"접수마감일\"}},{\"url\":\"http://www.saramin.co.kr/zf_user/jobs/relay/view?rec_idx=48701739&utm_source=job-search-api&utm_medium=api&utm_campaign=saramin-job-search-api\",\"active\":1,\"company\":{\"detail\":{\"href\":\"http://www.saramin.co.kr/zf_user/company-info/view?csn=8816400361&utm_source=job-search-api&utm_medium=api&utm_campaign=saramin-job-search-api\",\"name\":\"프로소프트\"}},\"position\":{\"title\":\"웹 개발자 신입사원 모집\",\"industry\":{\"code\":\"301\",\"name\":\"솔루션·SI·ERP·CRM\"},\"location\":{\"code\":\"110000,110040,110120,110150\",\"name\":\"경남 &gt; 경남전체,경남 &gt; 김해시,경남 &gt; 진주시,경남 &gt; 창원시\"},\"job-type\":{\"code\":\"1\",\"name\":\"정규직\"},\"job-mid-code\":{\"code\":\"2\",\"name\":\"IT개발·데이터\"},\"job-code\":{\"code\":\"84,87,92,216,220,222,235,236,240,250,277,279,282,291,292,302,312,89,118,277,292\",\"name\":\"SI·시스템통합,SM,소프트웨어개발,백엔드/서버개발,웹개발,프론트엔드,ECMAScript,Flutter,Git,Java,Javascript,JSP,MariaDB,React,ReactJS,RestAPI,Spring,SpringBoot,TypeScript,Vue.js,유지보수,솔루션\"},\"experience-level\":{\"code\":0,\"min\":0,\"max\":0,\"name\":\"경력무관\"},\"required-education-level\":{\"code\":\"0\",\"name\":\"학력무관\"}},\"keyword\":\"SI·시스템통합,SM,소프트웨어개발\",\"salary\":{\"code\":\"99\",\"name\":\"면접후 결정\"},\"id\":\"48701739\",\"posting-timestamp\":\"1722218821\",\"modification-timestamp\":\"1722218821\",\"opening-timestamp\":\"1722218400\",\"expiration-timestamp\":\"1724857199\",\"close-type\":{\"code\":\"1\",\"name\":\"접수마감일\"}},{\"url\":\"http://www.saramin.co.kr/zf_user/jobs/relay/view?rec_idx=48701591&utm_source=job-search-api&utm_medium=api&utm_campaign=saramin-job-search-api\",\"active\":1,\"company\":{\"detail\":{\"href\":\"http://www.saramin.co.kr/zf_user/company-info/view?csn=2208811661&utm_source=job-search-api&utm_medium=api&utm_campaign=saramin-job-search-api\",\"name\":\"(주)핀커스코리아\"}},\"position\":{\"title\":\"[TOP 서빙 로봇] SW개발\",\"industry\":{\"code\":\"111\",\"name\":\"시설관리·경비·용역\"},\"location\":{\"code\":\"101010\",\"name\":\"서울 &gt; 강남구\"},\"job-type\":{\"code\":\"1\",\"name\":\"정규직\"},\"job-mid-code\":{\"code\":\"2\",\"name\":\"IT개발·데이터\"},\"job-code\":{\"code\":\"84,92,142,164,2250,184,201,204,205,214,222,235,237,257,270,292\",\"name\":\"헤드헌팅,백엔드/서버개발,프론트엔드,API,RDBMS,HTTP,.NET,AWS,C#,C++,Docker,Git,Java,Jenkins,MySQL,PostgreSQL,SpringBoot\"},\"experience-level\":{\"code\":2,\"min\":3,\"max\":0,\"name\":\"경력3년↑\"},\"required-education-level\":{\"code\":\"8\",\"name\":\"대학교졸업(4년)이상\"}},\"keyword\":\"헤드헌팅\",\"salary\":{\"code\":\"99\",\"name\":\"면접후 결정\"},\"id\":\"48701591\",\"posting-timestamp\":\"1722218340\",\"modification-timestamp\":\"1722218340\",\"opening-timestamp\":\"1722214800\",\"expiration-timestamp\":\"1724857199\",\"close-type\":{\"code\":\"1\",\"name\":\"접수마감일\"}},{\"url\":\"http://www.saramin.co.kr/zf_user/jobs/relay/view?rec_idx=48700572&utm_source=job-search-api&utm_medium=api&utm_campaign=saramin-job-search-api\",\"active\":1,\"company\":{\"detail\":{\"href\":\"http://www.saramin.co.kr/zf_user/company-info/view?csn=1208773754&utm_source=job-search-api&utm_medium=api&utm_campaign=saramin-job-search-api\",\"name\":\"(주)지란지교에스앤씨\"}},\"position\":{\"title\":\"웹 개발자 경력 모집 (JAVA, JSP, Spring)\",\"industry\":{\"code\":\"301\",\"name\":\"솔루션·SI·ERP·CRM\"},\"location\":{\"code\":\"101180\",\"name\":\"서울 &gt; 송파구\"},\"job-type\":{\"code\":\"2,9\",\"name\":\"계약직,프리랜서\"},\"job-mid-code\":{\"code\":\"2\",\"name\":\"IT개발·데이터\"},\"job-code\":{\"code\":\"87,235,292,87,130,203,235,235,240,240,291,291,312\",\"name\":\"DBMS,SI·시스템통합,소프트웨어개발,솔루션업체,웹개발,Java,SpringBoot,정보통신,Bootstrap,JSP,Spring,Vue.js\"},\"experience-level\":{\"code\":2,\"min\":5,\"max\":0,\"name\":\"경력5년↑\"},\"required-education-level\":{\"code\":\"0\",\"name\":\"학력무관\"}},\"keyword\":\"DBMS,SI·시스템통합,소프트웨어개발,솔루션업체\",\"salary\":{\"code\":\"99\",\"name\":\"면접후 결정\"},\"id\":\"48700572\",\"posting-timestamp\":\"1722215418\",\"modification-timestamp\":\"1722215485\",\"opening-timestamp\":\"1722214800\",\"expiration-timestamp\":\"1724857199\",\"close-type\":{\"code\":\"1\",\"name\":\"접수마감일\"}},{\"url\":\"http://www.saramin.co.kr/zf_user/jobs/relay/view?rec_idx=48700463&utm_source=job-search-api&utm_medium=api&utm_campaign=saramin-job-search-api\",\"active\":1,\"company\":{\"detail\":{\"href\":\"http://www.saramin.co.kr/zf_user/company-info/view?csn=7808700034&utm_source=job-search-api&utm_medium=api&utm_campaign=saramin-job-search-api\",\"name\":\"메가스터디교육(주)\"}},\"position\":{\"title\":\"서비스 플랫폼 개발자 채용\",\"industry\":{\"code\":\"602\",\"name\":\"학원·어학원\"},\"location\":{\"code\":\"101150\",\"name\":\"서울 &gt; 서초구\"},\"job-type\":{\"code\":\"1\",\"name\":\"정규직\"},\"job-mid-code\":{\"code\":\"2\",\"name\":\"IT개발·데이터\"},\"job-code\":{\"code\":\"84,86,87,88,92,199,200,235,236,250,255,257,292,2232,89,200,235,250,257,291,293\",\"name\":\"입시학원,백엔드/서버개발,앱개발,웹개발,웹마스터,프론트엔드,ASP,ASP.NET,Java,Javascript,MariaDB,MSSQL,MySQL,SpringBoot,풀스택,유지보수,Spring,SQL\"},\"experience-level\":{\"code\":2,\"min\":2,\"max\":0,\"name\":\"경력2년↑\"},\"required-education-level\":{\"code\":\"0\",\"name\":\"학력무관\"}},\"keyword\":\"입시학원\",\"salary\":{\"code\":\"99\",\"name\":\"면접후 결정\"},\"id\":\"48700463\",\"posting-timestamp\":\"1722215098\",\"modification-timestamp\":\"1722215098\",\"opening-timestamp\":\"1722214800\",\"expiration-timestamp\":\"1723388399\",\"close-type\":{\"code\":\"1\",\"name\":\"접수마감일\"}},{\"url\":\"http://www.saramin.co.kr/zf_user/jobs/relay/view?rec_idx=48700312&utm_source=job-search-api&utm_medium=api&utm_campaign=saramin-job-search-api\",\"active\":1,\"company\":{\"detail\":{\"href\":\"http://www.saramin.co.kr/zf_user/company-info/view?csn=4460020117&utm_source=job-search-api&utm_medium=api&utm_campaign=saramin-job-search-api\",\"name\":\"SEAGILE주식회사\"}},\"position\":{\"title\":\"[일본 동경 시마네현] IT 프로그래머web개발 엔지니어\",\"industry\":{\"code\":\"301\",\"name\":\"솔루션·SI·ERP·CRM\"},\"location\":{\"code\":\"211223\",\"name\":\"일본 &gt; 시마네\"},\"job-type\":{\"code\":\"1,9\",\"name\":\"정규직,프리랜서\"},\"job-mid-code\":{\"code\":\"2\",\"name\":\"IT개발·데이터\"},\"job-code\":{\"code\":\"87,122,159,194,215,235,238,257,258,268,272,291,292,294,297,318,89,182,204,205,235,236,239,246,257,268,291\",\"name\":\"SI·시스템통합,소프트웨어개발,솔루션업체,시스템관리,웹개발,알고리즘,Nginx,Ajax,Eclipse,Java,JPA,MySQL,Node.js,PHP,Python,Spring,SpringBoot,SQLite,SVN,XML,유지보수,Windows,C#,C++,Javascript,jQuery,Linux\"},\"experience-level\":{\"code\":2,\"min\":1,\"max\":0,\"name\":\"경력1년↑\"},\"required-education-level\":{\"code\":\"7\",\"name\":\"대학졸업(2,3년)이상\"}},\"keyword\":\"SI·시스템통합,소프트웨어개발,솔루션업체,시스템관리\",\"salary\":{\"code\":\"15\",\"name\":\"연봉 3,600만원\"},\"id\":\"48700312\",\"posting-timestamp\":\"1722214681\",\"modification-timestamp\":\"1722214681\",\"opening-timestamp\":\"1722211200\",\"expiration-timestamp\":\"1724857199\",\"close-type\":{\"code\":\"1\",\"name\":\"접수마감일\"}},{\"url\":\"http://www.saramin.co.kr/zf_user/jobs/relay/view?rec_idx=48699852&utm_source=job-search-api&utm_medium=api&utm_campaign=saramin-job-search-api\",\"active\":1,\"company\":{\"detail\":{\"href\":\"http://www.saramin.co.kr/zf_user/company-info/view?csn=1848100063&utm_source=job-search-api&utm_medium=api&utm_campaign=saramin-job-search-api\",\"name\":\"(주)어니스트에이아이\"}},\"position\":{\"title\":\"백엔드 엔지니어\",\"industry\":{\"code\":\"401\",\"name\":\"은행·금융·저축\"},\"location\":{\"code\":\"101000,101010\",\"name\":\"서울 &gt; 서울전체,서울 &gt; 강남구\"},\"job-type\":{\"code\":\"1\",\"name\":\"정규직\"},\"job-mid-code\":{\"code\":\"2\",\"name\":\"IT개발·데이터\"},\"job-code\":{\"code\":\"84,87,95,243,257,291,292,293,243,257,291\",\"name\":\"금융기관,백엔드/서버개발,웹개발,DBA,Kotlin,MySQL,Spring,SpringBoot,SQL\"},\"experience-level\":{\"code\":2,\"min\":3,\"max\":8,\"name\":\"경력 3~8년\"},\"required-education-level\":{\"code\":\"0\",\"name\":\"학력무관\"}},\"keyword\":\"금융기관\",\"salary\":{\"code\":\"0\",\"name\":\"회사내규에 따름\"},\"id\":\"48699852\",\"posting-timestamp\":\"1722213349\",\"modification-timestamp\":\"1722213349\",\"opening-timestamp\":\"1722178800\",\"expiration-timestamp\":\"1724770799\",\"close-type\":{\"code\":\"1\",\"name\":\"접수마감일\"}},{\"url\":\"http://www.saramin.co.kr/zf_user/jobs/relay/view?rec_idx=48699799&utm_source=job-search-api&utm_medium=api&utm_campaign=saramin-job-search-api\",\"active\":1,\"company\":{\"detail\":{\"href\":\"http://www.saramin.co.kr/zf_user/company-info/view?csn=6338802956&utm_source=job-search-api&utm_medium=api&utm_campaign=saramin-job-search-api\",\"name\":\"디에이치엔주식회사\"}},\"position\":{\"title\":\"영어 가능 개발 PM\",\"industry\":{\"code\":\"301\",\"name\":\"솔루션·SI·ERP·CRM\"},\"location\":{\"code\":\"104000,104080\",\"name\":\"대구 &gt; 대구전체,대구 &gt; 중구\"},\"job-type\":{\"code\":\"1\",\"name\":\"정규직\"},\"job-mid-code\":{\"code\":\"2,16\",\"name\":\"IT개발·데이터,기획·전략\"},\"job-code\":{\"code\":\"84,87,91,92,127,201,216,235,236,238,277,279,291,292,302,1648,1649,1650,1651,89,92,104,118,201,235,238,277,291,302\",\"name\":\"CRM,백엔드/서버개발,웹개발,퍼블리셔,프론트엔드,인프라,AWS,ECMAScript,Java,Javascript,JPA,React,ReactJS,Spring,SpringBoot,TypeScript,PL(프로젝트리더),PM(프로젝트매니저),PMO,PO(프로덕트오너),유지보수,네트워크,솔루션\"},\"experience-level\":{\"code\":2,\"min\":5,\"max\":10,\"name\":\"경력 5~10년\"},\"required-education-level\":{\"code\":\"8\",\"name\":\"대학교졸업(4년)이상\"}},\"keyword\":\"CRM\",\"salary\":{\"code\":\"0\",\"name\":\"회사내규에 따름\"},\"id\":\"48699799\",\"posting-timestamp\":\"1722213186\",\"modification-timestamp\":\"1722213186\",\"opening-timestamp\":\"1722178800\",\"expiration-timestamp\":\"1724770799\",\"close-type\":{\"code\":\"1\",\"name\":\"접수마감일\"}},{\"url\":\"http://www.saramin.co.kr/zf_user/jobs/relay/view?rec_idx=48699795&utm_source=job-search-api&utm_medium=api&utm_campaign=saramin-job-search-api\",\"active\":1,\"company\":{\"detail\":{\"href\":\"http://www.saramin.co.kr/zf_user/company-info/view?csn=6338802956&utm_source=job-search-api&utm_medium=api&utm_campaign=saramin-job-search-api\",\"name\":\"디에이치엔주식회사\"}},\"position\":{\"title\":\"프론트/백엔드 개발자, AWS서버관리자\",\"industry\":{\"code\":\"301\",\"name\":\"솔루션·SI·ERP·CRM\"},\"location\":{\"code\":\"104000,104080\",\"name\":\"대구 &gt; 대구전체,대구 &gt; 중구\"},\"job-type\":{\"code\":\"1\",\"name\":\"정규직\"},\"job-mid-code\":{\"code\":\"2\",\"name\":\"IT개발·데이터\"},\"job-code\":{\"code\":\"84,87,91,92,100,127,146,201,216,235,236,238,277,279,291,292,302,89,92,104,118,201,235,238,277,291,302,2232\",\"name\":\"CRM,백엔드/서버개발,웹개발,퍼블리셔,프론트엔드,SE(시스템엔지니어),인프라,DevOps,AWS,ECMAScript,Java,Javascript,JPA,React,ReactJS,Spring,SpringBoot,TypeScript,유지보수,네트워크,솔루션,풀스택\"},\"experience-level\":{\"code\":2,\"min\":3,\"max\":5,\"name\":\"경력 3~5년\"},\"required-education-level\":{\"code\":\"8\",\"name\":\"대학교졸업(4년)이상\"}},\"keyword\":\"CRM\",\"salary\":{\"code\":\"0\",\"name\":\"회사내규에 따름\"},\"id\":\"48699795\",\"posting-timestamp\":\"1722213171\",\"modification-timestamp\":\"1722213171\",\"opening-timestamp\":\"1722178800\",\"expiration-timestamp\":\"1724770799\",\"close-type\":{\"code\":\"1\",\"name\":\"접수마감일\"}}]}}";

        when(memberRepository.findById(memberId)).thenReturn(Optional.of(member));
        when(jobCodeRepository.findByJobName("SpringBoot")).thenReturn(jobCode);
        when(restTemplate.getForObject(expectedUrl, String.class)).thenReturn(expectedResponse);

        String result = jobPostingService.getRecommendedJobPostingsForMember(memberId);

        assertEquals(expectedResponse, result);
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

