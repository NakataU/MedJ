package com.medj.entities;

import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Data;

import java.time.LocalDate;

@Entity
@Table(name = "Appointment")
@Data
public class Appointment extends BaseEntity{

    private String name;
    private String place;
    private LocalDate date;
    private Long practitionerId;
    private Long userId;
}
