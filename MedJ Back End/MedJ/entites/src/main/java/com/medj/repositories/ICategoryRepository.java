package com.medj.repositories;

import com.medj.entities.Category;
import com.medj.entities.CategoryTarget;
import com.medj.entities.Document;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface ICategoryRepository extends JpaRepository<Category, Long> {

    @Query("SELECT c FROM Category c WHERE c.isActive = true and c.target = :target")
    Page<Category> findAllCategoriesByTargetAndByByIsActive(CategoryTarget target, Pageable pageable);

    @Query("SELECT c FROM Category c WHERE c.isActive = true AND c.id = :id")
    Category findByIdAndByIsActive(@Param("id")Long id);
}
