package com.medj.service.impl;

import com.medj.entities.Appointment;
import com.medj.entities.Category;
import com.medj.entities.CategoryTarget;
import com.medj.entities.Specialty;
import com.medj.repositories.ICategoryRepository;
import com.medj.service.ICategoryService;
import com.medj.view.inView.CategoryInView;
import com.medj.view.outView.AppointmentOutView;
import com.medj.view.outView.CategoryOutView;
import com.medj.view.outView.SpecialtyOutView;
import org.modelmapper.ModelMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.Optional;
import java.util.UUID;

@Service
public class CategoryService implements ICategoryService {
    Logger log = LoggerFactory.getLogger(this.getClass().getName());

    @Autowired
    private ICategoryRepository categoryRepository;

    @Autowired
    private ModelMapper modelMapper;

    @Override
    public CategoryOutView addOne(CategoryInView category) throws Exception {
        Category mapped = modelMapper.map(category, Category.class);
        mapped.setIsActive(true);
        return modelMapper.map(categoryRepository.save(mapped), CategoryOutView.class);
    }

    @Override
    public Page<CategoryOutView> findAll(CategoryTarget target, Pageable pageable) {
        String logId = UUID.randomUUID().toString();
        log.info("{} : findAll start", logId);
        Page<Category> categories = categoryRepository.findAllCategoriesByTargetAndByByIsActive(target, pageable);

        return categories.map(app -> {
            CategoryOutView view = modelMapper.map(app, CategoryOutView.class);
            return view;
        });
    }

    @Override
    public Optional<CategoryOutView> findById(Long id) {
        Category category = categoryRepository.findByIdAndByIsActive(id);
        return Optional.ofNullable(modelMapper.map(category, CategoryOutView.class));
    }
}
