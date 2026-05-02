package com.medj.controller;

import com.google.zxing.WriterException;
import com.medj.service.impl.MedicalSummaryService;
import com.medj.view.outView.MedicalSummaryResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;

@CrossOrigin(origins = "http://localhost:5173")
@RestController
@RequestMapping("/qr")
public class QrCodeController {

    @Autowired
    private MedicalSummaryService medicalSummaryService;

    @PostMapping("/generate")
    public ResponseEntity<MedicalSummaryResponse> generate(@RequestBody String prompt)
            throws IOException, WriterException {
        return ResponseEntity.ok(medicalSummaryService.generate(prompt));
    }
}
