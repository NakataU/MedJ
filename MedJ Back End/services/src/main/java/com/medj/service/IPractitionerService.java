package com.medj.service;

import com.medj.view.inView.PractitionerInView;
import com.medj.view.outView.PractitionerOutView;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface IPractitionerService {

    public PractitionerOutView addOne(PractitionerInView practitioner);

    public Page<PractitionerOutView> findAll(Pageable pageable);

    public PractitionerOutView updatePractitioner(Long id, PractitionerInView newPractitioner);

    }
