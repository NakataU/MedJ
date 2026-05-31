package com.medj.service.impl;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.EncodeHintType;
import com.google.zxing.WriterException;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;
import com.google.zxing.qrcode.decoder.ErrorCorrectionLevel;
import com.medj.entities.ApplicationUser;
import com.medj.repositories.IApplicationUserRepository;
import com.medj.view.outView.MedicalSummaryResponse;
import jakarta.persistence.EntityNotFoundException;
import lombok.extern.slf4j.Slf4j;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.font.PDFont;
import org.apache.pdfbox.pdmodel.font.PDType0Font;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.apache.pdfbox.pdmodel.font.Standard14Fonts;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import java.util.Map;

@Service
@Slf4j
public class MedicalSummaryService {

    private static final float MARGIN      = 50f;
    private static final float PAGE_WIDTH  = PDRectangle.A4.getWidth();
    private static final float PAGE_HEIGHT = PDRectangle.A4.getHeight();

    @Value("${app.base-url}")
    private String baseUrl;

    @Autowired
    private IApplicationUserRepository userRepository;

    @Autowired
    private DocumentService documentService;

    @Autowired
    private SummaryStore summaryStore;

    /**
     * Generates both the QR code PNG and the medical card PDF in one call.
     * Returns base64-encoded versions of each so the frontend can handle both
     * from a single JSON response.
     */
    public MedicalSummaryResponse generate(String summary, String lang) throws IOException, WriterException {
        log.info("MedicalSummaryService.generate started");

        ApplicationUser user = userRepository.findById(1L)
                .orElseThrow(() -> new EntityNotFoundException("User not found"));

        String patientName = buildPatientName(user);

        // Persist summary and get shareable token
        String token   = summaryStore.save(user.getUsername(), summary);
        String pageUrl = baseUrl + "/summary/" + token;

        // Build QR code PNG (encodes the shareable URL)
        byte[] qrPng = generateQrPng(pageUrl, 500, 500);

        // Build PDF card (embeds the QR as vector graphics)
        BitMatrix qrMatrix = buildBitMatrix(pageUrl);
        byte[] pdf         = buildPdf(patientName, summary, qrMatrix, lang);

        return new MedicalSummaryResponse(
                Base64.getEncoder().encodeToString(qrPng),
                Base64.getEncoder().encodeToString(pdf)
        );
    }

    private String buildPatientName(ApplicationUser user) {
        String first = user.getFirstName();
        String last = user.getLastName();
        if (first != null && !first.isBlank() && last != null && !last.isBlank()) {
            return first.trim() + " " + last.trim();
        }
        if (first != null && !first.isBlank()) return first.trim();
        if (last != null && !last.isBlank()) return last.trim();
        return user.getUsername();
    }

    // ── QR helpers ────────────────────────────────────────────────────────────

    private BitMatrix buildBitMatrix(String content) throws WriterException {
        return new QRCodeWriter().encode(
                content,
                BarcodeFormat.QR_CODE,
                1, 1,
                Map.of(
                        EncodeHintType.CHARACTER_SET,    "UTF-8",
                        EncodeHintType.ERROR_CORRECTION, ErrorCorrectionLevel.L,
                        EncodeHintType.MARGIN,           1
                )
        );
    }

    private byte[] generateQrPng(String content, int width, int height) throws WriterException, IOException {
        BitMatrix matrix = new QRCodeWriter().encode(
                content,
                BarcodeFormat.QR_CODE,
                width, height,
                Map.of(
                        EncodeHintType.CHARACTER_SET,    "UTF-8",
                        EncodeHintType.ERROR_CORRECTION, ErrorCorrectionLevel.L,
                        EncodeHintType.MARGIN,           1
                )
        );
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        MatrixToImageWriter.writeToStream(matrix, "PNG", out);
        return out.toByteArray();
    }

    // ── PDF helpers ───────────────────────────────────────────────────────────

    private PDFont loadFont(PDDocument doc, String... paths) throws IOException {
        for (String path : paths) {
            File f = new File(path);
            if (f.exists()) return PDType0Font.load(doc, f);
        }
        return new PDType1Font(Standard14Fonts.FontName.HELVETICA);
    }

