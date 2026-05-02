package com.medj.service;

import com.medj.entities.CategoryTarget;
import com.medj.view.inView.CategoryInView;
import com.medj.view.outView.CategoryOutView;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.Optional;

public interface ICategoryService {
    public CategoryOutView addOne(CategoryInView category) throws Exception;

    public Page<CategoryOutView> findAll(CategoryTarget target, Pageable pageable);

    public Optional<CategoryOutView> findById(Long id);
}
