package com.medj.repositories;

import com.medj.entities.Category;
import com.medj.entities.CategoryTarget;
import com.medj.entities.CategoryType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ICategoryRepository extends JpaRepository<Category, Long> {

    @Query("SELECT c FROM Category c WHERE c.isActive = true and c.target = :target")
    Page<Category> findAllCategoriesByTargetAndByByIsActive(CategoryTarget target, Pageable pageable);

    @Query("SELECT c FROM Category c WHERE c.isActive = true AND c.target = :target AND c.categoryType = :categoryType")
    List<Category> findAllByTargetAndCategoryType(@Param("target") CategoryTarget target,
                                                   @Param("categoryType") CategoryType categoryType);

    @Query("SELECT c FROM Category c WHERE c.isActive = true AND c.id = :id")
    Category findByIdAndByIsActive(@Param("id")Long id);
}
