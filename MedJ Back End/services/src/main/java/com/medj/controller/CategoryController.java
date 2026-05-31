package com.medj.controller;

import com.medj.entities.CategoryTarget;
import com.medj.entities.CategoryType;
import com.medj.service.impl.CategoryService;
import com.medj.view.inView.CategoryInView;
import com.medj.view.outView.CategoryOutView;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/category")
public class CategoryController {
    @Autowired
    private CategoryService service;

    @GetMapping("/all")
    public Page<CategoryOutView> getAll(@RequestParam CategoryTarget target, Pageable pageable){
        return service.findAll(target, pageable);
    }

    @GetMapping("/all/byType")
    public List<CategoryOutView> getAllByType(@RequestParam CategoryTarget target,
                                              @RequestParam CategoryType categoryType) {
        return service.findAllByType(target, categoryType);
    }

    @PostMapping("/add")
    public CategoryOutView addOne(@RequestBody CategoryInView category) throws Exception {
        return service.addOne(category);
    }
}
