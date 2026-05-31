package com.medj.service.impl;

import com.google.cloud.vertexai.VertexAI;
import com.google.cloud.vertexai.api.GenerateContentResponse;
import com.google.cloud.vertexai.api.GenerationConfig;
import com.google.cloud.vertexai.generativeai.GenerativeModel;
import com.google.cloud.vertexai.generativeai.ResponseHandler;
import com.medj.config.GeminiClient;
import com.medj.entities.AppointmentDocuments;
import com.medj.entities.Document;
import com.medj.exception.MedJFileAlreadyExists;
import com.medj.models.DocumentSummaryProjection;
import com.medj.repositories.IAppointmentDocumentsRepository;
import com.medj.repositories.IDocumentRepository;
import com.medj.service.IDocumentService;
import com.medj.view.outView.DocumentListOutView;
import com.medj.view.outView.DocumentOutView;
import com.medj.view.outView.SummaryResponse;
import jakarta.persistence.EntityNotFoundException;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.MessageDigest;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Slf4j
public class DocumentService implements IDocumentService {

    @Autowired
    private IDocumentRepository documentRepository;

    @Autowired
    private IAppointmentDocumentsRepository appointmentDocumentsRepository;

    @Autowired
    private ModelMapper modelMapper;

    @Autowired
    private OcrService ocrService;

    @Autowired
    private VertexAI vertexAI;

    @Override
    public DocumentOutView getById(Long id) {
        log.info("getById started, id={}", id);

        Document document  = documentRepository.findByIdAndByIsActive(id);
        DocumentOutView result = modelMapper.map(document, DocumentOutView.class);

        result.setSize(formatSize(document.getSize()));
        return result;
    }

    @Override
    public Resource openDocument(Long id) {
        log.info("openDocument started, id={}", id);

        Document document = documentRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Document not found"));

        try {
            Path path = Paths.get(document.getPath()).normalize();
            Resource resource = new UrlResource(path.toUri());

            if (!resource.exists()) {
                throw new FileNotFoundException("File not found");
            }

            return resource;
        } catch (Exception e) {
            throw new RuntimeException("Could not open document", e);
        }
    }

    @Override
    public Resource downloadDocument(Long id) {
        log.info("downloadDocument started, id={}", id);

        Document document = documentRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Document not found"));

        try {
            Path path = Paths.get(document.getPath()).normalize();
            Resource resource = new UrlResource(path.toUri());

            if (!resource.exists()) {
                throw new FileNotFoundException("File not found");
            }

            return resource;
        } catch (Exception e) {
            throw new RuntimeException("Could not download document", e);
        }
    }

    private static final List<String> ALLOWED_CONTENT_TYPES = List.of(
            "application/pdf",
            "image/jpeg",
            "image/png"
    );

    @Override
    public List<DocumentOutView> addMany(MultipartFile[] documents) throws Exception {
        log.info("addOne started");
        List<DocumentOutView> result = new ArrayList<>();

        for (MultipartFile document : documents) {
            String contentType = document.getContentType();
            if (contentType == null || !ALLOWED_CONTENT_TYPES.contains(contentType.toLowerCase())) {
                throw new IllegalArgumentException(
                        "Unsupported file type: " + document.getOriginalFilename()
                        + ". Only PDF, JPG and PNG files are allowed.");
            }

            String checksum = generateChecksum(document, "SHA-256");
            if (documentRepository.existsByChecksumAndIsActive(checksum, true)) {
                throw new MedJFileAlreadyExists("The file " + document.getOriginalFilename() + " already exists");
            }

            String uploadDir = "./uploads";
            File dir = new File(uploadDir);
            if (!dir.exists()) dir.mkdirs();

            // Save file locally
            Path filePath = Paths.get(uploadDir, document.getOriginalFilename());
            Files.write(filePath, document.getBytes());

            // Run OCR on saved file
            String ocrText = null;
            try {
                ocrText = ocrService.extractText(filePath);
            } catch (Exception e) {
                log.warn("OCR failed for file {}: {}", document.getOriginalFilename(), e.getMessage());
            }

            // Save metadata + OCR content to DB
            Document doc = new Document();
            doc.setFileName(document.getOriginalFilename());
            doc.setContentType(document.getContentType());
            doc.setPath(filePath.toString());
            doc.setSize(document.getSize());
            doc.setChecksum(checksum);
            doc.setUploadedByUserId(1L);
            doc.setIsActive(true);
            doc.setContent(ocrText); // null if OCR failed or not applicable

            result.add(modelMapper.map(documentRepository.save(doc), DocumentOutView.class));
        }

        return result;
    }


