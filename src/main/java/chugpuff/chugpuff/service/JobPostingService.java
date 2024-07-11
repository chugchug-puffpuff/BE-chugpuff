package chugpuff.chugpuff.service;

import chugpuff.chugpuff.entity.JobPosting;
import chugpuff.chugpuff.repository.JobPostingRepository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.Collections;

@Service
public class JobPostingService {

    private final String API_URL = "https://oapi.saramin.co.kr/job-search";
    private final String API_KEY = "fXUtujznPIRqfBsGXXSxoeD2eOgUZx99aR7OMBW1b43WIasHMZFI";

    @Autowired
    private JobPostingRepository jobPostingRepository;

    public String fetchJobPosting(String jobId) {
        RestTemplate restTemplate = new RestTemplate();
        HttpHeaders headers = new HttpHeaders();
        headers.setAccept(Collections.singletonList(MediaType.APPLICATION_JSON));
        HttpEntity<String> entity = new HttpEntity<>("parameters", headers);

        String url = API_URL + "?access-key=" + API_KEY + "&keywords=" + jobId;
        ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.GET, entity, String.class);
        return response.getBody();
    }


    public JobPosting saveJobPosting(String jobId) {
        JobPosting jobPosting = new JobPosting();
        jobPosting.setJobId(jobId);
        return jobPostingRepository.save(jobPosting);
    }

    public JobPosting getJobPosting(Long j_id) {
        return jobPostingRepository.findById(j_id).orElse(null);
    }

}
