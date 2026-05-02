package com.medj.entities;

import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Data;

import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "Document")
@Data
public class Document extends BaseEntity {

    private String fileName;
    private String contentType;
    private String path;
    private String checksum;
    private Long size;
    private Long uploadedByUserId;
    private Long categoryId;
    private String content;
}
