package com.medj.repositories;

import com.medj.entities.Practitioner;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface IPractitionerRepository extends JpaRepository<Practitioner, Long> {

    @Query("SELECT p FROM Practitioner p WHERE p.isActive = true")
    Page<Practitioner> findAllPractitioners(Pageable pageable);

    @Query("SELECT p FROM Practitioner p WHERE p.id = :id AND p.isActive = true")
    Practitioner findPractitionerByIdAndByIsActive(@Param("id") Long id);

    boolean existsById(Long id);
}
