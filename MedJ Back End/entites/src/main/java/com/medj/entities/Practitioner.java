package com.medj.entities;


import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Data;

@Entity
@Table(name = "Practitioner")
@Data
public class Practitioner extends BaseEntity{

    private String firstName;
    private String lastName;
    private Long specialtyId;
    private String specialization;
}
