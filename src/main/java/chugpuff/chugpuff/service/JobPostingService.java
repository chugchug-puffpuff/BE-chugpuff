package chugpuff.chugpuff.service;

import chugpuff.chugpuff.entity.JobPosting;
import chugpuff.chugpuff.repository.JobPostingRepository;

import org.json.JSONArray;
import org.json.JSONException;
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

    public void fetchAndSaveAllJobPostings() {
        int page = 1;
        int countPerPage = 100; // 한 페이지당 가져올 공고 수
        int totalJobs = getTotalJobsCount(); // 전체 공고 수

        while (countPerPage * (page - 1) < totalJobs) {
            String url = API_URL + "?access-key=" + API_KEY + "&count=" + countPerPage + "&page=" + page;

            RestTemplate restTemplate = new RestTemplate();
            ResponseEntity<String> response = restTemplate.getForEntity(url, String.class);

            if (response.getStatusCode() == HttpStatus.OK) {
                JSONObject jsonObject = new JSONObject(response.getBody());
                JSONObject jobsObject = jsonObject.getJSONObject("jobs");
                JSONArray jobsArray = jobsObject.getJSONArray("job");


                for (int i = 0; i < jobsArray.length(); i++) {
                    JSONObject job = jobsArray.getJSONObject(i);
                    String jobNo = job.getString("id");

                    // 이미 존재하는지 확인 후 저장
                    if (!jobPostingRepository.existsByJobNo(jobNo)) {
                        JobPosting jobPosting = new JobPosting();
                        jobPosting.setJobNo(jobNo);
                        jobPosting.setScraps(0); // 새로운 공고의 스크랩 초기값은 0으로 설정
                        saveJobPosting(jobPosting);
                    }
                }
            }

            page++;
        }
    }

    private int getTotalJobsCount() {
        String url = API_URL + "?access-key=" + API_KEY + "&count=1"; // 한 페이지당 1개씩 가져와서 전체 수 확인

        RestTemplate restTemplate = new RestTemplate();
        ResponseEntity<String> response = restTemplate.getForEntity(url, String.class);

        if (response.getStatusCode() == HttpStatus.OK) {
            try {
                JSONObject jsonObject = new JSONObject(response.getBody());
                JSONObject jobsObject = jsonObject.getJSONObject("jobs");

                if (jobsObject.has("total")) {
                    return jobsObject.getInt("total"); // 전체 공고 수 반환
                } else {
                    throw new JSONException("Field 'total' not found in 'jobs' object");
                }
            } catch (JSONException e) {
                // JSON 파싱 오류 처리
                e.printStackTrace(); // 실제로는 로깅 등으로 처리하는 것이 좋음
                return 0; // 예외 상황에서는 0을 반환하거나 다른 적절한 처리를 수행
            }
        } else {
            // HTTP 오류 처리
            System.err.println("HTTP error: " + response.getStatusCode());
            return 0; // 예외 상황에서는 0을 반환하거나 다른 적절한 처리를 수행
        }
    }
}

