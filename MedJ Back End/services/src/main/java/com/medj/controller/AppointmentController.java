package com.medj.controller;

import com.medj.service.impl.AppointmentService;
import com.medj.view.inView.AppointmentInView;
import com.medj.view.outView.AppointmentOutView;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import tools.jackson.databind.ObjectMapper;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/appointment")
public class AppointmentController {

    @Autowired
    private AppointmentService service;

    @GetMapping("/all")
    public Page<AppointmentOutView> getAll(Pageable pageable){
        return service.findAll(pageable);
    }

    @PostMapping(path = "/add", consumes = { MediaType.MULTIPART_FORM_DATA_VALUE })
    public ResponseEntity<AppointmentOutView> addOne(@RequestPart AppointmentInView appointment, @RequestPart(name = "documents", required = false) List<Long> documents) throws Exception {

        AppointmentOutView result = service.addOne(appointment);
        return ResponseEntity.ok(result);
    }

    @GetMapping("/{id}")
    public Optional<AppointmentOutView> getById(@PathVariable("id") Long id){
        return service.findById(id);
    }

    @PutMapping("/{id}/practitioner/{practitionerId}")
    public ResponseEntity<AppointmentOutView> addPractitionerToAppointment(
            @PathVariable Long id,
            @PathVariable Long practitionerId) {

        return ResponseEntity.ok(
                service.addPractitionerToAppointment(id, practitionerId)
        );
    }

    @PutMapping("/{id}")
    public ResponseEntity<AppointmentOutView> updateAppointment(
            @PathVariable("id") Long id,
            @RequestBody AppointmentInView newAppointment) {

        AppointmentOutView response =
                service.updateAppointment(id,newAppointment);

        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteAppointment(@PathVariable("id") Long id){
        service.deleteAppointment(id);
        return ResponseEntity.ok().build();
    }
}
