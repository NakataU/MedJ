package com.medj.service.impl;

import com.google.zxing.common.BitMatrix;
import com.google.zxing.WriterException;
import com.medj.entities.ApplicationUser;
import com.medj.repositories.IApplicationUserRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.extern.slf4j.Slf4j;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.apache.pdfbox.pdmodel.font.Standard14Fonts;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

@Service
@Slf4j
public class MedicalCardService {

    private static final float MARGIN = 50f;
    private static final float PAGE_WIDTH = PDRectangle.A4.getWidth();
    private static final float PAGE_HEIGHT = PDRectangle.A4.getHeight();

    @Value("${app.base-url}")
    private String baseUrl;

    @Autowired
    private IApplicationUserRepository userRepository;

    @Autowired
    private DocumentService documentService;

    @Autowired
    private QrCodeService qrCodeService;

    @Autowired
    private SummaryStore summaryStore;

    public byte[] generate(String prompt) throws IOException, WriterException {
        log.info("MedicalCardService.generate started");

        ApplicationUser user = userRepository.findById(1L)
                .orElseThrow(() -> new EntityNotFoundException("User not found"));

        String summary = documentService.generateSummary(prompt, "en").getSummary();
        String token = summaryStore.save(user.getUsername(), summary);

        String url = baseUrl + "/summary/" + token;
        BitMatrix qrMatrix = qrCodeService.getBitMatrix(url);

        return buildPdf(user.getUsername(), summary, qrMatrix);
    }

    private byte[] buildPdf(String username, String summary, BitMatrix qrMatrix) throws IOException {
        try (PDDocument doc = new PDDocument()) {
            PDPage page = new PDPage(PDRectangle.A4);
            doc.addPage(page);

            PDType1Font fontBold   = new PDType1Font(Standard14Fonts.FontName.HELVETICA_BOLD);
            PDType1Font fontNormal = new PDType1Font(Standard14Fonts.FontName.HELVETICA);

            try (PDPageContentStream cs = new PDPageContentStream(doc, page)) {

                float y = PAGE_HEIGHT - MARGIN;

                // ── Header bar ──────────────────────────────────────────
                cs.setNonStrokingColor(0.18f, 0.42f, 0.65f);
                cs.addRect(0, y - 10, PAGE_WIDTH, 46);
                cs.fill();

                cs.setNonStrokingColor(1f, 1f, 1f);
                drawText(cs, "MedJ — Medical Summary Card", fontBold, 16, MARGIN, y + 14);

                y -= 60;

                // ── Date ────────────────────────────────────────────────
                cs.setNonStrokingColor(0.4f, 0.4f, 0.4f);
                drawText(cs, "Generated: " + LocalDate.now().format(DateTimeFormatter.ofPattern("dd MMM yyyy")),
                        fontNormal, 9, MARGIN, y);
                y -= 24;

                // ── Divider ─────────────────────────────────────────────
                cs.setStrokingColor(0.18f, 0.42f, 0.65f);
                cs.setLineWidth(1f);
                cs.moveTo(MARGIN, y);
                cs.lineTo(PAGE_WIDTH - MARGIN, y);
                cs.stroke();
                y -= 20;

                // ── Patient block ────────────────────────────────────────
                cs.setNonStrokingColor(0f, 0f, 0f);
                drawText(cs, "Patient", fontBold, 11, MARGIN, y);
                y -= 16;
                drawText(cs, username, fontNormal, 11, MARGIN, y);
                y -= 28;

                // ── Divider ─────────────────────────────────────────────
                cs.setStrokingColor(0.85f, 0.85f, 0.85f);
                cs.moveTo(MARGIN, y);
                cs.lineTo(PAGE_WIDTH - MARGIN, y);
                cs.stroke();
                y -= 20;

                // ── Summary section ──────────────────────────────────────
                float qrSize  = 200f;
                float textWidth = PAGE_WIDTH - MARGIN * 2 - qrSize - 20;

                drawText(cs, "Medical Summary", fontBold, 11, MARGIN, y);
                y -= 18;

                List<String> lines = wrapText(summary, fontNormal, 10, textWidth);

                float textStartY = y;
                for (String line : lines) {
                    if (y < MARGIN + qrSize) break;
                    cs.setNonStrokingColor(0.1f, 0.1f, 0.1f);
                    drawText(cs, line, fontNormal, 10, MARGIN, y);
                    y -= 14;
                }

                // ── QR code as vector (top-right of summary area) ────────
                float qrX = PAGE_WIDTH - MARGIN - qrSize;
                float qrY = textStartY - qrSize;
                drawQrVector(cs, qrMatrix, qrX, qrY, qrSize);

                cs.setNonStrokingColor(0.5f, 0.5f, 0.5f);
                drawText(cs, "Scan for summary", fontNormal, 8,
                        qrX + (qrSize / 2) - 28, qrY - 12);

                // ── Footer ───────────────────────────────────────────────
                cs.setNonStrokingColor(0.18f, 0.42f, 0.65f);
                cs.addRect(0, 0, PAGE_WIDTH, 28);
                cs.fill();
                cs.setNonStrokingColor(1f, 1f, 1f);
                drawText(cs, "This document is auto-generated by MedJ and is for informational purposes only.",
                        fontNormal, 7, MARGIN, 10);
            }

            ByteArrayOutputStream out = new ByteArrayOutputStream();
            doc.save(out);
            return out.toByteArray();
        }
    }

    private void drawQrVector(PDPageContentStream cs, BitMatrix matrix,
                              float x, float y, float size) throws IOException {
        int modules = matrix.getWidth();
        float moduleSize = size / modules;

        cs.setNonStrokingColor(0f, 0f, 0f);
        for (int row = 0; row < modules; row++) {
            for (int col = 0; col < modules; col++) {
                if (matrix.get(col, row)) {
                    float px = x + col * moduleSize;
                    // PDF Y is bottom-up, QR row 0 is top — flip vertically
                    float py = y + (modules - 1 - row) * moduleSize;
                    cs.addRect(px, py, moduleSize, moduleSize);
                }
            }
        }
        cs.fill();
    }

    private void drawText(PDPageContentStream cs, String text, PDType1Font font,
                          float size, float x, float y) throws IOException {
        cs.beginText();
        cs.setFont(font, size);
        cs.newLineAtOffset(x, y);
        cs.showText(text);
        cs.endText();
    }

    private List<String> wrapText(String text, PDType1Font font, float fontSize, float maxWidth) throws IOException {
        List<String> lines = new ArrayList<>();
        for (String paragraph : text.split("\n")) {
            String[] words = paragraph.split(" ");
            StringBuilder line = new StringBuilder();
            for (String word : words) {
                String test = line.isEmpty() ? word : line + " " + word;
                float w = font.getStringWidth(test) / 1000 * fontSize;
                if (w > maxWidth && !line.isEmpty()) {
                    lines.add(line.toString());
                    line = new StringBuilder(word);
                } else {
                    line = new StringBuilder(test);
                }
            }
            if (!line.isEmpty()) lines.add(line.toString());
            lines.add("");
        }
        return lines;
    }
}
