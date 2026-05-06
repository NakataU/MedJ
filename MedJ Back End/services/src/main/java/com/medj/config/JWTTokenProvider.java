package com.medj.config;

import com.medj.entities.Role;
import io.jsonwebtoken.*;
import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.nio.file.Files;
import java.nio.file.Paths;
import java.security.Key;
import java.security.KeyFactory;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;
import java.util.Date;

@Component
public class JWTTokenProvider {

    // 15 minutes for access token, 7 days for refresh token
    private final long jwtAccessTokenExpirationMs = 15 * 60 * 1000L;
    private final long jwtRefreshTokenExpirationMs = 7 * 24 * 60 * 60 * 1000L;

    @Value("${jwt.privateKeyPath}")
    private String privateKeyPath;

    @Value("${jwt.publicKeyPath}")
    private String publicKeyPath;

    private RSAPrivateKey privateKey;
    private RSAPublicKey publicKey;

    private static final long KEY_CACHE_TTL_MS = 24 * 60 * 60 * 1000L;
    private volatile long lastCacheRefreshTime;

    private static final String ROLE_PREFIX = "ROLE_";
    private static final Logger LOGGER = LoggerFactory.getLogger(JWTTokenProvider.class);

    @PostConstruct
    public void init() {
        if (privateKey == null || publicKey == null || isCacheExpired()) {
            synchronized (this) {
                if (privateKey == null || publicKey == null || isCacheExpired()) {
                    try {
                        this.privateKey = (RSAPrivateKey) loadKey(privateKeyPath, true);
                        this.publicKey = (RSAPublicKey) loadKey(publicKeyPath, false);
                        this.lastCacheRefreshTime = System.currentTimeMillis();
                        LOGGER.info("JWT RSA keys loaded successfully.");
                    } catch (Exception e) {
                        LOGGER.error("Failed to initialize JWT keys.", e);
                        throw new JwtException("Failed to load JWT keys", e);
                    }
                }
            }
        }
    }

    private Key loadKey(String filePath, boolean isPrivateKey) {
        try {
            byte[] keyBytes = Files.readAllBytes(Paths.get(filePath));
            String keyContent = new String(keyBytes)
                    .replace("-----BEGIN " + (isPrivateKey ? "PRIVATE" : "PUBLIC") + " KEY-----", "")
                    .replace("-----END " + (isPrivateKey ? "PRIVATE" : "PUBLIC") + " KEY-----", "")
                    .replaceAll("\\s", "");
            byte[] decodedKey = Base64.getDecoder().decode(keyContent);
            KeyFactory keyFactory = KeyFactory.getInstance("RSA");
            if (isPrivateKey) {
                return keyFactory.generatePrivate(new PKCS8EncodedKeySpec(decodedKey));
            } else {
                return keyFactory.generatePublic(new X509EncodedKeySpec(decodedKey));
            }
        } catch (Exception e) {
            LOGGER.error("Failed to load key from: {}", filePath, e);
            throw new JwtException("Failed to load key", e);
        }
    }

    private boolean isCacheExpired() {
        return System.currentTimeMillis() - lastCacheRefreshTime > KEY_CACHE_TTL_MS;
    }

    public String generateAccessToken(String username, Role role) {
        return buildToken(username, role, jwtAccessTokenExpirationMs);
    }

    public String generateRefreshToken(String username, Role role) {
        return buildToken(username, role, jwtRefreshTokenExpirationMs);
    }

    private String buildToken(String username, Role role, long expirationMs) {
        Date now = new Date();
        Date expiry = new Date(now.getTime() + expirationMs);
        try {
            return Jwts.builder()
                    .setSubject(username)
                    .claim("role", role.addPrefix(role))
                    .setIssuedAt(now)
                    .setExpiration(expiry)
                    .signWith(privateKey)
                    .compact();
        } catch (Exception e) {
            LOGGER.error("Token generation failed for user: {}", username, e);
            throw new JwtException("Token generation failed", e);
        }
    }

    public long getAccessTokenExpirationSeconds() {
        return jwtAccessTokenExpirationMs / 1000;
    }

    public long getRefreshTokenExpirationSeconds() {
        return jwtRefreshTokenExpirationMs / 1000;
    }

    public boolean validateToken(String token) {
        try {
            parseToken(token);
            return true;
        } catch (Exception e) {
            LOGGER.warn("Invalid JWT token: {}", e.getMessage());
            return false;
        }
    }

    public String getUsernameFromJWT(String token) {
        return extractClaims(token).getSubject();
    }

    public Role getRoleFromJWT(String token) {
        String roleClaim = extractClaims(token).get("role", String.class);
        if (roleClaim == null) {
            throw new JwtException("Missing role claim in JWT");
        }
        String roleName = roleClaim.startsWith(ROLE_PREFIX)
                ? roleClaim.substring(ROLE_PREFIX.length())
                : roleClaim;
        try {
            return Role.valueOf(roleName);
        } catch (IllegalArgumentException e) {
            throw new JwtException("Invalid role in JWT: " + roleName);
        }
    }

    private Claims extractClaims(String token) {
        try {
            return parseToken(token);
        } catch (Exception e) {
            LOGGER.error("Failed to extract claims from JWT.", e);
            throw new JwtException("Invalid token", e);
        }
    }

    private Claims parseToken(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(publicKey)
                .build()
                .parseClaimsJws(token)
                .getBody();
    }
}
