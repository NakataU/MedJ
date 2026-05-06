package com.medj.service;

import com.medj.entities.JwtResponse;
import com.medj.view.inView.ApplicationUserInView;
import com.medj.view.inView.ApplicationUserLogInInView;
import com.medj.view.outView.ApplicationUserOutView;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.Optional;

public interface IApplicationUserService {

    Optional<ApplicationUserOutView> getById(Long id);

    Optional<ApplicationUserOutView> getByUserName(Long id);

    ApplicationUserOutView addOne(ApplicationUserInView user);

    JwtResponse login(ApplicationUserLogInInView loginView, HttpServletResponse response);

    JwtResponse refresh(String refreshToken, HttpServletResponse response);

    void logout(HttpServletResponse response);

    Page<ApplicationUserOutView> getAllByUserId(Long id, Pageable pageable);
}
