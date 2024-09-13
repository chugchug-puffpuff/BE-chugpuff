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
    private CalenderService calenderService;

    @Autowired
    private JobPostingCommentRepository jobPostingCommentRepository;

    //공고 조회 및 필터링
    public String getJobPostings(String regionName, String jobName, String sort) {
        UriComponentsBuilder builder = UriComponentsBuilder.fromHttpUrl(API_URL)
                .queryParam("access-key", accessKey)
                .queryParam("count", 110);


        List<LocationCode> locationCodes = locationCodeRepository.findByRegionName(regionName);

        if (locationCodes != null && !locationCodes.isEmpty()) {
            for (LocationCode locationCode : locationCodes) {
                builder.queryParam("loc_cd", locationCode.getLocCd());
            }
        }

        JobCode jobCode = jobCodeRepository.findByJobName(jobName);

        if (jobCode != null) {
            builder.queryParam("job_cd", jobCode.getJobCd());
        }

        if (sort != null && !sort.isEmpty()) {
            builder.queryParam("sort", sort);
        }

        String url = builder.toUriString();

        return restTemplate.getForObject(url, String.class);
    }

    // 키워드 검색 + 필터링
    public String getJobPostingsByKeywords(String keywords, String regionName, String jobName, String sort) {
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
                .queryParam("count", 110);

        // 지역 필터 추가
        if (regionName != null && !regionName.isEmpty()) {
            List<LocationCode> locationCodes = locationCodeRepository.findByRegionName(regionName);
            if (locationCodes != null && !locationCodes.isEmpty()) {
                for (LocationCode locationCode : locationCodes) {
                    builder.queryParam("loc_cd", locationCode.getLocCd());
                    logger.info("Added Location Code: " + locationCode.getLocCd());
                }
            } else {
                logger.warning("No location codes found for region: " + regionName);
            }
        }

        // 직무 필터 추가
        if (jobName != null && !jobName.isEmpty()) {
            JobCode jobCode = jobCodeRepository.findByJobName(jobName);
            if (jobCode != null) {
                builder.queryParam("job_cd", jobCode.getJobCd());
                logger.info("Added Job Code: " + jobCode.getJobCd());
            } else {
                logger.warning("No job code found for jobName: " + jobName);
            }
        }

        // 정렬 옵션 추가
        if (sort != null && !sort.isEmpty()) {
            builder.queryParam("sort", sort);
        }

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
                .queryParam("id", jobId)
                .queryParam("fields", "expiration-date"); //캘린더 마감기한 제공 부분에서 필요

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
                .queryParam("access-key", accessKey)
                .queryParam("count", 110);


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
            Scrap scrap = scrapOptional.get();
            scrapRepository.delete(scrap);

            // 관련 캘린더 항목도 삭제
            calenderService.deleteCalenderByScrap(scrap);
        } else {
            // 스크랩하지 않은 공고인 경우, 스크랩 추가
            Scrap scrap = new Scrap();
            scrap.setMember(member);
            scrap.setJobId(jobId);
            scrapRepository.save(scrap);

            // 관련 캘린더 항목도 추가
            calenderService.scrapExpirationDateToCalender(scrap);
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

    // 댓글 조회
    public List<JobPostingComment> getCommentsForJobPosting(String jobId) {
        return jobPostingCommentRepository.findByJobId(jobId);
    }

    //사용자가 작성한 댓글 모두 조회
    public List<JobPostingComment> getCommentsByUser(String memberId) {
        Optional<Member> optionalMember = memberRepository.findById(memberId);

        if (optionalMember.isEmpty()) {
            throw new IllegalArgumentException("Member not found with id: " + memberId);
        }

        Member member = optionalMember.get();

        // 사용자가 작성한 모든 댓글 조회
        return jobPostingCommentRepository.findByMember(member);
    }


    //스크랩순 정렬
    public List<String> getJobPostingsSortedByScrapCount() {
        List<Object[]> jobIdsWithScrapCount = scrapRepository.findJobIdsOrderByScrapCount();
        return jobIdsWithScrapCount.stream()
                .map(jobIdWithCount -> getJobDetails((String) jobIdWithCount[0]))
                .collect(Collectors.toList());
    }

    //2차 근무지 지역명 조회
    public List<String> getRegionsByLocBcd(String regionName) {
        List<LocationCode> locationCodes = locationCodeRepository.findByRegionName(regionName);

        if (locationCodes != null && !locationCodes.isEmpty()) {
            String locBcd = locationCodes.get(0).getLocBcd();  // 첫 번째 결과의 locBcd를 사용
            List<LocationCode> matchingRegions = locationCodeRepository.findByLocBcd(locBcd);

            return matchingRegions.stream()
                    .map(LocationCode::getRegionName)
                    .collect(Collectors.toList());
        } else {
            throw new IllegalArgumentException("Region name not found: " + regionName);
        }
    }

    //세부 직무명 조회
    public List<String> getJobNamesByJobMidName(String jobMidName) {
        List<JobCode> jobCodes = jobCodeRepository.findByJobMidName(jobMidName);

        // jobCodes 목록에서 jobName만 추출하여 리스트로 반환
        return jobCodes.stream()
                .map(JobCode::getJobName)
                .collect(Collectors.toList());
    }
}
