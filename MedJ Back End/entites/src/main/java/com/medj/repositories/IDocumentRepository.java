package com.medj.repositories;

import com.medj.entities.Document;
import com.medj.models.DocumentSummaryProjection;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface IDocumentRepository extends JpaRepository<Document, Long> {

    @Query("SELECT d FROM Document d WHERE d.uploadedByUserId = :userId AND d.isActive = true")
    Page<Document> findAllDocumentsByIsActive(@Param("userId")Long userId, Pageable pageable);

    @Query("SELECT d FROM Document d WHERE d.isActive = true AND d.id = :id")
    Document findByIdAndByIsActive(@Param("id")Long id);

    @Query("SELECT d FROM Document d " +
            "JOIN AppointmentDocuments ad ON d.id = ad.documentId " +
            "WHERE d.isActive = true AND ad.appointmentId = :appointmentId")
    Page<Document> findAllDocumentsByAppointmentId(@Param("appointmentId")Long id, Pageable pageable);

    boolean existsByChecksumAndIsActive(String checkSum, Boolean isActive);

    @Query("""
        SELECT d.content AS content,
               d.fileName AS fileName,
               d.createdOn AS createdAt
        FROM Document d
        WHERE d.uploadedByUserId = :userId
        ORDER BY d.createdOn DESC
    """)
    List<DocumentSummaryProjection> findContentByUser(@Param("userId") Long userId);
}
