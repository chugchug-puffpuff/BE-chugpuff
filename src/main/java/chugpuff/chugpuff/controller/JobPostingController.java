package chugpuff.chugpuff.controller;

import chugpuff.chugpuff.entity.JobPostingComment;
import chugpuff.chugpuff.service.CustomUserDetails;
import chugpuff.chugpuff.service.JobPostingService;
import chugpuff.chugpuff.service.MemberService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/job-postings")
public class JobPostingController {
    private final JobPostingService jobPostingService;

    @Autowired
    public JobPostingController(JobPostingService jobPostingService) {
        this.jobPostingService = jobPostingService;
    }

    @Autowired
    private MemberService memberService;

    //공고 조회 및 필터링
    @GetMapping("")
    public ResponseEntity<String> getJobPostings(
            @RequestParam(required = false) String regionName,
            @RequestParam(required = false) String jobName,
            @RequestParam(required = false) String jobMidname,
            @RequestParam(required = false) String sort) {

        String result;
        if ("scrap-count".equals(sort)) {
            result = String.join(", ", jobPostingService.getJobPostingsSortedByScrapCount());
        } else {
            result = jobPostingService.getJobPostings(regionName, jobName, jobMidname, sort);
        }

        return ResponseEntity.ok().body(result);
    }

    //키워드 검색 + 필터링
    @GetMapping("/search")
    public ResponseEntity<String> getJobPostingsByKeywords(
            @RequestParam String keywords,
            @RequestParam(required = false) String regionName,
            @RequestParam(required = false) String jobName,
            @RequestParam(required = false) String jobMidname,
            @RequestParam(required = false) String sort) {

        String result = jobPostingService.getJobPostingsByKeywords(keywords, regionName, jobName,jobMidname, sort);
        return ResponseEntity.ok().body(result);
    }


    //특정 공고 조회
    @GetMapping("/{jobId}")
    public ResponseEntity<String> getJobDetails(@PathVariable String jobId) {
        String result = jobPostingService.getJobDetails(jobId);
        return ResponseEntity.ok().body(result);
    }

    //회원 맞춤 공고 조회
    @GetMapping("/recommendations")
    public ResponseEntity<String> getRecommendedJobPostings(Authentication authentication) {
        CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();
        String memberId = userDetails.getUsername();
        String result = jobPostingService.getRecommendedJobPostingsForMember(memberId);
        return ResponseEntity.ok().body(result);
    }

    // 스크랩 토글
    @PostMapping("/{jobId}/scrap")
    public ResponseEntity<Void> toggleScrap(@PathVariable String jobId, Authentication authentication) {
        CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();
        String memberId = userDetails.getUsername();

        jobPostingService.toggleScrap(memberId, jobId);
        return ResponseEntity.ok().build();
    }

    // 사용자가 스크랩한 공고 조회
    @GetMapping("/scraps")
    public ResponseEntity<List<String>> getScrappedJobPostings(Authentication authentication) {
        CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();
        String memberId = userDetails.getUsername();

        List<String> scrappedJobPostings = jobPostingService.getScrappedJobPostings(memberId);
        return ResponseEntity.ok().body(scrappedJobPostings);
    }

    // 특정 공고의 스크랩 수 조회
    @GetMapping("/{jobId}/scrap-count")
    public ResponseEntity<Long> getJobScrapCount(@PathVariable String jobId) {
        Long scrapCount = jobPostingService.getJobScrapCount(jobId);
        return ResponseEntity.ok().body(scrapCount);
    }

    // 댓글 작성
    @PostMapping("/{jobId}/comments")
    public ResponseEntity<JobPostingComment> addComment(@PathVariable String jobId, @RequestBody CommentRequest commentRequest, Authentication authentication) {
        CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();
        String userId = userDetails.getUsername();
        JobPostingComment jobPostingComment = jobPostingService.addComment(jobId, userId, commentRequest.getComment());
        return ResponseEntity.ok().body(jobPostingComment);
    }

    // 댓글 수정
    @PutMapping("/comments/{commentId}")
    public ResponseEntity<JobPostingComment> updateComment(@PathVariable Long commentId, @RequestBody CommentRequest commentRequest, Authentication authentication) {
        CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();
        String userId = userDetails.getUsername();
        JobPostingComment updatedComment = jobPostingService.updateComment(commentId, userId, commentRequest.getComment());
        return ResponseEntity.ok().body(updatedComment);
    }

    // 댓글 삭제
    @DeleteMapping("/comments/{commentId}")
    public ResponseEntity<Void> deleteComment(@PathVariable Long commentId, Authentication authentication) {
        CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();
        String userId = userDetails.getUsername();
        jobPostingService.deleteComment(commentId, userId);
        return ResponseEntity.noContent().build();
    }

    // 특정 공고에 달린 댓글 조회
    @GetMapping("/{jobId}/comments")
    public ResponseEntity<List<JobPostingComment>> getCommentsForJobPosting(@PathVariable String jobId) {
        List<JobPostingComment> comments = jobPostingService.getCommentsForJobPosting(jobId);
        return ResponseEntity.ok(comments);
    }

    // 사용자가 작성한 모든 댓글 조회
    @GetMapping("/comments/user")
    public ResponseEntity<List<JobPostingComment>> getUserComments(Authentication authentication) {
        CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();
        String memberId = userDetails.getUsername();

        List<JobPostingComment> userComments = jobPostingService.getCommentsByUser(memberId);
        return ResponseEntity.ok().body(userComments);
    }

    // 요청 바디 작성을 위한
    public static class CommentRequest {
        private String comment;

        // Getters and setters

        public String getComment() {
            return comment;
        }

        public void setComment(String comment) {
            this.comment = comment;
        }
    }

    //2차 근무지 조회
    @GetMapping("/regions")
    public ResponseEntity<List<String>> getRegionsByLocBcd(@RequestParam String regionName) {
        List<String> regions = jobPostingService.getRegionsByLocBcd(regionName);
        return ResponseEntity.ok(regions);
    }

    //세부 직무명 조회
    @GetMapping("/job-names")
    public ResponseEntity<List<String>> getJobNamesByJobMidName(@RequestParam String jobMidName) {
        List<String> jobNames = jobPostingService.getJobNamesByJobMidName(jobMidName);
        return ResponseEntity.ok(jobNames);
    }

    /*// 기업 로고 검색
    @GetMapping("/logo")
    public ResponseEntity<String> getCompanyLogo(@RequestParam String company) {
        String logoUrl = jobPostingService.getCompanyLogos(company).toString();
        return ResponseEntity.ok().body(logoUrl);
    }*/
}