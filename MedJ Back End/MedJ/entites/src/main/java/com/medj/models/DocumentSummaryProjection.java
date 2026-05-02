package com.medj.models;

import java.time.LocalDateTime;

public interface DocumentSummaryProjection {
    String getContent();
    String getFileName();
    LocalDateTime getCreatedAt();
}