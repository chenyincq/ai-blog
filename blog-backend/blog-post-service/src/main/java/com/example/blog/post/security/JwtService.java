package com.example.blog.post.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;

import java.nio.charset.StandardCharsets;
import java.security.Key;
import java.util.Arrays;

public class JwtService {

    private static final String SECRET = "blog-jwt-secret-key-at-least-32-bytes-long-for-hs256";

    private static Key key() {
        byte[] bytes = SECRET.getBytes(StandardCharsets.UTF_8);
        return Keys.hmacShaKeyFor(bytes.length >= 32 ? bytes : Arrays.copyOf(bytes, 32));
    }

    public static Claims parseToken(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(key())
                .build()
                .parseClaimsJws(token)
                .getBody();
    }
}
