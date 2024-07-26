package chugpuff.chugpuff.service;

import chugpuff.chugpuff.domain.Member;
import chugpuff.chugpuff.entity.JobCode;
import chugpuff.chugpuff.entity.JobPostingComment;
import chugpuff.chugpuff.entity.LocationCode;
import chugpuff.chugpuff.entity.Scrap;
import chugpuff.chugpuff.repository.*;
import org.hibernate.Hibernate;
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
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.logging.Logger;
import java.util.stream.Collectors;

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

    @Autowired
    private MemberService memberService;

    @Autowired
    private ScrapRepository scrapRepository;

    @Autowired
    private JobPostingCommentRepository jobPostingCommentRepository;

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
        String jobKeyword = member.getJobKeyword();

        UriComponentsBuilder builder = UriComponentsBuilder.fromHttpUrl(API_URL)
                .queryParam("access-key", accessKey);


        if (jobKeyword != null && !jobKeyword.isEmpty()) {
            JobCode jobCode = jobCodeRepository.findByJobName(jobKeyword);
            if (jobCode != null) {
                builder.queryParam("job_cd", jobCode.getJobCd());
            }
        }

        String url = builder.toUriString();
        return restTemplate.getForObject(url, String.class);
    }

    // 스크랩 토글
    public void toggleScrap(String memberId, String jobId) {
        Optional<Member> optionalMember = memberService.getMemberByUsername(memberId);

        if (optionalMember.isEmpty()) {
            throw new IllegalArgumentException("Member not found with id: " + memberId);
        }

        Member member = optionalMember.get();
        Optional<Scrap> scrapOptional = scrapRepository.findByMemberAndJobId(member, jobId);

        if (scrapOptional.isPresent()) {
            // 이미 스크랩한 공고인 경우, 스크랩을 취소 (삭제)
            scrapRepository.delete(scrapOptional.get());
        } else {
            // 스크랩하지 않은 공고인 경우, 스크랩 추가
            Scrap scrap = new Scrap();
            scrap.setMember(member);
            scrap.setJobId(jobId);
            scrapRepository.save(scrap);
        }
    }

    // 사용자가 스크랩한 공고 조회
    public List<String> getScrappedJobPostings(String memberId) {
        Optional<Member> optionalMember = memberService.getMemberByUsername(memberId);

        if (optionalMember.isEmpty()) {
            throw new IllegalArgumentException("Member not found with id: " + memberId);
        }

        Member member = optionalMember.get();
        List<Scrap> scraps = scrapRepository.findByMember(member);

        return scraps.stream()
                .map(scrap -> getJobDetails(scrap.getJobId())) //특정 공고 조회로 넘기기
                .collect(Collectors.toList());
    }

    // 특정 공고의 스크랩 수 조회
    public Long getJobScrapCount(String jobId) {
        return scrapRepository.countByJobId(jobId);
    }

    // 댓글 작성
    public JobPostingComment addComment(String jobId, String userId, String comment) {
        Optional<Member> optionalMember = memberRepository.findById(userId);

        if (optionalMember.isEmpty()) {
            throw new IllegalArgumentException("Member not found with id: " + userId);
        }

        Member member = optionalMember.get();

        JobPostingComment jobPostingComment = new JobPostingComment();
        jobPostingComment.setJobId(jobId);
        jobPostingComment.setMember(member);
        jobPostingComment.setComment(comment);
        jobPostingComment.setCreatedAt(LocalDateTime.now());

        return jobPostingCommentRepository.save(jobPostingComment);
    }
    // 댓글 수정
    public JobPostingComment updateComment(Long commentId, String userId, String newComment) {
        Optional<JobPostingComment> optionalComment = jobPostingCommentRepository.findById(commentId);

        if (optionalComment.isEmpty()) {
            throw new IllegalArgumentException("Comment not found with id: " + commentId);
        }

        JobPostingComment jobPostingComment = optionalComment.get();

        if (!jobPostingComment.getMember().getId().equals(userId)) {
            throw new IllegalArgumentException("You are not authorized to update this comment.");
        }

        jobPostingComment.setComment(newComment);
        jobPostingComment.setUpdatedAt(LocalDateTime.now());

        // 강제로 프록시 초기화
        Hibernate.initialize(jobPostingComment.getMember());

        return jobPostingCommentRepository.save(jobPostingComment);
    }

    // 댓글 삭제
    public void deleteComment(Long commentId, String userId) {
        Optional<JobPostingComment> optionalComment = jobPostingCommentRepository.findById(commentId);

        if (optionalComment.isEmpty()) {
            throw new IllegalArgumentException("Comment not found with id: " + commentId);
        }

        JobPostingComment jobPostingComment = optionalComment.get();

        if (!jobPostingComment.getMember().getId().equals(userId)) {
            throw new IllegalArgumentException("You are not authorized to delete this comment.");
        }

        jobPostingCommentRepository.delete(jobPostingComment);
    }
}

