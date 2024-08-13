package chugpuff.chugpuff.service;

import chugpuff.chugpuff.domain.Member;
import chugpuff.chugpuff.entity.Calender;
import chugpuff.chugpuff.repository.CalenderRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class CalenderService {

    @Autowired
    private CalenderRepository calenderRepository;

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
}
