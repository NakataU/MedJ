package com.medj.service;

import com.medj.entities.JwtResponse;
import com.medj.view.inView.ApplicationUserInView;
import com.medj.view.inView.ApplicationUserLogInInView;
import com.medj.view.outView.ApplicationUserOutView;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.Optional;

public interface IApplicationUserService {

    public Optional<ApplicationUserOutView> getById(Long id);

    public Optional<ApplicationUserOutView> getByUserName(Long id);

    public ApplicationUserOutView addOne(ApplicationUserInView user);

    public JwtResponse login(ApplicationUserLogInInView applicationUserLogInInView);

    public void logout();


    public Page<ApplicationUserOutView> getAllByUserId(Long id, Pageable pageable);
}
