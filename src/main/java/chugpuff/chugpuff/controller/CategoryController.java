/*package chugpuff.chugpuff.controller;

import chugpuff.chugpuff.entity.Category;
import chugpuff.chugpuff.service.CategoryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/category")
public class CategoryController {

    @Autowired
    private CategoryService categoryService;

    // 모든 카테고리 조회
    @GetMapping
    public List<Category> getAllCategories() {
        return categoryService.getAllCategories();
    }

    // 이름으로 카테고리 조회
    @GetMapping("/name/{name}")
    public Category getCategoryByName(@PathVariable String name) {
        return categoryService.findCategoryByName(name);
    }

    // ID로 카테고리 조회
    *//*@GetMapping("/{id}")
    public Category getCategoryById(@PathVariable int id) {
        return categoryService.findCategoryById(id);
    }*//*
}*/
