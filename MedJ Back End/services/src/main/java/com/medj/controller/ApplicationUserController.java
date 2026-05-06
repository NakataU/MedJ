package com.medj.controller;

import com.medj.entities.JwtResponse;
import com.medj.service.IApplicationUserService;
import com.medj.view.inView.ApplicationUserInView;
import com.medj.view.inView.ApplicationUserLogInInView;
import com.medj.view.outView.ApplicationUserOutView;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/user")
public class ApplicationUserController {

    @Autowired
    private IApplicationUserService applicationUserService;

    @PostMapping("/register")
    public ResponseEntity<ApplicationUserOutView> register(@RequestBody ApplicationUserInView user) {
        return ResponseEntity.status(HttpStatus.CREATED).body(applicationUserService.addOne(user));
    }

    @PostMapping("/login")
    public ResponseEntity<JwtResponse> login(@RequestBody ApplicationUserLogInInView user,
                                              HttpServletResponse response) {
        return ResponseEntity.ok(applicationUserService.login(user, response));
    }

    // Called by the frontend on page refresh to get a new access token.
    // The refresh token is read from the HttpOnly cookie automatically by the browser.
    @PostMapping("/refresh")
    public ResponseEntity<JwtResponse> refresh(@CookieValue(name = "refreshToken", required = false) String refreshToken,
                                                HttpServletResponse response) {
        if (refreshToken == null) {
            return ResponseEntity.status(401).build();
        }
        return ResponseEntity.ok(applicationUserService.refresh(refreshToken, response));
    }

    @PostMapping("/logout")
    public ResponseEntity<String> logout(HttpServletResponse response) {
        applicationUserService.logout(response);
        return ResponseEntity.ok("Logged out");
    }
}
