package com.example.blog.auth.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;

import java.security.Key;
import java.time.Instant;
import java.util.Date;
import java.util.Map;

public class JwtService {

    private static final String SECRET = "blog-jwt-secret-key-at-least-32-bytes-long-for-hs256";
    private static final long EXPIRE_SECONDS = 7 * 24 * 60 * 60;

    private static Key key() {
        byte[] bytes = SECRET.getBytes(java.nio.charset.StandardCharsets.UTF_8);
        return Keys.hmacShaKeyFor(bytes.length >= 32 ? bytes : java.util.Arrays.copyOf(bytes, 32));
    }

    public static String generateToken(String subject, String role, Long userId) {
        Instant now = Instant.now();
        return Jwts.builder()
                .setSubject(subject)
                .claim("role", role)
                .claim("userId", userId)
                .setIssuedAt(Date.from(now))
                .setExpiration(Date.from(now.plusSeconds(EXPIRE_SECONDS)))
                .signWith(key(), SignatureAlgorithm.HS256)
                .compact();
    }

    public static Claims parseToken(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(key())
                .build()
                .parseClaimsJws(token)
                .getBody();
    }
}
