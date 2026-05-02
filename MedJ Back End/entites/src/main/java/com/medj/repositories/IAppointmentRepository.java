package com.medj.repositories;

import com.medj.entities.Appointment;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface IAppointmentRepository extends JpaRepository<Appointment, Long> {

    @Query("SELECT a FROM Appointment a WHERE a.isActive = true")
    Page<Appointment> findAllAppointmentsByIsActive(Pageable pageable);

    @Query("SELECT a FROM Appointment  a WHERE a.id = :id AND a.isActive = true")
    Appointment findAppointmentByIdAndByIsActive(@Param("id") Long id);

}
