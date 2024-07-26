package chugpuff.chugpuff.service;

import chugpuff.chugpuff.domain.Member;
import chugpuff.chugpuff.entity.JobCode;
import chugpuff.chugpuff.entity.LocationCode;
import chugpuff.chugpuff.repository.JobCodeRepository;
import chugpuff.chugpuff.repository.LocationCodeRepository;
import chugpuff.chugpuff.repository.MemberRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Optional;
import java.util.logging.Logger;

@Service
public class JobPostingService {

    private final RestTemplate restTemplate = new RestTemplate();
    private static final String API_URL = "https://oapi.saramin.co.kr/job-search";
    private static final Logger logger = Logger.getLogger(JobPostingService.class.getName());

    @Value("${saramin.access-key}")
    private String accessKey;

    @Autowired
    private LocationCodeRepository locationCodeRepository;

    @Autowired
    private JobCodeRepository jobCodeRepository;

    @Autowired
    private MemberRepository memberRepository;

    //공고 조회 및 필터링
    public String getJobPostings(String regionName, String jobMidName, String jobName) {
        UriComponentsBuilder builder = UriComponentsBuilder.fromHttpUrl(API_URL)
                .queryParam("access-key", accessKey);

        List<LocationCode> locationCodes = locationCodeRepository.findByRegionName(regionName);

        if (locationCodes != null && !locationCodes.isEmpty()) {
            for (LocationCode locationCode : locationCodes) {
                builder.queryParam("loc_cd", locationCode.getLocCd());
            }
        }

        List<JobCode> jobCodes = jobCodeRepository.findByJobMidName(jobMidName);

        for (JobCode jobCode : jobCodes) {
            builder.queryParam("job_mid_cd", jobCode.getJobMidCd())
                    .queryParam("job_cd", jobCode.getJobCd());
        }

        String url = builder.toUriString();

        return restTemplate.getForObject(url, String.class);
    }

    //키워드 검색
    public String getJobPostingsByKeywords(String keywords) {
        String encodedKeywords;
        try {
            encodedKeywords = URLEncoder.encode(keywords, StandardCharsets.UTF_8.toString());
        } catch (UnsupportedEncodingException e) {
            logger.severe("Error encoding keywords: " + e.getMessage());
            return null;
        }

        UriComponentsBuilder builder = UriComponentsBuilder.fromHttpUrl(API_URL)
                .queryParam("access-key", accessKey)
                .queryParam("keywords", encodedKeywords)
                .queryParam("count", 1000);

        URI uri = builder.build(true).toUri();
        logger.info("Request URL: " + uri.toString());

        ResponseEntity<String> response = restTemplate.getForEntity(uri, String.class);
        logger.info("Response Status Code: " + response.getStatusCode());

        return response.getBody();
    }

    //특정 공고 조회
    public String getJobDetails(String jobId) {
        UriComponentsBuilder builder = UriComponentsBuilder.fromHttpUrl(API_URL)
                .queryParam("access-key", accessKey)
                .queryParam("id", jobId);

        URI uri = builder.build(true).toUri();

        logger.info("Request URL for job details: " + uri.toString());

        ResponseEntity<String> response = restTemplate.getForEntity(uri, String.class);
        logger.info("Response Status Code: " + response.getStatusCode());

        return response.getBody();
    }

    // 회원 맞춤 공고 조회
    public String getRecommendedJobPostingsForMember(String memberId) {
        Optional<Member> optionalMember = memberRepository.findById(memberId);

        if (optionalMember.isEmpty()) {
            throw new IllegalArgumentException("Member not found with id: " + memberId);
        }

        Member member = optionalMember.get();
        String job = member.getJob();
        String jobKeyword = member.getJobKeyword();

        UriComponentsBuilder builder = UriComponentsBuilder.fromHttpUrl(API_URL)
                .queryParam("access-key", accessKey);

        if (job != null && !job.isEmpty()) {
            List<JobCode> jobCodes = jobCodeRepository.findByJobMidName(job);
            for (JobCode jobCode : jobCodes) {
                builder.queryParam("job_mid_cd", jobCode.getJobMidCd());
            }
        }

        if (jobKeyword != null && !jobKeyword.isEmpty()) {
            JobCode jobCode = jobCodeRepository.findByJobName(jobKeyword);
            if (jobCode != null) {
                builder.queryParam("job_cd", jobCode.getJobCd());
            }
        }

        String url = builder.toUriString();
        return restTemplate.getForObject(url, String.class);
    }
}

