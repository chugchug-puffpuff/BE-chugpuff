package chugpuff.chugpuff.entity;

import jakarta.persistence.*;
import lombok.*;

@Getter
@Setter
@Entity
public class LocationCode {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String regionName;
    private String locCd;
    private String locMcd;
    private String locBcd;

    // Getters and Setters
}
