package com.medj.controller;

import com.google.zxing.WriterException;
import com.medj.service.impl.MedicalSummaryService;
import com.medj.view.outView.MedicalSummaryResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.Map;

@RestController
@RequestMapping("/qr")
public class QrCodeController {

    @Autowired
    private MedicalSummaryService medicalSummaryService;

    @PostMapping("/generate")
    public ResponseEntity<MedicalSummaryResponse> generate(@RequestBody Map<String, String> body)
            throws IOException, WriterException {
        String summary = body.get("summary");
        String lang = body.getOrDefault("lang", "en");
        return ResponseEntity.ok(medicalSummaryService.generate(summary, lang));
    }
}
