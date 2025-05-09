package chugpuff.chugpuff.repository;

import chugpuff.chugpuff.entity.Category;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CategoryRepository extends JpaRepository<Category, Integer> {
    //카테고리 이름으로 카테고리 조회
    Category findByCategoryName(String categoryName);
}
