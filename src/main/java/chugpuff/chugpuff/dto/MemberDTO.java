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
}
