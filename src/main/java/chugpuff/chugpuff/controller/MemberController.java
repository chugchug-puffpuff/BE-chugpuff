package chugpuff.chugpuff.controller;

import chugpuff.chugpuff.domain.Member;
import chugpuff.chugpuff.dto.MemberDTO;
import chugpuff.chugpuff.dto.PasswordUpdateDTO;
// import chugpuff.chugpuff.service.EmailService;
import chugpuff.chugpuff.service.MemberService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.mail.MessagingException;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Random;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/members")
public class MemberController {

    private MemberService memberService;
//    private EmailService emailService;
//    private ConcurrentHashMap<String, String> emailVerificationCodes = new ConcurrentHashMap<>();
//    private ConcurrentHashMap<String, Boolean> emailVerificationStatus = new ConcurrentHashMap<>();
//
    public MemberController(MemberService memberService) {
        this.memberService = memberService;
//        this.emailService = emailService;
    }

    // 새로운 회원 추가
    @PostMapping
//    public ResponseEntity<?> addMember(@RequestBody MemberDTO memberDTO) {
//        String email = memberDTO.getEmail();
//        Boolean isVerified = emailVerificationStatus.getOrDefault(email, false);
//
//        if (!isVerified) {
//            return new ResponseEntity<>("Email not verified.", HttpStatus.BAD_REQUEST);
//        }
//
//        try {
//            Member member = convertToEntity(memberDTO);
//            Member savedMember = memberService.saveMember(member);
//            MemberDTO savedMemberDTO = convertToDto(savedMember);
//            return new ResponseEntity<>(savedMemberDTO, HttpStatus.CREATED);
//        } catch (IllegalArgumentException e) {
//            return new ResponseEntity<>(e.getMessage(), HttpStatus.BAD_REQUEST);
//        } catch (Exception e) {
//            return new ResponseEntity<>("Failed to add member: " + e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
//        }
//    }
    public ResponseEntity<?> addMember(@RequestBody MemberDTO memberDTO) {
        try {
            Member member = convertToEntity(memberDTO);
            Member savedMember = memberService.saveMember(member);
            MemberDTO savedMemberDTO = convertToDto(savedMember);
            return new ResponseEntity<>(savedMemberDTO, HttpStatus.CREATED);
        } catch (IllegalArgumentException e) {
            return new ResponseEntity<>(e.getMessage(), HttpStatus.BAD_REQUEST);
        } catch (Exception e) {
            return new ResponseEntity<>("Failed to add member: " + e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

//    // 이메일 인증 코드 요청
//    @PostMapping("/request-email-verification")
//    public ResponseEntity<?> requestEmailVerification(@RequestParam String email) {
//        String verificationCode = generateVerificationCode();
//        emailVerificationCodes.put(email, verificationCode);
//        emailVerificationStatus.put(email, false);
//        try {
//            emailService.sendEmail(email, "Email Verification Code", "Your verification code is: " + verificationCode);
//            return new ResponseEntity<>("Verification code sent to email.", HttpStatus.OK);
//        } catch (MessagingException e) {
//            return new ResponseEntity<>("Failed to send verification email: " + e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
//        }
//    }
//
//    // 이메일 인증 코드 확인
//    @PostMapping("/verify-email-code")
//    public ResponseEntity<?> verifyEmailCode(@RequestBody Map<String, String> request) {
//        String email = request.get("email");
//        String code = request.get("code");
//
//        String storedCode = emailVerificationCodes.get(email);
//        if (storedCode != null && storedCode.equals(code)) {
//            emailVerificationCodes.remove(email);
//            emailVerificationStatus.put(email, true);
//            return new ResponseEntity<>("Email verified successfully.", HttpStatus.OK);
//        } else {
//            return new ResponseEntity<>("Invalid verification code.", HttpStatus.BAD_REQUEST);
//        }
//    }
//
//    // 이메일 인증 상태 조회
//    @GetMapping("/email-verification-status")
//    public ResponseEntity<Boolean> getEmailVerificationStatus(@RequestParam String email) {
//        Boolean isVerified = emailVerificationStatus.getOrDefault(email, false);
//        return new ResponseEntity<>(isVerified, HttpStatus.OK);
//    }
//
//    // 인증 코드 생성
//    private String generateVerificationCode() {
//        Random random = new Random();
//        int code = 100000 + random.nextInt(900000);
//        return String.valueOf(code);
//    }

    // user_id로 특정 회원 조회
    @GetMapping("/{user_id}")
    public ResponseEntity<MemberDTO> getMemberByUser_id(@PathVariable Long user_id) {
        Optional<Member> optionalMember = memberService.getMemberByUser_id(user_id);
        return optionalMember.map(member -> new ResponseEntity<>(convertToDto(member), HttpStatus.OK))
                .orElseGet(() -> new ResponseEntity<>(HttpStatus.NOT_FOUND));
    }

    // id로 특정 회원 조회
    @GetMapping("/username/{username}")
    public ResponseEntity<MemberDTO> getMemberByUsername(@PathVariable String username) {
        Optional<Member> optionalMember = memberService.getMemberByUsername(username);
        return optionalMember.map(member -> new ResponseEntity<>(convertToDto(member), HttpStatus.OK))
                .orElseGet(() -> new ResponseEntity<>(HttpStatus.NOT_FOUND));
    }

    // 모든 회원 조회
    @GetMapping
    public ResponseEntity<List<MemberDTO>> getAllMembers() {
        List<Member> members = memberService.getAllMembers();
        List<MemberDTO> memberDTOs = members.stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
        return new ResponseEntity<>(memberDTOs, HttpStatus.OK);
    }

    // 회원 삭제
    @DeleteMapping("/{user_id}")
    public ResponseEntity<Void> deleteMember(@PathVariable Long user_id) {
        memberService.deleteMember(user_id);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }

    // 회원 정보 업데이트
    @PutMapping("/{user_id}")
    public ResponseEntity<Object> updateMember(@PathVariable Long user_id, @RequestBody MemberDTO memberDTO) {
        try {
            Member updatedMember = convertToEntity(memberDTO);
            Member updated = memberService.updateMember(user_id, updatedMember);
            MemberDTO updatedDTO = convertToDto(updated);
            return new ResponseEntity<>(updatedDTO, HttpStatus.OK);
        } catch (IllegalArgumentException e) {
            return new ResponseEntity<>(e.getMessage(), HttpStatus.BAD_REQUEST);
        }
    }

    // 비밀번호 업데이트
    @PutMapping("/{user_id}/password")
    public ResponseEntity<Object> updatePassword(@PathVariable Long user_id, @RequestParam String oldPassword, @RequestBody PasswordUpdateDTO passwordUpdateDTO) {
        try {
            // 새로운 비밀번호와 확인 비밀번호 일치 여부 확인
            if (!passwordUpdateDTO.getNewPassword().equals(passwordUpdateDTO.getConfirmPassword())) {
                throw new IllegalArgumentException("새로운 비밀번호와 확인 비밀번호가 일치하지 않습니다.");
            }

            Member updated = memberService.updatePassword(user_id, oldPassword, passwordUpdateDTO.getNewPassword());
            MemberDTO updatedDTO = convertToDto(updated);
            return new ResponseEntity<>(updatedDTO, HttpStatus.OK);
        } catch (IllegalArgumentException e) {
            return new ResponseEntity<>(e.getMessage(), HttpStatus.BAD_REQUEST);
        }
    }

    // 회원 ID 중복 체크
    @GetMapping("/checkUserId")
    public ResponseEntity<Boolean> checkUserIdDuplicate(@RequestParam String id) {
        boolean isDuplicate = memberService.checkUserIdDuplicate(id);
        return new ResponseEntity<>(isDuplicate, HttpStatus.OK);
    }

    // 엔티티를 DTO로 변환
    private MemberDTO convertToDto(Member member) {
        MemberDTO memberDTO = new MemberDTO();
        memberDTO.setUser_id(member.getUser_id());
        memberDTO.setId(member.getId());
        memberDTO.setPassword(member.getPassword());
        memberDTO.setName(member.getName());
        memberDTO.setJob(member.getJob());
        memberDTO.setJobKeyword(member.getJobKeyword());
        memberDTO.setEmail(member.getEmail());
        memberDTO.setIsAbove15(member.getIsAbove15());
        memberDTO.setPrivacyPolicyAccepted(member.getPrivacyPolicyAccepted());
        memberDTO.setRecordingAccepted(member.getRecordingAccepted());
        return memberDTO;
    }

    // DTO를 엔티티로 변환
    private Member convertToEntity(MemberDTO memberDTO) {
        Member member = new Member();
        member.setUser_id(memberDTO.getUser_id());
        member.setId(memberDTO.getId());
        member.setPassword(memberDTO.getPassword());
        member.setName(memberDTO.getName());
        member.setJob(memberDTO.getJob());
        member.setJobKeyword(memberDTO.getJobKeyword());
        member.setEmail(memberDTO.getEmail());
        member.setIsAbove15(memberDTO.getIsAbove15());
        member.setPrivacyPolicyAccepted(memberDTO.getPrivacyPolicyAccepted());
        member.setRecordingAccepted(memberDTO.getRecordingAccepted());
        return member;
    }
}
