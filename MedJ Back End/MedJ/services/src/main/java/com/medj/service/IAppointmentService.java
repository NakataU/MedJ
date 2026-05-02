package com.medj.service;

import com.medj.view.inView.AppointmentInView;
import com.medj.view.outView.AppointmentOutView;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.multipart.MultipartFile;

import java.util.Optional;

public interface IAppointmentService {

    public AppointmentOutView addOne(AppointmentInView appointment) throws Exception;

    public Page<AppointmentOutView> findAll(Pageable pageable);

    public Optional<AppointmentOutView> findById(Long id);

    public AppointmentOutView addPractitionerToAppointment(Long id, Long practitionerId);

    public AppointmentOutView updateAppointment(Long id, AppointmentInView newAppointment);

    public void deleteAppointment(Long id);
    }
