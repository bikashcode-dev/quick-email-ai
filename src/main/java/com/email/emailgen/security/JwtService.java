package com.email.emailgen.security;

import com.email.emailgen.config.AuthProperties;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Date;

@Service
public class JwtService {

    private final SecretKey secretKey;
    private final AuthProperties authProperties;

    public JwtService(AuthProperties authProperties) {
        this.authProperties = authProperties;
        this.secretKey = buildSecretKey(authProperties.getJwt().getSecret());
    }

    public String generateToken(String email) {
        Instant now = Instant.now();
        return Jwts.builder()
                .setSubject(email)
                .setIssuedAt(Date.from(now))
                .setExpiration(Date.from(now.plusMillis(authProperties.getJwt().getExpiration())))
                .signWith(secretKey, SignatureAlgorithm.HS256)
                .compact();
    }

    public String extractEmail(String token) {
        return parseClaims(token).getSubject();
    }

    public boolean isTokenValid(String token, String email) {
        Claims claims = parseClaims(token);
        return email.equals(claims.getSubject()) && claims.getExpiration().after(new Date());
    }

    private Claims parseClaims(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(secretKey)
                .build()
                .parseClaimsJws(token)
                .getBody();
    }

    private SecretKey buildSecretKey(String rawSecret) {
        byte[] secretBytes = rawSecret.getBytes(StandardCharsets.UTF_8);
        return Keys.hmacShaKeyFor(secretBytes.length >= 32 ? secretBytes : padSecret(secretBytes));
    }

    private byte[] padSecret(byte[] original) {
        byte[] padded = new byte[32];
        for (int i = 0; i < padded.length; i++) {
            padded[i] = original[i % original.length];
        }
        return padded;
    }
}
