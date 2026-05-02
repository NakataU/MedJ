package com.medj.repositories;

import com.medj.entities.Specialty;
import org.springframework.context.annotation.Bean;
import org.springframework.data.domain.Page;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.data.domain.Pageable;

@Repository
public interface ISpecialtyRepository extends JpaRepository<Specialty, Long> {
    @Query("SELECT s FROM Specialty s WHERE s.isActive = true")
    Page<Specialty> findAllSpecialtiesByIsActive(Pageable pageable);

    @Query("SELECT s FROM Specialty s WHERE s.id = :id AND s.isActive = true")
    Specialty findSpecialtiesByIdAndByIsActive(@Param("id") Long id);

    boolean existsById(Long id);


}
