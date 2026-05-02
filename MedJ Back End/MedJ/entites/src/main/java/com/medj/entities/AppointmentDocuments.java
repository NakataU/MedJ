package com.medj.entities;

import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Data;

@Entity
@Table(name = "Appointment Documents")
@Data
public class AppointmentDocuments extends BaseEntity{
   private Long appointmentId;
   private Long documentId;
}
