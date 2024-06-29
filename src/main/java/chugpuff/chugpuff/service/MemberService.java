package chugpuff.chugpuff.service;

import chugpuff.chugpuff.domain.Member;
import chugpuff.chugpuff.repository.MemberRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class MemberService {

    @Autowired
    private MemberRepository memberRepository;

    public Member saveMember(Member member) {
        return memberRepository.save(member);
    }

    public Optional<Member> getMemberById(Long id) {
        return memberRepository.findById(id);
    }

    public List<Member> getAllMembers() {
        return memberRepository.findAll();
    }

    public void deleteMember(Long id) {
        memberRepository.deleteById(id);
    }

    public Member updateMember(Long id, Member updatedMember) {
        return memberRepository.findById(id)
                .map(member -> {
                    member.setPassword(updatedMember.getPassword());
                    member.setEmail(updatedMember.getEmail());
                    member.setJob(updatedMember.getJob());
                    member.setJobKeyword(updatedMember.getJobKeyword());
                    return memberRepository.save(member);
                })
                .orElseGet(() -> {
                    updatedMember.setUserId(id);
                    return memberRepository.save(updatedMember);
                });
    }
}
