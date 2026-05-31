package com.medj.models;

import java.time.LocalDateTime;

public interface DocumentSummaryProjection {
    Long getId();
    String getContent();
    String getFileName();
    LocalDateTime getCreatedAt();
}