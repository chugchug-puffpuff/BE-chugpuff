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
        validateDuplicateMember(member);
        validateAllTermsAccepted(member);
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

    public Member updateMember(Long user_id, String password, Member updatedMember) {
        Optional<Member> optionalMember = memberRepository.findById(user_id);
        if (optionalMember.isPresent()) {
            Member member = optionalMember.get();
            if (member.getPassword().equals(password)) {
                member.setPassword(updatedMember.getPassword());
                member.setEmail(updatedMember.getEmail());
                member.setJob(updatedMember.getJob());
                member.setJobKeyword(updatedMember.getJobKeyword());
                return memberRepository.save(member);
            } else {
                throw new IllegalArgumentException("비밀번호가 일치하지 않습니다.");
            }
        } else {
            throw new IllegalArgumentException("해당 id에 해당하는 회원이 존재하지 않습니다: " + id);
        }
    }

    private void validateDuplicateMember(Member member) {
        memberRepository.findById(member.getUser_id())
                .ifPresent(m -> {
                    throw new IllegalStateException("이미 존재하는 회원입니다.");
                });
    }

    private void validateAllTermsAccepted(Member member) {
        if (!Boolean.TRUE.equals(member.getIsAbove15()) ||
                !Boolean.TRUE.equals(member.getTermsAccepted()) ||
                !Boolean.TRUE.equals(member.getPrivacyPolicyAccepted()) ||
                !Boolean.TRUE.equals(member.getRecordingAccepted())) {
            throw new IllegalArgumentException("모든 필수 항목에 동의해야 회원가입이 가능합니다.");
        }
    }

    public boolean checkUserIdDuplicate(Long user_id) {
        return memberRepository.findById(user_id).isPresent();
    }
}
