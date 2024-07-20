package chugpuff.chugpuff.service;

import chugpuff.chugpuff.entity.Category;
import chugpuff.chugpuff.repository.CategoryRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

@Service
public class CategoryService {
    @Autowired
    private CategoryRepository categoryRepository;

    //카테고리 목록
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

    /**
     * 모든 카테고리 조회 메서드
     *
     * @return 모든 카테고리 엔티티 리스트
     */
    public List<Category> getAllCategories() {
        return categoryRepository.findAll();
    }

    /**
     * 카테고리 이름으로 카테고리 조회
     *
     * @param categoryName 조회할 카테고리 이름
     * @return 해당 이름을 가진 카테고리 엔티티
     */
    public Category findCategoryByName(String categoryName) {
        return categoryRepository.findByCategoryName(categoryName);
    }

    /**
     * 카테고리 ID로 카테고리 조회
     *
     * @param id 조회할 카테고리 ID
     * @return 해당 ID를 가진 카테고리 엔티티, 없으면 null
     */
    public Optional<Category> findCategoryById(int id) {
        return categoryRepository.findById(id);
    }
}
