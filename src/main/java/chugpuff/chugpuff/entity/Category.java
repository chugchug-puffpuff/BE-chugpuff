package chugpuff.chugpuff.entity;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Getter
@Setter
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
public class Category {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int categoryId; //카테고리 ID

    @Column(name = "category_name")
    private String categoryName; //카테고리 이름

    // 기본 생성자 추가
    public Category() {
    }

    // 필요한 생성자 추가
    public Category(String categoryName) {
        this.categoryName = categoryName;
    }


}
