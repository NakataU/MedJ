package com.medj.entities;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Data;

import java.time.LocalDateTime;

@Entity
@Table(name = "MedicalSummary")
@Data
public class MedicalSummary extends BaseEntity {

    private String token;
    private String username;

    @Column(columnDefinition = "TEXT")
    private String summary;

    private LocalDateTime generatedAt;
}