    @Override
    public Page<DocumentListOutView> getAllByUserId(Long id, Pageable pageable) {
        log.info("getAllByUserId starts");

        Page<Document> documents = documentRepository.findAllDocumentsByIsActive(id, pageable);

        return documents.map(doc -> {
            DocumentListOutView view = modelMapper.map(doc, DocumentListOutView.class);
            view.setSize(formatSize(doc.getSize()));
            return view;
        });
    }

    public Page<DocumentListOutView> getAllByUserIdFiltered(Long userId, Long documentTypeId,
                                                            Long medicalSpecialtyId, Long medicalCategoryId,
                                                            Pageable pageable) {
        log.info("getAllByUserIdFiltered starts");

        Page<Document> documents = documentRepository.findAllDocumentsFiltered(
                userId, documentTypeId, medicalSpecialtyId, medicalCategoryId, pageable);

        return documents.map(doc -> {
            DocumentListOutView view = modelMapper.map(doc, DocumentListOutView.class);
            view.setSize(formatSize(doc.getSize()));
            return view;
        });
    }

    @Override
    public Page<DocumentListOutView> getAllByAppointmentId(Long id, Pageable pageable) {
        log.info("getAllByAppointmentId starts");

        Page<Document> documents = documentRepository.findAllDocumentsByAppointmentId(id, pageable);

        return documents.map(doc -> {
            DocumentListOutView view = modelMapper.map(doc, DocumentListOutView.class);
            view.setSize(formatSize(doc.getSize()));
            return view;
        });
    }

    @Override
    public void deleteDocument(Long id){
        log.info("deleteDocument starts");

        Document document = documentRepository.findByIdAndByIsActive(id);
        //AppointmentDocuments appointmentDocument = appointmentDocumentsRepository.findAppointmentDocumentsByDocumentIdAndIsActive(id);

        Path filePath = Paths.get("uploads").resolve(document.getFileName());
        try {
            Files.deleteIfExists(filePath);
        } catch (IOException e) {
            throw new RuntimeException("Failed to delete file from disk", e);
        }
        document.setIsActive(false);
        documentRepository.save(document);

        //appointmentDocument.setIsActive(false);
        //appointmentDocumentsRepository.save(appointmentDocument);
    }

    @Override
    public DocumentOutView updateContent(Long id, String content) {
        log.info("updateContent started, id={}", id);
        Document document = documentRepository.findByIdAndByIsActive(id);
        document.setContent(content);
        Document saved = documentRepository.save(document);
        DocumentOutView result = modelMapper.map(saved, DocumentOutView.class);
        result.setSize(formatSize(saved.getSize()));
        return result;
    }

    public void updateCategories(Long id, Long documentTypeId, Long medicalSpecialtyId, Long medicalCategoryId) {
        log.info("updateCategories started, id={}", id);
        Document document = documentRepository.findByIdAndByIsActive(id);
        document.setDocumentTypeId(documentTypeId);
        document.setMedicalSpecialtyId(medicalSpecialtyId);
        document.setMedicalCategoryId(medicalCategoryId);
        documentRepository.save(document);
    }

