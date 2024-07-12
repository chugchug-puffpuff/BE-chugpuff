package chugpuff.chugpuff.controller;

import chugpuff.chugpuff.entity.JobPosting;
import chugpuff.chugpuff.service.JobPostingService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/jobpostings")
public class JobPostingController {

    @Autowired
    private JobPostingService jobPostingService;

    @GetMapping
    public List<JobPosting> getAllJobPostings() {
        return jobPostingService.getAllJobPostings();
    }

    @PostMapping
    public void fetchAndSaveJobPostings() {
        jobPostingService.fetchAndSaveJobPostings();
    }
}
