package com.medj.controller;

import com.medj.service.impl.PractitionerService;
import com.medj.view.inView.PractitionerInView;
import com.medj.view.outView.PractitionerOutView;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@CrossOrigin(origins = "http://localhost:5173")
@RestController
@RequestMapping("/practitioner")
public class PractitionerController {

    @Autowired
    private PractitionerService service;

    @GetMapping("/all")
    public Page<PractitionerOutView> getAll(Pageable pageable){
        return service.findAll(pageable);
    }

    @PostMapping("/add")
    public PractitionerOutView addOne(@RequestBody PractitionerInView specialty){
        return service.addOne(specialty);
    }

    @PutMapping("/{id}")
    public ResponseEntity<PractitionerOutView> updatePractitioner(@PathVariable("id") Long id, @RequestBody PractitionerInView newPractitioner){
        PractitionerOutView response = service.updatePractitioner(id, newPractitioner);
        return ResponseEntity.ok(response);
    }
}
