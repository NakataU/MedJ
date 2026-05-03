package com.medj.controller;

import com.medj.service.impl.SpecialtyService;
import com.medj.view.inView.SpecialtyInView;
import com.medj.view.outView.SpecialtyOutView;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.web.bind.annotation.*;

import org.springframework.data.domain.Pageable;

@RestController
@RequestMapping("/specialty")
public class SpecialtyController {

    @Autowired
    private SpecialtyService service;

    @GetMapping("/all")
    public Page<SpecialtyOutView> getAll(Pageable pageable){
        return service.findAll(pageable);
    }

    @PostMapping("/add")
    public SpecialtyOutView addOne(@RequestBody SpecialtyInView specialty){
        return service.addOne(specialty);
    }

}
