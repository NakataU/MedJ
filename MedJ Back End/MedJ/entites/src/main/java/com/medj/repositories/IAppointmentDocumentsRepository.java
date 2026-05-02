package com.medj.repositories;

import com.medj.entities.AppointmentDocuments;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface IAppointmentDocumentsRepository extends JpaRepository<AppointmentDocuments, Long> {

    @Query("SELECT ad FROM AppointmentDocuments ad WHERE ad.documentId = :id and ad.isActive = true")
    AppointmentDocuments findAppointmentDocumentsByDocumentIdAndIsActive(@Param("id") Long id);

    @Query("SELECT ad FROM AppointmentDocuments ad WHERE ad.appointmentId = :id and ad.isActive = true")
    List<AppointmentDocuments> findAppointmentDocumentsByAppointmentIdAndIsActive(@Param("id") Long id);
}
