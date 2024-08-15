package chugpuff.chugpuff.service;

import chugpuff.chugpuff.domain.Member;
import chugpuff.chugpuff.entity.Calender;
import chugpuff.chugpuff.entity.Scrap;
import chugpuff.chugpuff.repository.CalenderRepository;
import chugpuff.chugpuff.repository.ScrapRepository;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;
import java.util.List;
import java.util.Optional;

@Service
public class CalenderService {
    @Autowired
    private CalenderRepository calenderRepository;

    @Autowired
    private ScrapRepository scrapRepository;

    private final JobPostingService jobPostingService;

    @Autowired
    public CalenderService(@Lazy JobPostingService jobPostingService) {
        this.jobPostingService = jobPostingService;
    }

    //일정 저장
    public Calender saveCalender(Calender calender) {
        return calenderRepository.save(calender);
    }

    //일정 조회
    public Optional<Calender> getCalenderById(Long id) {
        return calenderRepository.findById(id);
    }

    //일정 모두 조회 (해당 멤버)
    public List<Calender> getCalendersByMember(Member member) {
        return calenderRepository.findByMember(member);
    }

    //일정 수정
    public Calender updateCalender(Long id, Calender calenderDetails) {
        Calender calender = calenderRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Calender not found with id: " + id));

        calender.setMemoDate(calenderDetails.getMemoDate());
        calender.setMemoContent(calenderDetails.getMemoContent());

        return calenderRepository.save(calender);
    }

    //일정 삭제
    public void deleteCalender(Long id) {
        Calender calender = calenderRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Calender not found with id: " + id));

        calenderRepository.delete(calender);
    }

    //스크랩 공고 마감기한 추가
    public void scrapExpirationDateToCalender(Scrap scrap) {
        String jobDetails = jobPostingService.getJobDetails(scrap.getJobId());

        try {
            JSONObject jsonResponse = new JSONObject(jobDetails);
            JSONArray jobArray = jsonResponse.getJSONObject("jobs").getJSONArray("job");

            if (jobArray.length() > 0) {
                JSONObject job = jobArray.getJSONObject(0);

                String title = job.getJSONObject("position").getString("title");
                String expirationDate = job.getString("expiration-date");

                // Calender 엔티티 생성 및 저장 로직
                Calender calender = new Calender();
                calender.setMemoContent(title);
                calender.setMemoDate(expirationDate);
                calender.setMember(scrap.getMember());
                calender.setScrap(scrap);

                calenderRepository.save(calender);
            } else {
                throw new RuntimeException("No job details found for the given job ID.");
            }
        } catch (JSONException e) {
            // 예외 처리 로직 추가
            e.printStackTrace();
            throw new RuntimeException("Failed to parse job details from API response.");
        }
    }

        // 스크랩이 삭제될 때 연관된 캘린더 삭제
        public void deleteCalenderByScrap (Scrap scrap){
            List<Calender> calenders = calenderRepository.findByScrap(scrap);
            for (Calender calender : calenders) {
                calenderRepository.delete(calender);
            }
        }
    }
