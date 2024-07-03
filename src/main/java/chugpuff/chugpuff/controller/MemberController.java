package chugpuff.chugpuff.controller;

import chugpuff.chugpuff.domain.Member;
import chugpuff.chugpuff.dto.MemberDTO;
import chugpuff.chugpuff.service.MemberService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/members")
public class MemberController {

    private MemberService memberService;

    public MemberController(MemberService memberService) {
        this.memberService = memberService;
    }

    // 새로운 회원 추가
    @PostMapping
    public ResponseEntity<?> addMember(@RequestBody MemberDTO memberDTO) {
        try {
            Member member = convertToEntity(memberDTO);
            Member savedMember = memberService.saveMember(member);
            MemberDTO savedMemberDTO = convertToDto(savedMember);
            return new ResponseEntity<>(savedMemberDTO, HttpStatus.CREATED);
        } catch (IllegalArgumentException e) {
            return new ResponseEntity<>(e.getMessage(), HttpStatus.BAD_REQUEST);
        }
    }

    // ID로 특정 회원 조회
    @GetMapping("/{user_id}")
    public ResponseEntity<MemberDTO> getMemberById(@PathVariable Long user_id) {
        Optional<Member> optionalMember = memberService.getMemberById(user_id);
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
    public ResponseEntity<Object> updateMember(@PathVariable Long user_id, @RequestParam String password, @RequestBody MemberDTO memberDTO) {
        try {
            // 비밀번호 일치 여부 검증
            if (!memberService.isPasswordCorrect(user_id, password)) {
                throw new IllegalArgumentException("Incorrect password.");
            }

            Member updatedMember = convertToEntity(memberDTO);
            Member updated = memberService.updateMember(user_id, password, updatedMember);
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
        memberDTO.setTermsAccepted(member.getTermsAccepted());
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
        member.setTermsAccepted(memberDTO.getTermsAccepted());
        member.setPrivacyPolicyAccepted(memberDTO.getPrivacyPolicyAccepted());
        member.setRecordingAccepted(memberDTO.getRecordingAccepted());
        return member;
    }
}
