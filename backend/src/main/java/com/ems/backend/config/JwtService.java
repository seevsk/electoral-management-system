package com.ems.backend.config;

import com.ems.backend.entity.Account;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseCookie;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.time.Duration;
import java.util.Date;

@Component
public class JwtService {

    static final String COOKIE_NAME = "ems_token";
    private static final String CLAIM_ROLE = "role";

    @Value("${jwt.secret}")
    private String jwtSecret;

    @Value("${jwt.expiration.admin}")
    private long expirationAdmin;

    @Value("${jwt.expiration.voter}")
    private long expirationVoter;

    public String generateToken(Account account) {
        long expMs = "admin".equals(account.getRole()) ? expirationAdmin : expirationVoter;
        return Jwts.builder()
                .subject(account.getDni())
                .claim(CLAIM_ROLE, "ROLE_" + account.getRole().toUpperCase())
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + expMs))
                .signWith(signingKey())
                .compact();
    }

    public String extractDni(String token) {
        return parseClaims(token).getSubject();
    }

    public String extractRole(String token) {
        return parseClaims(token).get(CLAIM_ROLE, String.class);
    }

    public boolean isTokenValid(String token) {
        try {
            parseClaims(token);
            return true;
        } catch (JwtException | IllegalArgumentException e) {
            return false;
        }
    }

    public ResponseCookie createAuthCookie(String token) {
        Date exp = parseClaims(token).getExpiration();
        long remainingMs = Math.max(exp.getTime() - System.currentTimeMillis(), 0);
        return ResponseCookie.from(COOKIE_NAME, token)
                .httpOnly(true)
                .path("/ems")
                .maxAge(Duration.ofMillis(remainingMs))
                .sameSite("Strict")
                .build();
    }

    public ResponseCookie clearAuthCookie() {
        return ResponseCookie.from(COOKIE_NAME, "")
                .httpOnly(true)
                .path("/ems")
                .maxAge(Duration.ZERO)
                .sameSite("Strict")
                .build();
    }

    private Claims parseClaims(String token) {
        return Jwts.parser()
                .verifyWith(signingKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    private SecretKey signingKey() {
        return Keys.hmacShaKeyFor(Decoders.BASE64.decode(jwtSecret));
    }
}
