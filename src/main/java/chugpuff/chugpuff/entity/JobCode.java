package chugpuff.chugpuff.entity;

import jakarta.persistence.*;
import lombok.*;

@Getter
@Setter
@Entity
public class JobCode {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String jobMidCd;
    private String jobMidName;
    private String jobCd;
    private String jobName;

    // Getters and Setters
}