//package com.medj.config;
//
//import com.medj.entities.Role;
//import io.jsonwebtoken.*;
//import jakarta.annotation.PostConstruct;
//import org.slf4j.Logger;
//import org.slf4j.LoggerFactory;
//import org.springframework.beans.factory.annotation.Value;
//import org.springframework.stereotype.Component;
//
//import java.nio.file.Files;
//import java.nio.file.Paths;
//import java.security.Key;
//import java.security.KeyFactory;
//import java.security.interfaces.RSAPrivateKey;
//import java.security.interfaces.RSAPublicKey;
//import java.security.spec.PKCS8EncodedKeySpec;
//import java.security.spec.X509EncodedKeySpec;
//import java.util.Base64;
//import java.util.Date;
//
//@Component
//public class JWTTokenProvider {
//
//    private Long jwtAccessTokenExpirationInMiliSecs = 90000L;
//    private Long jwtRefreshTokenExpirationInMiliSecs = 90000L;
//
//    @Value("${jwt.privateKeyPath}")
//    private String privateKeyPath;
//
//    @Value("${jwt.publicKeyPath}")
//    private String publicKeyPath;
//
//    private RSAPrivateKey privateKey;
//    private RSAPublicKey publicKey;
//
//    private static final long CACHE_EXPIRATION_TIME = 24 * 60 * 60 * 1000;
//    private volatile long lastCacheRefreshTime;
//
//    private static final String ROLE_PREFIX = "ROLE_";
//
//    private static final Logger LOGGER = LoggerFactory.getLogger(JWTTokenProvider.class);
//
//    @PostConstruct
//    public void init() throws JwtException {
//        if (privateKey == null || publicKey == null || isCacheExpired()) {
//            synchronized (this) {
//                // Double-checking inside synchronized block to prevent race conditions
//                if (privateKey == null || publicKey == null || isCacheExpired()) {
//                    try {
//                        this.privateKey = (RSAPrivateKey) loadKey(privateKeyPath, true);
//                        this.publicKey = (RSAPublicKey) loadKey(publicKeyPath, false);
//                        this.lastCacheRefreshTime = System.currentTimeMillis(); // Update the last refresh time
//                        LOGGER.info("Keys loaded and cached successfully.");
//                    } catch (Exception e) {
//                        LOGGER.error("Failed to initialize keys. Exception: ", e);
//                        throw new JwtException("Failed to load keys", e);
//                    }
//                }
//            }
//        }
//    }
//
//
//    private Key loadKey(String filePath, boolean isPrivateKey) throws JwtException {
//        try {
//            byte[] keyBytes = Files.readAllBytes(Paths.get(filePath));
//            String keyContent = new String(keyBytes)
//                    .replace("-----BEGIN " + (isPrivateKey ? "PRIVATE" : "PUBLIC") + " KEY-----", "")
//                    .replace("-----END " + (isPrivateKey ? "PRIVATE" : "PUBLIC") + " KEY-----", "")
//                    .replaceAll("\\s", "");
//            byte[] decodedKey = Base64.getDecoder().decode(keyContent);
//
//            KeyFactory keyFactory = KeyFactory.getInstance("RSA");
//            if (isPrivateKey) {
//                return keyFactory.generatePrivate(new PKCS8EncodedKeySpec(decodedKey));
//            } else {
//                return keyFactory.generatePublic(new X509EncodedKeySpec(decodedKey));
//            }
//        } catch (Exception e) {
//            LOGGER.error("Failed to load key from path: {}", filePath, e);
//            throw new JwtException("Failed to load key", e);
//        }
//    }
//
//    private boolean isCacheExpired() {
//        return System.currentTimeMillis() - lastCacheRefreshTime > CACHE_EXPIRATION_TIME;
//    }
//
//    public String generateAccessToken(String username, Role role) throws JwtException {
//        long expirationTime = System.currentTimeMillis() + jwtAccessTokenExpirationInMiliSecs;
//        try {
//            return Jwts.builder()
//                    .setSubject(username)
//                    .claim("role", role.addPrefix(role))
//                    .setIssuedAt(new Date())
//                    .setExpiration(new Date(expirationTime))
//                    .signWith(privateKey)
//                    .compact();
//        } catch (Exception e) {
//            LOGGER.error("JWT access token generation failed for user: {} with role: {}", username, role, e);
//            throw new JwtException("JWT access token generation failed", e);
//        }
//    }
//
//    public String generateRefreshToken(String username, Role role) throws JwtException {
//        long expirationTime = System.currentTimeMillis() + jwtRefreshTokenExpirationInMiliSecs;
//
//        try {
//            return Jwts.builder()
//                    .setSubject(username)
//                    .claim("role", role.addPrefix(role))
//                    .setIssuedAt(new Date())
//                    .setExpiration(new Date(expirationTime))
//                    .signWith(privateKey) // Sign with RSA private key
//                    .compact();
//        } catch (Exception e) {
//            LOGGER.error("JWT refresh token generation failed for user: {} with role: {}", username, role, e);
//            throw new JwtException("JWT refresh token generation failed", e);
//        }
//    }
//
//    public long getAccessTokenExpiration() {
//        return jwtAccessTokenExpirationInMiliSecs / 1000;
//    }
//
//    public long getRefreshTokenExpiration() {
//        return jwtRefreshTokenExpirationInMiliSecs / 1000;
//    }
//
//    private Claims extractClaims(String token) throws JwtException {
//        try {
//            return parseToken(token);
//        } catch (Exception e) {
//            LOGGER.error("Failed to extract claims from JWT token. Token: {}", token, e);
//            throw new JwtException("Invalid token exception:", e);
//        }
//    }
//
//    private Claims parseToken(String token) throws JwtException {
//        try {
//            return Jwts.parserBuilder()
//                    .setSigningKey(publicKey)
//                    .build()
//                    .parseClaimsJws(token)
//                    .getBody();
//        } catch (ExpiredJwtException e) {
//            LOGGER.error("JWT expired. Token: {}", token, e);
//            throw new JwtException("JWT token has expired.", e);
//        } catch (UnsupportedJwtException e) {
//            LOGGER.error("Unsupported JWT. Token: {}", token, e);
//            throw new UnsupportedJwtException("Unsupported JWT token.", e);
//        } catch (MalformedJwtException e) {
//            LOGGER.error("Malformed JWT. Token: {}", token, e);
//            throw new MalformedJwtException("Malformed JWT token.", e);
//        } catch (JwtException e) {
//            LOGGER.error("JWT validation failed. Token: {}", token, e);
//            throw new JwtException("Invalid JWT token.", e);
//        }
//    }
//
//    public boolean validateToken(String token) {
//        try {
//            parseToken(token); // Parsing validates the signature and expiration
//            return true;
//        } catch (Exception e) {
//            LOGGER.warn("Invalid JWT token detected. Token: {}", token, e);
//            return false;
//        }
//    }
//
//    public String getUsernameFromJWT(String token) {
//        return extractClaims(token).getSubject();
//    }
//
//    public Role getRoleFromJWT(String token) {
//        String roleClaim = extractClaims(token).get("role", String.class);
//
//        if (roleClaim == null) {
//            throw new JwtException("Missing role claim in JWT");
//        }
//
//        String roleName = roleClaim.startsWith(ROLE_PREFIX)
//                ? roleClaim.substring(ROLE_PREFIX.length())
//                : roleClaim;
//
//        try {
//            return Role.valueOf(roleName);
//        } catch (IllegalArgumentException ex) {
//            throw new JwtException("Invalid role in JWT: " + roleName);
//        }
//    }
//}