    @Override
    public SummaryResponse generateSummary(String prompt, String lang) throws IOException {
        log.info("generateSummary started");

        List<DocumentSummaryProjection> projections = documentRepository.findContentByUser(1L)
                .stream()
                .filter(p -> p.getContent() != null && !p.getContent().isBlank())
                .collect(Collectors.toList());

        List<String> documentTexts = projections.stream()
                .map(DocumentSummaryProjection::getContent)
                .collect(Collectors.toList());

        String summaryText = summarize(prompt, documentTexts, lang);

        List<DocumentListOutView> usedDocs = projections.stream().map(p -> {
            DocumentListOutView view = new DocumentListOutView();
            view.setId(p.getId());
            view.setFileName(p.getFileName());
            view.setCreatedOn(p.getCreatedAt());
            return view;
        }).collect(Collectors.toList());

        SummaryResponse response = new SummaryResponse();
        response.setSummary(summaryText);
        response.setUsedDocuments(usedDocs);
        return response;
    }

    private static final String SYSTEM_INSTRUCTION_EN = """
            You are an experienced medical professional summarizing patient documents.
            Produce clear, clinically accurate summaries using appropriate medical terminology.
            IMPORTANT: Always respond in English.
            Structure your output with these sections when relevant:
            - Chief Complaint
            - Relevant History
            - Findings / Results
            - Assessment
            - Recommendations

            If information is missing or ambiguous, state so explicitly rather than inferring.
            Do not fabricate diagnoses, dosages, or lab values.
            """;

    private static final String SYSTEM_INSTRUCTION_BG = """
            You are an experienced medical professional summarizing patient documents.
            Produce clear, clinically accurate summaries using appropriate medical terminology.
            IMPORTANT: Always respond in Bulgarian language.
            Structure your output with these sections when relevant:
            - Основно оплакване
            - Релевантна история
            - Находки / Резултати
            - Оценка
            - Препоръки

            If information is missing or ambiguous, state so explicitly rather than inferring.
            Do not fabricate diagnoses, dosages, or lab values.
            """;

    @Value("${gemini.model}")
    private String modelName;

    public String summarize(String userPrompt, List<String> documentTexts, String lang) throws IOException {
        GenerativeModel model = new GenerativeModel.Builder()
                .setModelName(modelName)
                .setVertexAi(vertexAI)
                .setGenerationConfig(GenerationConfig.newBuilder()
                        .setTemperature(0.2f)
                        .setMaxOutputTokens(2048)
                        .build())
                .build();

        String combinedInput = buildInput(userPrompt, documentTexts, lang);
        GenerateContentResponse response = model.generateContent(combinedInput);
        return ResponseHandler.getText(response);
    }

    private String buildInput(String userPrompt, List<String> documentTexts, String lang) {
        String systemInstruction = "bg".equalsIgnoreCase(lang) ? SYSTEM_INSTRUCTION_BG : SYSTEM_INSTRUCTION_EN;
        StringBuilder sb = new StringBuilder();
        sb.append(systemInstruction).append("\n\n");
        sb.append("User request: ").append(userPrompt).append("\n\n");
        sb.append("Documents to summarize:\n");
        for (int i = 0; i < documentTexts.size(); i++) {
            sb.append("--- Document ").append(i + 1).append(" ---\n");
            sb.append(documentTexts.get(i)).append("\n\n");
        }
        return sb.toString();
    }

    public static String generateChecksum(MultipartFile file, String algorithm) throws Exception {
        MessageDigest digest = MessageDigest.getInstance(algorithm);
        byte[] hash = digest.digest(file.getBytes());

        StringBuilder hexString = new StringBuilder();
        for (byte b : hash) {
            String hex = Integer.toHexString(0xff & b);
            if (hex.length() == 1) hexString.append('0');
            hexString.append(hex);
        }
        return hexString.toString();
    }

    public static String formatSize(Long bytes) {
        if (bytes == null || bytes < 0) return "0 B";

        final String[] units = {"B", "KB", "MB", "GB", "TB"};
        double size = bytes;
        int index = 0;

        while (size >= 1024 && index < units.length - 1) {
            size /= 1024;
            index++;
        }

        return String.format("%.2f %s", size, units[index]);
    }
}
