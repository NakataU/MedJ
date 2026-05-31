package com.medj.service.impl;

import com.medj.config.JWTTokenProvider;
import com.medj.entities.ApplicationUser;
import com.medj.entities.JwtResponse;
import com.medj.entities.Role;
import com.medj.entities.UserNameBlackList;
import com.medj.repositories.IApplicationUserRepository;
import com.medj.service.IApplicationUserService;
import com.medj.view.inView.ApplicationUserInView;
import com.medj.view.inView.ApplicationUserLogInInView;
import com.medj.view.outView.ApplicationUserOutView;
import io.jsonwebtoken.JwtException;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class ApplicationUserService implements IApplicationUserService {

    private final IApplicationUserRepository applicationUserRepository;
    private final PasswordEncoder passwordEncoder;
    private final JWTTokenProvider jwtTokenProvider;
    private final UserNameBlackList userNameBlackList;

    @Override
    public JwtResponse login(ApplicationUserLogInInView loginView, HttpServletResponse response) {
        ApplicationUser user = applicationUserRepository
                .findByUsername(loginView.getUsername())
                .orElseThrow(() -> new BadCredentialsException("Invalid username or password"));

        if (!passwordEncoder.matches(loginView.getPassword(), user.getPassword())) {
            throw new BadCredentialsException("Invalid username or password");
        }

        Long userId = applicationUserRepository.findIdByUsername(user.getUsername()).orElse(null);

        String accessToken = jwtTokenProvider.generateAccessToken(user.getUsername(), user.getRole());
        String refreshToken = jwtTokenProvider.generateRefreshToken(user.getUsername(), user.getRole());

        writeRefreshTokenCookie(response, refreshToken);

        return new JwtResponse(
                accessToken,
                "Bearer",
                jwtTokenProvider.getAccessTokenExpirationSeconds(),
                user.getRole(),
                user.getUsername(),
                userId
        );
    }

    @Override
    public JwtResponse refresh(String refreshToken, HttpServletResponse response) {
        if (!jwtTokenProvider.validateToken(refreshToken)) {
            throw new JwtException("Refresh token is invalid or expired");
        }

        String username = jwtTokenProvider.getUsernameFromJWT(refreshToken);

        if (userNameBlackList.isBlacklisted(username)) {
            throw new JwtException("Token has been revoked");
        }

        ApplicationUser user = applicationUserRepository
                .findByUsername(username)
                .orElseThrow(() -> new BadCredentialsException("User not found"));

        Long userId = applicationUserRepository.findIdByUsername(user.getUsername()).orElse(null);

        String newAccessToken = jwtTokenProvider.generateAccessToken(user.getUsername(), user.getRole());
        String newRefreshToken = jwtTokenProvider.generateRefreshToken(user.getUsername(), user.getRole());

        writeRefreshTokenCookie(response, newRefreshToken);

        return new JwtResponse(
                newAccessToken,
                "Bearer",
                jwtTokenProvider.getAccessTokenExpirationSeconds(),
                user.getRole(),
                user.getUsername(),
                userId
        );
    }

    @Override
    public void logout(HttpServletResponse response) {
        // Expire the cookie immediately
        writeRefreshTokenCookie(response, null);
    }

    // Writes the refresh token into an HttpOnly cookie.
    // Pass null to clear the cookie (on logout).
    private void writeRefreshTokenCookie(HttpServletResponse response, String refreshToken) {
        boolean clearing = refreshToken == null;
        int maxAge = clearing ? 0 : (int) jwtTokenProvider.getRefreshTokenExpirationSeconds();
        String value = clearing ? "" : refreshToken;

        // We build the Set-Cookie header manually to include SameSite
        String cookie = String.format(
                "refreshToken=%s; HttpOnly; Path=/user/refresh; Max-Age=%d; SameSite=Strict",
                value, maxAge
        );
        response.setHeader("Set-Cookie", cookie);
    }

    @Override
    public Optional<ApplicationUserOutView> getById(Long id) {
        return applicationUserRepository.findById(id)
                .map(user -> {
                    ApplicationUserOutView view = new ApplicationUserOutView();
                    view.setUsername(user.getUsername());
                    view.setRole(user.getRole());
                    view.setFirstName(user.getFirstName());
                    view.setLastName(user.getLastName());
                    view.setPhone(user.getPhone());
                    view.setAddress(user.getAddress());
                    return view;
                });
    }

    @Override
    public Optional<ApplicationUserOutView> getByUserName(Long id) {
        return Optional.empty();
    }

    @Override
    public ApplicationUserOutView addOne(ApplicationUserInView userInView) {
        if (applicationUserRepository.findByUsername(userInView.getUsername()).isPresent()) {
            throw new IllegalArgumentException("Username already in use");
        }

        ApplicationUser user = new ApplicationUser();
        user.setUsername(userInView.getUsername());
        user.setPassword(passwordEncoder.encode(userInView.getPassword()));
        user.setRole(Role.REGULAR);
        user.setIsActive(true);

        ApplicationUser saved = applicationUserRepository.save(user);

        ApplicationUserOutView outView = new ApplicationUserOutView();
        outView.setUsername(saved.getUsername());
        outView.setRole(saved.getRole());
        return outView;
    }

    @Override
    public Page<ApplicationUserOutView> getAllByUserId(Long id, Pageable pageable) {
        return null;
    }

    @Override
    public ApplicationUserOutView updateProfile(Long id, String firstName, String lastName, String phone, String address) {
        ApplicationUser user = applicationUserRepository.findById(id)
                .orElseThrow(() -> new BadCredentialsException("User not found"));
        user.setFirstName(firstName);
        user.setLastName(lastName);
        user.setPhone(phone);
        user.setAddress(address);
        ApplicationUser saved = applicationUserRepository.save(user);

        ApplicationUserOutView view = new ApplicationUserOutView();
        view.setUsername(saved.getUsername());
        view.setRole(saved.getRole());
        view.setFirstName(saved.getFirstName());
        view.setLastName(saved.getLastName());
        view.setPhone(saved.getPhone());
        view.setAddress(saved.getAddress());
        return view;
    }
}
