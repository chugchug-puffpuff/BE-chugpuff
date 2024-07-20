package chugpuff.chugpuff.service;

import chugpuff.chugpuff.entity.Category;
import chugpuff.chugpuff.repository.CategoryRepository;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

@Service
public class CategoryService {
    private final CategoryRepository categoryRepository;

    @Autowired
    public CategoryService(CategoryRepository categoryRepository) {
        this.categoryRepository = categoryRepository;
    }

    // 카테고리 목록
    private final List<Category> categories = Arrays.asList(
            new Category("정보공유"),
            new Category("취업고민")
    );

    /**
     * 애플리케이션 시작 시 초기 데이터를 설정하는 메서드
     */
    @PostConstruct
    public void init() {
        // 데이터베이스에 카테고리가 비어있을 경우에만 초기 카테고리를 저장
        if (categoryRepository.count() == 0) {
            categoryRepository.saveAll(categories);
        }
    }

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
