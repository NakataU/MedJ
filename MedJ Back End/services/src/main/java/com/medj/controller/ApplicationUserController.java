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

import java.util.Map;

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
        System.out.println("Virtual: " + Thread.currentThread().isVirtual());
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

    @GetMapping("/profile/{id}")
    public ResponseEntity<ApplicationUserOutView> getProfile(@PathVariable("id") Long id) {
        return applicationUserService.getById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PutMapping("/profile/{id}")
    public ResponseEntity<ApplicationUserOutView> updateProfile(@PathVariable("id") Long id,
                                                                 @RequestBody Map<String, String> body) {
        ApplicationUserOutView updated = applicationUserService.updateProfile(id,
                body.get("firstName"), body.get("lastName"),
                body.get("phone"), body.get("address"));
        return ResponseEntity.ok(updated);
    }
}