    private byte[] buildPdf(String patientName, String summary, BitMatrix qrMatrix, String lang) throws IOException {
        boolean bg = "bg".equalsIgnoreCase(lang);
        String headerTitle     = bg ? "MedJ — Медицинска карта" : "MedJ — Medical Summary Card";
        String generatedLabel  = bg ? "Генерирано: " : "Generated: ";
        String patientLabel    = bg ? "Пациент" : "Patient";
        String scanLabel       = bg ? "Сканирай за резюме" : "Scan for summary";
        String summaryLabel    = bg ? "Медицинско резюме" : "Medical Summary";
        String footerText      = bg ? "Този документ е автоматично генериран от MedJ и е само за информационни цели."
                               : "This document is auto-generated by MedJ and is for informational purposes only.";

        try (PDDocument doc = new PDDocument()) {

            PDFont fontBold = loadFont(doc,
                    "C:/Windows/Fonts/arialbd.ttf",
                    "/usr/share/fonts/truetype/dejavu/DejaVuSans-Bold.ttf",
                    "/System/Library/Fonts/Supplemental/Arial Bold.ttf");
            PDFont fontNormal = loadFont(doc,
                    "C:/Windows/Fonts/arial.ttf",
                    "/usr/share/fonts/truetype/dejavu/DejaVuSans.ttf",
                    "/System/Library/Fonts/Supplemental/Arial.ttf");

            float textWidth = PAGE_WIDTH - MARGIN * 2;
            float footerHeight = 36f;
            float footerLimit = MARGIN + footerHeight;

            // Parse summary into blocks (not pre-wrapped)
            List<PdfBlock> blocks = parseSummary(summary);

            // --- Page 1: header, patient info, QR, start of summary ---
            PDPage page1 = new PDPage(PDRectangle.A4);
            doc.addPage(page1);
            PDPageContentStream cs = new PDPageContentStream(doc, page1);

            float y = PAGE_HEIGHT - MARGIN;

            // Header bar
            cs.setNonStrokingColor(0.18f, 0.42f, 0.65f);
            cs.addRect(0, y - 10, PAGE_WIDTH, 46);
            cs.fill();
            cs.setNonStrokingColor(1f, 1f, 1f);
            drawText(cs, headerTitle, fontBold, 16, MARGIN, y + 14);
            y -= 60;

            // Date
            cs.setNonStrokingColor(0.4f, 0.4f, 0.4f);
            drawText(cs, generatedLabel + LocalDate.now().format(DateTimeFormatter.ofPattern("dd MMM yyyy")),
                    fontNormal, 9, MARGIN, y);
            y -= 24;

            // Divider
            cs.setStrokingColor(0.18f, 0.42f, 0.65f);
            cs.setLineWidth(1f);
            cs.moveTo(MARGIN, y);
            cs.lineTo(PAGE_WIDTH - MARGIN, y);
            cs.stroke();
            y -= 20;

            // Patient block
            cs.setNonStrokingColor(0f, 0f, 0f);
            drawText(cs, patientLabel, fontBold, 11, MARGIN, y);
            y -= 16;
            drawText(cs, patientName, fontNormal, 11, MARGIN, y);
            y -= 28;

            // Divider
            cs.setStrokingColor(0.85f, 0.85f, 0.85f);
            cs.moveTo(MARGIN, y);
            cs.lineTo(PAGE_WIDTH - MARGIN, y);
            cs.stroke();
            y -= 20;

            // QR code (top-right)
            float qrSize = 120f;
            float qrX = PAGE_WIDTH - MARGIN - qrSize;
            float qrY = y - qrSize;
            drawQrVector(cs, qrMatrix, qrX, qrY, qrSize);
            cs.setNonStrokingColor(0.5f, 0.5f, 0.5f);
            drawText(cs, scanLabel, fontNormal, 7,
                    qrX + (qrSize / 2) - 28, qrY - 10);

            // Summary section header
            cs.setNonStrokingColor(0.18f, 0.42f, 0.65f);
            drawText(cs, summaryLabel, fontBold, 12, MARGIN, y);
            y -= 22;

            // On page 1, text narrows to avoid QR area
            float qrBottomY = qrY - 16;
            float narrowWidth = PAGE_WIDTH - MARGIN * 2 - qrSize - 16;

            int blockIdx = 0;

            // Render blocks on page 1
            for (; blockIdx < blocks.size(); blockIdx++) {
                PdfBlock block = blocks.get(blockIdx);
                float availWidth = (y > qrBottomY) ? narrowWidth : textWidth;

                float beforeY = y;
                y = renderBlock(cs, block, y, MARGIN, availWidth, fontBold, fontNormal, footerLimit);

                if (y <= footerLimit && beforeY > footerLimit) {
                    y = beforeY;
                    break;
                }
            }

            drawFooter(cs, fontNormal, footerText);
            cs.close();

            // --- Additional pages if blocks remain ---
            while (blockIdx < blocks.size()) {
                PDPage nextPage = new PDPage(PDRectangle.A4);
                doc.addPage(nextPage);
                cs = new PDPageContentStream(doc, nextPage);

                y = PAGE_HEIGHT - MARGIN;

                for (; blockIdx < blocks.size(); blockIdx++) {
                    PdfBlock block = blocks.get(blockIdx);
                    float beforeY1 = y;
                    y = renderBlock(cs, block, y, MARGIN, textWidth, fontBold, fontNormal, footerLimit);

                    if (y <= footerLimit && beforeY1 > footerLimit) {
                        y = beforeY1;
                        break;
                    }
                }

                drawFooter(cs, fontNormal, footerText);
                cs.close();
            }

            ByteArrayOutputStream out = new ByteArrayOutputStream();
            doc.save(out);
            return out.toByteArray();
        }
    }

