package chugpuff.chugpuff.controller;

import chugpuff.chugpuff.entity.JobPosting;
import chugpuff.chugpuff.service.JobPostingService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/job-postings")
public class JobPostingController {

    @Autowired
    private JobPostingService jobPostingService;

    @PostMapping
    public JobPosting createJobPosting(@RequestParam String jobId) {
        return jobPostingService.saveJobPosting(jobId);
    }

    @GetMapping("/{j_id}")
    public String getJobPosting(@PathVariable Long j_id) {
        JobPosting jobPosting = jobPostingService.getJobPosting(j_id);
        if (jobPosting != null) {
            return jobPostingService.fetchJobPosting(jobPosting.getJobId());
        }
        return "Job posting not found";
    }
}
