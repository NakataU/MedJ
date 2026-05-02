package com.medj.repositories;

import com.medj.entities.MedicalSummary;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface IMedicalSummaryRepository extends JpaRepository<MedicalSummary, Long> {
    Optional<MedicalSummary> findByToken(String token);
}
