//package com.medj.config;
//
//import jakarta.servlet.http.HttpServletRequest;
//import jakarta.servlet.http.HttpServletResponse;
//import lombok.RequiredArgsConstructor;
//import org.slf4j.Logger;
//import org.slf4j.LoggerFactory;
//import org.springframework.beans.factory.annotation.Value;
//import org.springframework.context.annotation.Bean;
//import org.springframework.context.annotation.Configuration;
//import org.springframework.http.HttpMethod;
//import org.springframework.security.authentication.AuthenticationManager;
//import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
//import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
//import org.springframework.security.config.annotation.web.builders.HttpSecurity;
//import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
//import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
//import org.springframework.security.config.http.SessionCreationPolicy;
//import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
//import org.springframework.security.crypto.password.PasswordEncoder;
//import org.springframework.security.web.SecurityFilterChain;
//import org.springframework.security.web.access.AccessDeniedHandler;
//import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
//import org.springframework.web.cors.CorsConfiguration;
//import org.springframework.web.cors.CorsConfigurationSource;
//import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
//
//import java.security.SecureRandom;
//import java.util.Arrays;
//import java.util.List;
//import java.util.stream.Collectors;
//
//@Configuration
//@EnableWebSecurity
//@EnableMethodSecurity(securedEnabled = true, prePostEnabled = true)
//@RequiredArgsConstructor
//public class SecurityConfig {
//
//    private final JWTAuthFilter jwtAuthFilter;
//
//    private final JWTAuthenticationEntryPoint jwtAuthenticationEntryPoint;
//
//    private static final Logger LOGGER = LoggerFactory.getLogger(JWTTokenProvider.class);
//
//    @Value("${cors.allowed.origins}")
//    private String allowedOrigins;
//
//    @Bean
//    public PasswordEncoder passwordEncoder(){
//        final int passwordEncoderStrength = 15;
//
//        SecureRandom secureRandom = new SecureRandom();
//
//        return new BCryptPasswordEncoder(passwordEncoderStrength,secureRandom);
//    }
//
//    @Bean
//    public SecurityFilterChain securityFilterChain(HttpSecurity httpSecurity){
//        String[] publicUrls = {
//                "/api/projects",
//                "/api/projects/*",
//                "/api/auth/**",
//                "/api/logs",
//                "/user/login",
//                "/user/logout"
//        };
//
//        String[] authenticationUrls = {
//                "/api/user/**",
//                "/user/all"
//        };
//
//        httpSecurity
//                .cors(cors -> cors.configurationSource(corsConfigurationSource()))
//                .csrf(AbstractHttpConfigurer::disable) // Disable CSRF for API
//                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
//                .authorizeHttpRequests(auth -> {
//                            for (String url : publicUrls) {
//                                auth.requestMatchers(url).permitAll();
//                            }
//                            for (String url : authenticationUrls) {
//                                auth.requestMatchers(url).authenticated();
//                            }
//                            auth
//                                    .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()
//                                    .anyRequest().denyAll();// Deny everything else explicitly for safety
//                        }
//                )
//                .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class)
//                .exceptionHandling(exception -> exception
//                        .authenticationEntryPoint(jwtAuthenticationEntryPoint) // 401 Unauthorized
//                        .accessDeniedHandler(accessDeniedHandler()) // 403 Forbidden
//                );
//
//        return httpSecurity.build();
//    }
//
//    @Bean
//    public AccessDeniedHandler accessDeniedHandler() {
//        return (request, response, accessDeniedException) -> {
//            logUnauthorizedAccess(request);
//            response.sendError(HttpServletResponse.SC_FORBIDDEN, "Forbidden");
//        };
//    }
//
//    private void logUnauthorizedAccess(HttpServletRequest request) {
//        LOGGER.warn("Unauthorized access attempt: {}, from {}", request.getRequestURI(), request.getRemoteAddr());
//    }
//
//    @Bean
//    public AuthenticationManager authenticationManager(AuthenticationConfiguration authConfig) throws Exception {
//        return authConfig.getAuthenticationManager();
//    }
//
//    @Bean
//    public CorsConfigurationSource corsConfigurationSource() {
//        CorsConfiguration corsConfiguration = new CorsConfiguration();
//        List<String> allowedOriginsList = Arrays.stream(allowedOrigins.split(","))
//                .map(String::trim)
//                .filter(origin -> !origin.isEmpty())
//                .collect(Collectors.toList());
//
//        corsConfiguration.setAllowedOrigins(allowedOriginsList);
//        corsConfiguration.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "OPTIONS"));
//        corsConfiguration.setAllowCredentials(true);
//        corsConfiguration.setAllowedHeaders(List.of("*"));
//        corsConfiguration.setMaxAge(3600L);
//
//        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
//        source.registerCorsConfiguration("/**", corsConfiguration);
//        return source;
//    }
//}
