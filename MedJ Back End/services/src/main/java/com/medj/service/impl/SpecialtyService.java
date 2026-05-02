package com.medj.service.impl;

import com.medj.entities.Specialty;
import com.medj.repositories.ISpecialtyRepository;
import com.medj.service.ISpecialtyService;
import com.medj.view.inView.SpecialtyInView;
import com.medj.view.outView.SpecialtyOutView;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

@Service
public class SpecialtyService implements ISpecialtyService {

    @Autowired
    private ISpecialtyRepository specialtyRepository;

    @Autowired
    private ModelMapper modelMapper;

    @Override
    public SpecialtyOutView addOne(SpecialtyInView specialty){

        Specialty mapped = modelMapper.map(specialty, Specialty.class);
        mapped.setIsActive(true);
        return modelMapper.map(specialtyRepository.save(mapped), SpecialtyOutView.class);

    }

    @Override
    public Page<SpecialtyOutView> findAll(Pageable pageable) {

        Page<Specialty> specialties = specialtyRepository.findAllSpecialtiesByIsActive(pageable);

        return specialties.map(spec -> {
            SpecialtyOutView view = modelMapper.map(spec, SpecialtyOutView.class);
            return view;
        });
    }
}
