package com.medj.repositories;

import com.medj.entities.ApplicationUser;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface IApplicationUserRepository extends JpaRepository<ApplicationUser, Long> {

    Page<ApplicationUser> findAll(Pageable pageable);

    Optional<ApplicationUser> findByUsername(String username);
}
