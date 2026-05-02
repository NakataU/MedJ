//package com.medj.service.impl;
//
//import com.medj.config.JWTTokenProvider;
//import com.medj.entities.ApplicationUser;
//import com.medj.entities.JwtResponse;
//import com.medj.repositories.IApplicationUserRepository;
//import com.medj.service.IApplicationUserService;
//import com.medj.view.inView.ApplicationUserInView;
//import com.medj.view.inView.ApplicationUserLogInInView;
//import com.medj.view.outView.ApplicationUserOutView;
//import jakarta.servlet.http.Cookie;
//import jakarta.servlet.http.HttpServletRequest;
//import jakarta.servlet.http.HttpServletResponse;
//import lombok.RequiredArgsConstructor;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.data.domain.Page;
//import org.springframework.data.domain.Pageable;
//import org.springframework.security.core.context.SecurityContextHolder;
//import org.springframework.security.crypto.password.PasswordEncoder;
//import org.springframework.stereotype.Service;
//import org.springframework.web.context.request.RequestContextHolder;
//import org.springframework.web.context.request.ServletRequestAttributes;
//
//import java.util.Optional;
//
//@Service
//@RequiredArgsConstructor
//public class ApplicationServiceService implements IApplicationUserService {
//
//    @Autowired
//    private IApplicationUserRepository applicationUserRepository;
//
//    @Autowired
//    private PasswordEncoder passwordEncoder;
//
//    @Autowired
//    private JWTTokenProvider jwtTokenProvider;
//
//    @Override
//    public Optional<ApplicationUserOutView> getById(Long id) {
//        return Optional.empty();
//    }
//
//    @Override
//    public Optional<ApplicationUserOutView> getByUserName(Long id) {
//        return Optional.empty();
//    }
//
//    @Override
//    public ApplicationUserOutView addOne(ApplicationUserInView user) {
//        return null;
//    }
//
//    @Override
//    public JwtResponse login(ApplicationUserLogInInView applicationUserLogInInView) {
//        Optional<ApplicationUser> user = applicationUserRepository.findByUsername(applicationUserLogInInView.getUsername());
//
//        if (user.isPresent()) {
//            if (passwordEncoder.matches(applicationUserLogInInView.getPassword(), user.get().getPassword())) {
//                String accessToken = jwtTokenProvider.generateAccessToken(user.get().getUsername(), user.get().getRole());
//                String refreshToken = jwtTokenProvider.generateRefreshToken(user.get().getUsername(), user.get().getRole());
//
//                HttpServletResponse httpServletResponse = getHttpServletResponse();
//
//                setRefreshTokenCookie(httpServletResponse, refreshToken);
//
//                return new JwtResponse(accessToken, "Bearer", jwtTokenProvider.getAccessTokenExpiration(), user.get().getRole());
//            }
//        }
//        return null;
//    }
//
//    @Override
//    public void logout() {
//        //log.info("Logging out user...");
//
//        HttpServletRequest request = getHttpServletRequest();
//        HttpServletResponse response = getHttpServletResponse();
//
//        SecurityContextHolder.clearContext();
//        if (request.getSession(false) != null) {
//            request.getSession().invalidate();
//        }
//
//        // Remove refresh token cookie
//        setRefreshTokenCookie(response, null);
//    }
//
//    private void setRefreshTokenCookie(HttpServletResponse response, String refreshToken) {
//        Cookie cookie = new Cookie("refreshToken", refreshToken != null ? refreshToken : "");
//        cookie.setHttpOnly(true);
//        //cookie.setSecure(isProduction);
//        cookie.setPath("/"); // Cookie available for entire domain
//        cookie.setMaxAge(refreshToken != null ? (int) jwtTokenProvider.getRefreshTokenExpiration() : 0);
//
//        // SameSite policy
//        String sameSite = "None"; //isAPIOnSameDomain ? "Strict" : "None";
//        response.setHeader("Set-Cookie", String.format(
//                "refreshToken=%s; HttpOnly; Path=/; Max-Age=%d; %s; SameSite=%s",
//                refreshToken != null ? refreshToken : "",
//                cookie.getMaxAge(),
//                "",//isProduction ? "Secure" : "",
//                sameSite
//        ));
//    }
//
//    private HttpServletResponse getHttpServletResponse() {
//        return ((ServletRequestAttributes) RequestContextHolder.currentRequestAttributes()).getResponse();
//    }
//
//    private HttpServletRequest getHttpServletRequest() {
//        return ((ServletRequestAttributes) RequestContextHolder.currentRequestAttributes()).getRequest();
//    }
//
//    @Override
//    public Page<ApplicationUserOutView> getAllByUserId(Long id, Pageable pageable) {
//        return null;
//    }
//}
