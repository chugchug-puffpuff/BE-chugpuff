package chugpuff.chugpuff.service;

import chugpuff.chugpuff.entity.LocationCode;
import chugpuff.chugpuff.repository.LocationCodeRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

@Service
public class JobPostingService {

    private final RestTemplate restTemplate = new RestTemplate();
    private static final String API_URL = "https://oapi.saramin.co.kr/job-search";

    @Value("${saramin.access-key}")
    private String accessKey;

    @Autowired
    private LocationCodeRepository locationCodeRepository;

    public String getJobPostings(String regionName) {
        UriComponentsBuilder builder = UriComponentsBuilder.fromHttpUrl(API_URL)
                .queryParam("access-key", accessKey);

        LocationCode locationCode = locationCodeRepository.findByRegionName(regionName);

        if (locationCode != null) {
            builder.queryParam("loc_cd", locationCode.getLocCd());
        }

        builder.queryParam("count", 1000);

        String url = builder.toUriString();

        return restTemplate.getForObject(url, String.class);
    }
}

