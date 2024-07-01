package chugpuff.chugpuff.dto;

import chugpuff.chugpuff.domain.Member;
import lombok.*;

import java.time.LocalDate;

@Getter
@Setter
@Data
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class MemberDTO {

    private Long userId;
    private String password;
    private String name;
    private LocalDate birth;
    private String email;
    private String job;
    private String jobKeyword;
    private Boolean isAbove15;
    private Boolean termsAccepted;
    private Boolean privacyPolicyAccepted;
    private Boolean recordingAccepted;

    public static MemberDTO toMemberDTO(Member member) {
        MemberDTO memberDTO = new MemberDTO();
        memberDTO.setUserId(member.getUserId());
        memberDTO.setPassword(member.getPassword());
        memberDTO.setName(member.getName());
        memberDTO.setBirth(member.getBirth());
        memberDTO.setEmail(member.getEmail());
        memberDTO.setJob(member.getJob());
        memberDTO.setJobKeyword(member.getJobKeyword());
        memberDTO.setIsAbove15(member.getIsAbove15());
        memberDTO.setTermsAccepted(member.getTermsAccepted());
        memberDTO.setPrivacyPolicyAccepted(member.getPrivacyPolicyAccepted());
        memberDTO.setRecordingAccepted(member.getRecordingAccepted());

        return memberDTO;
    }
}
