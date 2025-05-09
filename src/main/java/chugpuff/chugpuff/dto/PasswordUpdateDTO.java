package chugpuff.chugpuff.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class PasswordUpdateDTO {
    private String newPassword;
    private String confirmPassword;
}
