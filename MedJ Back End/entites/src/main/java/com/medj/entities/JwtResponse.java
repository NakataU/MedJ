package com.medj.entities;

public record JwtResponse(String accessToken, String tokenType, long expiresIn, Role role, String username, Long id) {
}
