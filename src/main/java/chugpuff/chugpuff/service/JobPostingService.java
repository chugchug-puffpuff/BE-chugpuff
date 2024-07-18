package chugpuff.chugpuff.service;

import chugpuff.chugpuff.entity.JobCode;
import chugpuff.chugpuff.entity.LocationCode;
import chugpuff.chugpuff.repository.JobCodeRepository;
import chugpuff.chugpuff.repository.LocationCodeRepository;
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

    //공고 조회 및 필터링
    public String getJobPostings(String regionName, String jobMidName, String jobName) {
        UriComponentsBuilder builder = UriComponentsBuilder.fromHttpUrl(API_URL)
                .queryParam("access-key", accessKey);

        LocationCode locationCode = locationCodeRepository.findByRegionName(regionName);

        if (locationCode != null) {
            builder.queryParam("loc_cd", locationCode.getLocCd());
        }

        List<JobCode> jobCodes = jobCodeRepository.findByJobMidName(jobMidName);

        for (JobCode jobCode : jobCodes) {
            builder.queryParam("job_mid_cd", jobCode.getJobMidCd())
                    .queryParam("job_cd", jobCode.getJobCd());
        }

        builder.queryParam("count", 1000);

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

}

