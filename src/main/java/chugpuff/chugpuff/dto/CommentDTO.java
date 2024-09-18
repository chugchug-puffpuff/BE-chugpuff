package chugpuff.chugpuff.dto;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
public class CommentDTO {
    private int bcNo;
    private String bcContent;
    private LocalDateTime bcDate;
    private String memberName;
    private LocalDateTime bcModifiedDate;
    private int boradNo;

}