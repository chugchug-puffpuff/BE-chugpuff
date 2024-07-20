package chugpuff.chugpuff.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Getter
@Setter
public class Category {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int categoryId; //카테고리 ID

    @Column(name = "category_name")
    private String categoryName; //카테고리 이름

    public void setId(int id) {
        this.categoryId = id;
    }
}
