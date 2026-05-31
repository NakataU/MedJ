package com.medj.entities;

import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Table;
import lombok.Data;

@Entity
@Table(name = "Category")
@Data
public class Category extends BaseEntity {

    private String label;

    @Enumerated(EnumType.STRING)
    private CategoryTarget target;

    @Enumerated(EnumType.STRING)
    private CategoryType categoryType;
}
