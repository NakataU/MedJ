package com.medj.controller;

import com.medj.entities.Document;
import com.medj.service.impl.DocumentService;
import com.medj.view.outView.DocumentListOutView;
import com.medj.view.outView.DocumentOutView;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/document")
public class DocumentController {

    @Autowired
    private DocumentService service;

    @GetMapping("/{id}")
    public ResponseEntity<DocumentOutView> getOne(@PathVariable("id") Long id){
        return ResponseEntity.ok(service.getById(id));
    }

    @GetMapping("/{id}/content")
    public ResponseEntity<Resource> openDocument(@PathVariable Long id) {
        DocumentOutView document = service.getById(id);
        Resource resource = service.openDocument(id);

        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(document.getContentType()))
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        "inline; filename=\"" + document.getFileName() + "\"")
                .body(resource);
    }

    @GetMapping("/{id}/download")
    public ResponseEntity<Resource> downloadDocument(@PathVariable Long id) {
        DocumentOutView document = service.getById(id);
        Resource resource = service.downloadDocument(id);

        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        "attachment; filename=\"" + document.getFileName() + "\"")
                .body(resource);
    }

    @PostMapping("/add")
    public ResponseEntity<List<DocumentOutView>> addMany(@RequestParam("document") MultipartFile[] documents) throws Exception {
        return ResponseEntity.ok(service.addMany(documents));
    }

    @GetMapping("/all/{id}")
    public ResponseEntity<Page<DocumentListOutView>> allDocumentsForUser(
            @PathVariable("id") Long id,
            Pageable pageable) {
        Page<DocumentListOutView> documents = service.getAllByUserId(id, pageable);
        return ResponseEntity.ok(documents);
    }

    @GetMapping("/all/byAppointment/{id}")
    public ResponseEntity<Page<DocumentListOutView>> allDocumentsForAppointment(
            @PathVariable("id") Long id,
            Pageable pageable) {
        Page<DocumentListOutView> documents = service.getAllByAppointmentId(id, pageable);
        return ResponseEntity.ok(documents);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity deleteDocument(@PathVariable("id") Long id){
        service.deleteDocument(id);
        return ResponseEntity.ok().build();
    }

    @PostMapping(value = "/medical-summary")
    public ResponseEntity<String> getSummary(@RequestBody String prompt) throws IOException {
        String summary = service.generateSummary(prompt);
        return ResponseEntity.ok(summary);
    }
}
