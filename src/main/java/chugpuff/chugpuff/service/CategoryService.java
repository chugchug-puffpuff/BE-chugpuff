package chugpuff.chugpuff.service;

import chugpuff.chugpuff.entity.Category;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.List;

@Service
public class CategoryService {

    private final List<Category> categories = Arrays.asList(
            new Category() {{
                setCategoryId(1);
                setCategoryName("정보공유");
            }},
            new Category() {{
                setCategoryId(2);
                setCategoryName("취업고민");
            }}
    );

    // 모든 카테고리 조회
    public List<Category> getAllCategories() {
        return categories;
    }

    // 카테고리 이름으로 조회
    public Category findCategoryByName(String name) {
        return categories.stream()
                .filter(category -> category.getCategoryName().equals(name))
                .findFirst()
                .orElse(null);
    }

    // 카테고리 ID로 조회
    public Category findCategoryById(int id) {
        return categories.stream()
                .filter(category -> category.getCategoryId() == id)
                .findFirst()
                .orElse(null);
    }
}
