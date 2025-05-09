package chugpuff.chugpuff.domain;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;

import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.*;
@Entity
@Data
@Builder
@Getter
@Setter
@ToString
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "members")
public class Member {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long user_id;

    @Column(nullable = false, unique = true)
    private String id;

    @Column(nullable = false)
    private String password;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private String job;

    @Column(nullable = false)
    private String jobKeyword;

    @Column(nullable = false)
    private String email;

    @Column(nullable = false)
    private Boolean isAbove15;

    @Column(nullable = false)
    private Boolean privacyPolicyAccepted;

    @Column(nullable = false)
    private Boolean recordingAccepted;

}
