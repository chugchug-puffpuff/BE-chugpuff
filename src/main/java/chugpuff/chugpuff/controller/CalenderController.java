package chugpuff.chugpuff.controller;

import chugpuff.chugpuff.domain.Member;
import chugpuff.chugpuff.entity.Calender;
import chugpuff.chugpuff.service.CalenderService;
import chugpuff.chugpuff.service.MemberService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/calenders")
public class CalenderController {

    @Autowired
    private CalenderService calenderService;

    @Autowired
    private MemberService memberService;

    //일정 입력
    @PostMapping
    public Calender createCalender(@RequestBody Calender calender, @AuthenticationPrincipal UserDetails userDetails) {
        Member member = memberService.getMemberByUsername(userDetails.getUsername())
                .orElseThrow(() -> new IllegalArgumentException("해당 멤버를 찾을 수 없습니다."));

        calender.setMember(member);
        return calenderService.saveCalender(calender);
    }

    //일정 조회
    @GetMapping("/{id}")
    public ResponseEntity<Calender> getCalenderById(@PathVariable Long id) {
        Calender calender = calenderService.getCalenderById(id)
                .orElseThrow(() -> new RuntimeException("Calender not found with id: " + id));
        return ResponseEntity.ok().body(calender);
    }

    //일정 모두 조회 (해당 멤버)
    @GetMapping
    public List<Calender> getAllCalenders(@AuthenticationPrincipal UserDetails userDetails) {
        // 현재 인증된 사용자의 username을 가져옴
        String username = userDetails.getUsername();

        // username을 이용하여 Member 조회
        Member member = memberService.getMemberByUsername(username)
                .orElseThrow(() -> new IllegalArgumentException("해당 멤버를 찾을 수 없습니다."));

        return calenderService.getCalendersByMember(member);
    }

    //일정 수정
    @PutMapping("/{id}")
    public ResponseEntity<Calender> updateCalender(@PathVariable Long id, @RequestBody Calender calenderDetails) {
        Calender updatedCalender = calenderService.updateCalender(id, calenderDetails);
        return ResponseEntity.ok(updatedCalender);
    }

    //일정 삭제
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteCalender(@PathVariable Long id) {
        calenderService.deleteCalender(id);
        return ResponseEntity.noContent().build();
    }

    //마감기한 푸시 알림 (수동 트리거)
    @GetMapping("/notifications")
    public List<String> getD1DeadlineNotifications(@AuthenticationPrincipal UserDetails userDetails) {
        String username = userDetails.getUsername();
        Member member = memberService.getMemberByUsername(username)
                .orElseThrow(() -> new IllegalArgumentException("해당 멤버를 찾을 수 없습니다."));

        // 현재 날짜의 다음날을 구함 (테스트를 위해 +5일)
        LocalDate tomorrow = LocalDate.now().plusDays(1);

        // 쿼리 결과 디버깅
        List<Calender> d1Deadlines = calenderService.getCalendersByMemberAndMemoDate(member, tomorrow);
        if (d1Deadlines.isEmpty()) {
            System.out.println("No D-1 deadlines found for member: " + username);
        } else {
            d1Deadlines.forEach(calender -> System.out.println("Found D-1 deadline: " + calender.getMemoContent()));
        }


        return d1Deadlines.stream()
                .map(calender -> "'" + calender.getMemoContent() + "' 일정 하루 전입니다. 지금 바로 확인해 보세요!")
                .collect(Collectors.toList());
    }

}
