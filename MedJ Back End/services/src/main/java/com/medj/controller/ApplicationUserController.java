//package com.medj.controller;
//
//import com.medj.entities.JwtResponse;
//import com.medj.service.IApplicationUserService;
//import com.medj.view.inView.ApplicationUserLogInInView;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.http.ResponseEntity;
//import org.springframework.security.access.prepost.PreAuthorize;
//import org.springframework.web.bind.annotation.*;
//
//@RestController
//@RequestMapping("/user")
//public class ApplicationUserController {
//
//    @Autowired
//    private IApplicationUserService applicationUserService;
//
//    @PostMapping("/login")
//    public ResponseEntity<JwtResponse> login(@RequestBody ApplicationUserLogInInView user) {
//        return ResponseEntity.ok(applicationUserService.login(user));
//    }
//
//    @PostMapping("/logout")
//    public ResponseEntity<String> logout() {
//        applicationUserService.logout();
//        return ResponseEntity.ok("Logout Successfully");
//    }
//
//    @PreAuthorize("hasRole('ROLE_ADMIN')")
//    @GetMapping("/all")
//    public ResponseEntity<String> test(){
//        return ResponseEntity.ok("This is secured endpoint");
//    }
//}
