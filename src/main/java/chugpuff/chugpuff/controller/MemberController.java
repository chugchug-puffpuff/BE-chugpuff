package chugpuff.chugpuff.controller;

import chugpuff.chugpuff.domain.Member;
import chugpuff.chugpuff.dto.MemberDTO;
import chugpuff.chugpuff.service.MemberService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/members")
public class MemberController {

    @Autowired
    private MemberService memberService;

    @PostMapping
    public ResponseEntity<MemberDTO> addMember(@RequestBody MemberDTO memberDTO) {
        Member member = convertToEntity(memberDTO);
        Member savedMember = memberService.saveMember(member);
        MemberDTO savedMemberDTO = convertToDto(savedMember);
        return new ResponseEntity<>(savedMemberDTO, HttpStatus.CREATED);
    }

    @GetMapping("/{id}")
    public ResponseEntity<MemberDTO> getMemberById(@PathVariable Long id) {
        Optional<Member> optionalMember = memberService.getMemberById(id); // ID로 회원 조회
        return optionalMember.map(member -> new ResponseEntity<>(convertToDto(member), HttpStatus.OK))
                .orElseGet(() -> new ResponseEntity<>(HttpStatus.NOT_FOUND)); // 조회된 회원이 있으면 회원 정보 반환, 없으면 404 에러 반환
    }

    @GetMapping
    public ResponseEntity<List<MemberDTO>> getAllMembers() {
        List<Member> members = memberService.getAllMembers(); // 모든 회원 조회
        List<MemberDTO> memberDTOs = members.stream()
                .map(this::convertToDto) // 엔티티를 DTO로 변환
                .collect(Collectors.toList());
        return new ResponseEntity<>(memberDTOs, HttpStatus.OK); // 조회된 모든 회원 정보 반환
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteMember(@PathVariable Long id) {
        memberService.deleteMember(id); // 회원 삭제
        return new ResponseEntity<>(HttpStatus.NO_CONTENT); // HTTP 상태 코드 204 반환 (콘텐츠 없음)
    }

    @PutMapping("/{id}")
    public ResponseEntity<MemberDTO> updateMember(@PathVariable Long id, @RequestParam String password, @RequestBody MemberDTO memberDTO) {
        Member updatedMember = convertToEntity(memberDTO); // DTO를 엔티티로 변환
        Member updated = memberService.updateMember(id, password, updatedMember); // 회원 정보 업데이트
        MemberDTO updatedDTO = convertToDto(updated); // 업데이트된 회원 엔티티를 DTO로 변환
        return new ResponseEntity<>(updatedDTO, HttpStatus.OK); // HTTP 상태 코드 200과 함께 업데이트된 회원 정보 반환
    }

    private MemberDTO convertToDto(Member member) {
        MemberDTO memberDTO = new MemberDTO();
        memberDTO.setUserId(member.getUserId());
        memberDTO.setPassword(member.getPassword());
        memberDTO.setName(member.getName());
        memberDTO.setBirth(member.getBirth());
        memberDTO.setEmail(member.getEmail());
        memberDTO.setJob(member.getJob());
        memberDTO.setJobKeyword(member.getJobKeyword());
        return memberDTO;
    }

    private Member convertToEntity(MemberDTO memberDTO) {
        Member member = new Member();
        member.setUserId(memberDTO.getUserId());
        member.setPassword(memberDTO.getPassword());
        member.setName(memberDTO.getName());
        member.setBirth(memberDTO.getBirth());
        member.setEmail(memberDTO.getEmail());
        member.setJob(memberDTO.getJob());
        member.setJobKeyword(memberDTO.getJobKeyword());
        return member;
    }
}
