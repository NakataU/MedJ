package com.medj.service.impl;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.EncodeHintType;
import com.google.zxing.WriterException;
import com.google.zxing.qrcode.decoder.ErrorCorrectionLevel;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;
import com.medj.entities.ApplicationUser;
import com.medj.repositories.IApplicationUserRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Map;

@Service
@Slf4j
public class QrCodeService {

    @Autowired
    private IApplicationUserRepository userRepository;

    @Autowired
    private DocumentService documentService;

    public byte[] generateForCurrentUser(String prompt) throws IOException, WriterException {
        log.info("generateForCurrentUser started");

        ApplicationUser user = userRepository.findById(1L)
                .orElseThrow(() -> new EntityNotFoundException("User not found"));

        String summary = documentService.generateSummary(prompt);

        String header = "Patient: " + user.getUsername() + "\n\n";
        String content = header + truncate(summary, 250 - header.length());

        return generatePng(content, 500, 500);
    }

    public byte[] generateFromContent(String content) throws WriterException, IOException {
        return generatePng(content, 500, 500);
    }

    public BitMatrix getBitMatrix(String content) throws WriterException {
        QRCodeWriter writer = new QRCodeWriter();
        // Pass 1x1 so ZXing returns the raw module grid (no upscaling).
        // Each pixel in the matrix = one logical QR module.
        return writer.encode(
                content,
                BarcodeFormat.QR_CODE,
                1, 1,
                Map.of(
                        EncodeHintType.CHARACTER_SET, "UTF-8",
                        EncodeHintType.ERROR_CORRECTION, ErrorCorrectionLevel.L,
                        EncodeHintType.MARGIN, 1
                )
        );
    }

    private String truncate(String text, int maxBytes) {
        byte[] bytes = text.getBytes(java.nio.charset.StandardCharsets.UTF_8);
        if (bytes.length <= maxBytes) return text;
        return new String(bytes, 0, maxBytes, java.nio.charset.StandardCharsets.UTF_8) + "...";
    }

    private byte[] generatePng(String content, int width, int height) throws WriterException, IOException {
        QRCodeWriter writer = new QRCodeWriter();
        BitMatrix matrix = writer.encode(
                content,
                BarcodeFormat.QR_CODE,
                width,
                height,
                Map.of(
                        EncodeHintType.CHARACTER_SET, "UTF-8",
                        EncodeHintType.ERROR_CORRECTION, ErrorCorrectionLevel.L,
                        EncodeHintType.MARGIN, 1
                )
        );

        ByteArrayOutputStream out = new ByteArrayOutputStream();
        MatrixToImageWriter.writeToStream(matrix, "PNG", out);
        return out.toByteArray();
    }
}
