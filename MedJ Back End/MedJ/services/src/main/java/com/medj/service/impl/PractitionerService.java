package com.medj.service.impl;

import com.medj.entities.Practitioner;
import com.medj.entities.Specialty;
import com.medj.exception.MedJEntityNotFound;
import com.medj.repositories.IPractitionerRepository;
import com.medj.repositories.ISpecialtyRepository;
import com.medj.service.IPractitionerService;
import com.medj.view.inView.PractitionerInView;
import com.medj.view.outView.PractitionerOutView;
import com.medj.view.outView.SpecialtyOutView;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

@Service
public class PractitionerService implements IPractitionerService {

    @Autowired
    private IPractitionerRepository practitionerRepository;

    @Autowired
    private ISpecialtyRepository specialtyRepository;

    @Autowired
    private ModelMapper modelMapper;

    @Override
    public PractitionerOutView addOne(PractitionerInView practitioner) {

        if(!specialtyRepository.existsById(practitioner.getSpecialtyId())){
            throw new RuntimeException("There is no specialty with id: " + practitioner.getSpecialtyId());
        }

        Practitioner mapped = modelMapper.map(practitioner, Practitioner.class);
        mapped.setIsActive(true);
        return modelMapper.map(practitionerRepository.save(mapped), PractitionerOutView.class);
    }

    @Override
    public Page<PractitionerOutView> findAll(Pageable pageable) {
        Page<Practitioner> specialties = practitionerRepository.findAllPractitioners(pageable);

        return specialties.map(p -> {
            PractitionerOutView view = modelMapper.map(p, PractitionerOutView.class);
            Specialty specialty = specialtyRepository.findSpecialtiesByIdAndByIsActive(p.getSpecialtyId());
            view.setSpecialty(modelMapper.map(specialty, SpecialtyOutView.class));
            return view;
        });
    }

    @Override
    public PractitionerOutView updatePractitioner(Long id, PractitionerInView newPractitioner){
        Practitioner practitioner = practitionerRepository.findPractitionerByIdAndByIsActive(id);

        if(practitioner==null){
            throw new MedJEntityNotFound("There is no such practitioner");
        }
        Specialty specialty;
        if(!newPractitioner.getSpecialtyId().equals(practitioner.getSpecialtyId())){
            specialty = specialtyRepository.findSpecialtiesByIdAndByIsActive(newPractitioner.getSpecialtyId());

            if(specialty==null){
                throw new MedJEntityNotFound("There is no such category");
            }

            practitioner.setSpecialtyId(specialty.getId());
        }else{
            specialty = specialtyRepository.findSpecialtiesByIdAndByIsActive(practitioner.getSpecialtyId());

            if(specialty == null){
                throw new MedJEntityNotFound("There is no such specialty");
            }
        }

        practitioner.setFirstName(newPractitioner.getFirstName());
        practitioner.setLastName(newPractitioner.getLastName());
        practitioner.setSpecialization(newPractitioner.getSpecialization());

        PractitionerOutView result =
                modelMapper.map(practitioner, PractitionerOutView.class);

        SpecialtyOutView specialtyOutView = modelMapper.map(specialty, SpecialtyOutView.class);

        result.setSpecialty(specialtyOutView);

        return result;
    }
}
