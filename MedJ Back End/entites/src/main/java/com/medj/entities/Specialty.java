package com.medj.entities;


import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Data;

@Entity
@Table(name = "Specialty")
@Data
public class Specialty extends BaseEntity{

    private String specialty;

}
