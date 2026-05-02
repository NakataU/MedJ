package com.medj.entities;

import jakarta.persistence.Column;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.MappedSuperclass;
import lombok.Data;
import org.springframework.data.annotation.CreatedBy;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedBy;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

@MappedSuperclass
@EntityListeners(AuditingEntityListener.class)
@Data
public abstract class AuditableEntity {
    @Column(name = "ISACTIVE", nullable = false)
    protected Boolean isActive;
    @CreatedBy
    @Column(name = "CREATEDBY", length = 200, nullable = false)
    protected String createdBy;
    @CreatedDate
    @Column(name = "CREATEDON", nullable = false)
    protected LocalDateTime createdOn;
    @LastModifiedBy
    @Column(name = "UPDATEDBY", length = 200, nullable = true)
    protected String updatedBy;
    @LastModifiedDate
    @Column(name = "UPDATEDON", nullable = true)
    protected LocalDateTime updateOn;
}