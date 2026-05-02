package com.medj.service.impl;

import com.google.cloud.vision.v1.*;
import com.google.protobuf.ByteString;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import com.google.auth.oauth2.ServiceAccountCredentials;
import com.google.api.gax.core.FixedCredentialsProvider;
import java.io.FileInputStream;

@Slf4j
@Service
public class OcrService {

    public String extractText(Path filePath) {
        try {
            ImageAnnotatorSettings settings = ImageAnnotatorSettings.newBuilder()
                    .setCredentialsProvider(
                            FixedCredentialsProvider.create(
                                    ServiceAccountCredentials.fromStream(
                                            new FileInputStream("D:/DOWNLOAD/medj-ocr-490912-3a416ab66189.json")
                                    )
                            )
                    )
                    .build();

            try (ImageAnnotatorClient client = ImageAnnotatorClient.create(settings)) {
                byte[] fileBytes = Files.readAllBytes(filePath);
                ByteString imgBytes = ByteString.copyFrom(fileBytes);

                Image img = Image.newBuilder().setContent(imgBytes).build();

                Feature feat = Feature.newBuilder()
                        .setType(Feature.Type.DOCUMENT_TEXT_DETECTION)
                        .build();

                AnnotateImageRequest request = AnnotateImageRequest.newBuilder()
                        .addFeatures(feat)
                        .setImage(img)
                        .build();

                BatchAnnotateImagesResponse response = client.batchAnnotateImages(List.of(request));
                AnnotateImageResponse res = response.getResponsesList().get(0);

                if (res.hasError()) {
                    log.warn("OCR error: {}", res.getError().getMessage());
                    return null;
                }

                return res.getFullTextAnnotation().getText();
            }

        } catch (IOException e) {
            log.error("OCR failed: {}", e.getMessage());
            return null;
        }
    }
}
