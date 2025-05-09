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

    private Long user_id;
    private String id;
    private String password;
    private String name;
    private String job;
    private String jobKeyword;
    private String email;
    private Boolean isAbove15;
    private Boolean privacyPolicyAccepted;
    private Boolean recordingAccepted;

    public Member toEntity() {
        Member member = new Member();
        member.setUser_id(this.user_id);
        member.setId(this.id);
        member.setPassword(this.password);
        member.setName(this.name);
        member.setJob(this.job);
        member.setJobKeyword(this.jobKeyword);
        member.setEmail(this.email);
        member.setIsAbove15(this.isAbove15);
        member.setPrivacyPolicyAccepted(this.privacyPolicyAccepted);
        member.setRecordingAccepted(this.recordingAccepted);
        return member;
    }
}
