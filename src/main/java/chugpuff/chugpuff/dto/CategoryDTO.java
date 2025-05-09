package chugpuff.chugpuff.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CategoryDTO {
    private int categoryId;
    private String categoryName;

    //기본 생성자
    public CategoryDTO() {
    }

    //매개변수 받는 생성자
    public CategoryDTO(int categoryId, String categoryName) {
        this.categoryId = categoryId;
        this.categoryName = categoryName;
    }

    // getters and setters
}
