package chugpuff.chugpuff.service;

import chugpuff.chugpuff.entity.JobPosting;
import chugpuff.chugpuff.repository.JobPostingRepository;

import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.Collections;
import java.util.List;

@Service
public class JobPostingService {

    private final String API_URL = "https://oapi.saramin.co.kr/job-search";
    private final String API_KEY = "fXUtujznPIRqfBsGXXSxoeD2eOgUZx99aR7OMBW1b43WIasHMZFI";

    @Autowired
    private JobPostingRepository jobPostingRepository;

    public List<JobPosting> getAllJobPostings() {
        return jobPostingRepository.findAll();
    }

    public void saveJobPosting(JobPosting jobPosting) {
        jobPostingRepository.save(jobPosting);
    }

    public void fetchAndSaveJobPostings() {
        RestTemplate restTemplate = new RestTemplate();
        String url = API_URL + "?access-key=" + API_KEY + "&count=100"; // example parameters

        ResponseEntity<String> response = restTemplate.getForEntity(url, String.class);
        if (response.getStatusCode() == HttpStatus.OK) {
            JSONObject jsonObject = new JSONObject(response.getBody());
            JSONObject jobsObject = jsonObject.getJSONObject("jobs");

            JSONArray jobs = jobsObject.getJSONArray("job"); // "job" 배열을 가져옴

            for (int i = 0; i < jobs.length(); i++) {
                JSONObject job = jobs.getJSONObject(i);
                String jobNo = job.getString("id");

                JobPosting jobPosting = new JobPosting();
                jobPosting.setJobNo(jobNo);
                jobPosting.setScraps(0);

                saveJobPosting(jobPosting);
            }
        }
    }
}