    private void drawFooter(PDPageContentStream cs, PDFont fontNormal, String text) throws IOException {
        cs.setNonStrokingColor(0.18f, 0.42f, 0.65f);
        cs.addRect(0, 0, PAGE_WIDTH, 28);
        cs.fill();
        cs.setNonStrokingColor(1f, 1f, 1f);
        drawText(cs, text, fontNormal, 7, MARGIN, 10);
    }

    private record PdfBlock(String type, String rawText) {}

    private List<PdfBlock> parseSummary(String summary) {
        List<PdfBlock> result = new ArrayList<>();
        String cleaned = summary.replace("\\n", "\n").replace("**", "");

        for (String rawLine : cleaned.split("\n")) {
            String trimmed = rawLine.trim();

            if (trimmed.isEmpty()) {
                result.add(new PdfBlock("space", ""));
            } else if (trimmed.startsWith("### ")) {
                result.add(new PdfBlock("space", ""));
                result.add(new PdfBlock("heading", trimmed.substring(4).trim()));
            } else if (trimmed.startsWith("## ")) {
                result.add(new PdfBlock("space", ""));
                result.add(new PdfBlock("section", trimmed.substring(3).trim()));
            } else if (trimmed.startsWith("# ")) {
                result.add(new PdfBlock("space", ""));
                result.add(new PdfBlock("title", trimmed.substring(2).trim()));
            } else if (trimmed.startsWith("- ") || trimmed.startsWith("* ")) {
                result.add(new PdfBlock("bullet", trimmed.substring(2).trim()));
            } else {
                result.add(new PdfBlock("text", trimmed));
            }
        }
        return result;
    }

    private float renderBlock(PDPageContentStream cs, PdfBlock block, float y, float margin,
                              float maxWidth, PDFont fontBold, PDFont fontNormal,
                              float footerLimit) throws IOException {
        if ("space".equals(block.type)) {
            return y - 8;
        }

        PDFont font;
        float fontSize;
        float lineHeight;
        float indent = 0;
        float r = 0.1f, g = 0.1f, b = 0.1f;

        switch (block.type) {
            case "title":
                font = fontBold; fontSize = 12; lineHeight = 20;
                r = 0.12f; g = 0.30f; b = 0.55f;
                break;
            case "section":
                font = fontBold; fontSize = 11; lineHeight = 18;
                r = 0.18f; g = 0.42f; b = 0.65f;
                break;
            case "heading":
                font = fontBold; fontSize = 10; lineHeight = 16;
                r = 0.2f; g = 0.2f; b = 0.2f;
                break;
            case "bullet":
                font = fontNormal; fontSize = 10; lineHeight = 14;
                indent = 16;
                break;
            default:
                font = fontNormal; fontSize = 10; lineHeight = 14;
                break;
        }

        float wrapWidth = maxWidth - indent;
        List<String> lines = wrapText(block.rawText, font, fontSize, wrapWidth);

        for (int i = 0; i < lines.size(); i++) {
            String line = lines.get(i);
            if (line.isEmpty()) continue;
            if (y - lineHeight < footerLimit) break;

            if ("bullet".equals(block.type) && i == 0) {
                cs.setNonStrokingColor(0.18f, 0.42f, 0.65f);
                drawText(cs, "•", fontNormal, fontSize, margin + 4, y);
            }

            cs.setNonStrokingColor(r, g, b);
            drawText(cs, line, font, fontSize, margin + indent, y);
            y -= lineHeight;
        }
        return y;
    }

    private void drawQrVector(PDPageContentStream cs, BitMatrix matrix,
                              float x, float y, float size) throws IOException {
        int modules    = matrix.getWidth();
        float modSize  = size / modules;
        cs.setNonStrokingColor(0f, 0f, 0f);
        for (int row = 0; row < modules; row++) {
            for (int col = 0; col < modules; col++) {
                if (matrix.get(col, row)) {
                    float px = x + col * modSize;
                    float py = y + (modules - 1 - row) * modSize;
                    cs.addRect(px, py, modSize, modSize);
                }
            }
        }
        cs.fill();
    }

    private void drawText(PDPageContentStream cs, String text, PDFont font,
                          float size, float x, float y) throws IOException {
        cs.beginText();
        cs.setFont(font, size);
        cs.newLineAtOffset(x, y);
        cs.showText(text);
        cs.endText();
    }

    private List<String> wrapText(String text, PDFont font,
                                  float fontSize, float maxWidth) throws IOException {
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
