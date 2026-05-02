package com.medj.service;

import com.medj.view.inView.SpecialtyInView;
import com.medj.view.outView.SpecialtyOutView;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface ISpecialtyService {

    public SpecialtyOutView addOne(SpecialtyInView specialty);

    public Page<SpecialtyOutView> findAll(Pageable pageable);

}
