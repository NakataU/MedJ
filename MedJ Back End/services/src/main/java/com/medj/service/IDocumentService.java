package com.medj.service;

import com.medj.view.outView.DocumentListOutView;
import com.medj.view.outView.DocumentOutView;
import org.springframework.core.io.Resource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

public interface IDocumentService {

    public DocumentOutView getById(Long id);

    public Resource openDocument(Long id);

    public Resource downloadDocument(Long id);

    public List<DocumentOutView> addMany(MultipartFile[] documents) throws Exception;

    public Page<DocumentListOutView> getAllByUserId(Long id, Pageable pageable);

    public Page<DocumentListOutView> getAllByAppointmentId(Long id, Pageable pageable);

    public void deleteDocument(Long id);

    public String generateSummary(String prompt) throws IOException;

}
