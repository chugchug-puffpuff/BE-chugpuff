package chugpuff.chugpuff.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Getter
@Setter
public class User {
    @Id
    private String id; // 유저 ID, 기본키


}
