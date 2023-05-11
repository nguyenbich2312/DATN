package com.bzone.repository;

import com.bzone.model.Category;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Set;


@Repository
public interface CategoryRepository extends JpaRepository<Category, Long> {
    Category findByCategoryName(String categoryName);

    Set<Category> findByParentCategory_Id(Long id);

    @Query(value = "SELECT * FROM category where category_id = parent_id",nativeQuery = true)
    Set<Category> findParentCategory();

    @Query(value = "SELECT * FROM category where category_id = ?1",nativeQuery = true)
    Category findCategoryById(Long id);

}
