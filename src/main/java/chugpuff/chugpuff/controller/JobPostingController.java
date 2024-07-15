package chugpuff.chugpuff.controller;

import chugpuff.chugpuff.service.JobPostingService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/job-postings")
public class JobPostingController {

    @Autowired
    private JobPostingService jobPostingService;

    @GetMapping("")
    public ResponseEntity<String> getJobPostings(
            @RequestParam(required = false) String regionName,
            @RequestParam(required = false) String jobMidName,
            @RequestParam(required = false) String jobName) {

        String result = jobPostingService.getJobPostings(regionName, jobMidName, jobName);
        return ResponseEntity.ok().body(result);
    }
}