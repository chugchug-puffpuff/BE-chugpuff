package chugpuff.chugpuff.service;

import chugpuff.chugpuff.domain.Member;
import chugpuff.chugpuff.repository.MemberRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class MemberService {

    private final MemberRepository memberRepository;
    private final PasswordEncoder passwordEncoder;

    @Autowired
    public MemberService(MemberRepository memberRepository, PasswordEncoder passwordEncoder) {
        this.memberRepository = memberRepository;
        this.passwordEncoder = passwordEncoder;
    }

    // 회원 저장
    public Member saveMember(Member member) {
        if (checkUserIdDuplicate(member.getId())) {
            throw new IllegalStateException("이미 존재하는 회원입니다.");
        }
        validateAllTermsAccepted(member);
        member.setPassword(passwordEncoder.encode(member.getPassword()));
        return memberRepository.save(member);
    }

    // ID로 회원 조회
    public Optional<Member> getMemberByUser_id(Long user_id) {
        return memberRepository.findById(user_id);
    }

    // 모든 회원 조회
    public List<Member> getAllMembers() {
        return memberRepository.findAll();
    }

    // 회원 삭제
    public void deleteMember(Long user_id) {
        memberRepository.deleteById(user_id);
    }

    // 회원 정보 업데이트
    public Member updateMember(Long user_id, String password, Member updatedMember) {
        Optional<Member> optionalMember = memberRepository.findById(user_id);
        if (optionalMember.isPresent()) {
            Member member = optionalMember.get();
            // 비밀번호 검증
            if (passwordEncoder.matches(password, member.getPassword())) {
                member.setPassword(passwordEncoder.encode(updatedMember.getPassword()));
                member.setEmail(updatedMember.getEmail());
                member.setJob(updatedMember.getJob());
                member.setJobKeyword(updatedMember.getJobKeyword());
                return memberRepository.save(member);
            } else {
                throw new IllegalArgumentException("비밀번호가 일치하지 않습니다.");
            }
        } else {
            throw new IllegalArgumentException("해당 id에 해당하는 회원이 존재하지 않습니다: " + user_id);
        }
    }

    // 비밀번호 일치 여부 확인
    public boolean isPasswordCorrect(Long user_id, String password) {
        Optional<Member> optionalMember = memberRepository.findById(user_id);
        return optionalMember.map(member -> passwordEncoder.matches(password, member.getPassword())).orElse(false);
    }

    // 모든 필수 항목 동의 체크
    private void validateAllTermsAccepted(Member member) {
        if (!Boolean.TRUE.equals(member.getIsAbove15()) ||
                !Boolean.TRUE.equals(member.getPrivacyPolicyAccepted()) ||
                !Boolean.TRUE.equals(member.getRecordingAccepted())) {
            throw new IllegalArgumentException("모든 필수 항목에 동의해야 회원가입이 가능합니다.");
        }
    }

    // 회원 ID 중복 체크
    public boolean checkUserIdDuplicate(String id) {
        return memberRepository.findById(id).isPresent();
    }

    // 로그인 인증
    public Member authenticate(String id, String password) {
        Member member = memberRepository.findById(id)
                .orElse(null);

        if (member != null && passwordEncoder.matches(password, member.getPassword())) {
            return member;
        } else {
            return null;
        }
    }
}
