package com.cesde.library.Utils;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import org.springframework.stereotype.Component;

import jakarta.servlet.http.HttpServletRequest;
import java.security.Key;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

@Component
public class JwtUtils {

    private final String SECRET_KEY = "mi_clave_secreta_super_segura_y_larga_12345678901234567890123456789012"; // Reemplaza por una más segura
    private final long EXPIRATION_TIME = 1000 * 60 * 60 * 24; // 24 horas

    private Key getSigningKey() {
        return Keys.hmacShaKeyFor(SECRET_KEY.getBytes());
    }

    // Generar token con username y userId
    public String generateToken(String username, Long userId) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("userId", userId);

        return Jwts.builder()
                .setClaims(claims)
                .setSubject(username)
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + EXPIRATION_TIME))
                .signWith(getSigningKey(), SignatureAlgorithm.HS256)
                .compact();
    }

    // Método alternativo para mantener compatibilidad (solo username)
    public String generateToken(String username) {
        return Jwts.builder()
                .setSubject(username)
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + EXPIRATION_TIME))
                .signWith(getSigningKey(), SignatureAlgorithm.HS256)
                .compact();
    }

    // Extraer username (correo) del token
    public String extractUsername(String token) {
        return getClaims(token).getSubject();
    }

    // Extraer userId del token
    public Long extractUserId(String token) {
        Claims claims = getClaims(token);
        Object userIdObj = claims.get("userId");

        if (userIdObj == null) {
            throw new RuntimeException("Token no contiene userId");
        }

        // Manejar diferentes tipos de número que puede devolver JWT
        if (userIdObj instanceof Integer) {
            return ((Integer) userIdObj).longValue();
        } else if (userIdObj instanceof Long) {
            return (Long) userIdObj;
        } else {
            throw new RuntimeException("userId en token tiene formato inválido");
        }
    }

    // Validar token
    public boolean isTokenValid(String token) {
        try {
            getClaims(token); // lanza excepción si es inválido
            return true;
        } catch (JwtException | IllegalArgumentException e) {
            return false;
        }
    }

    // Validar token y verificar que tenga userId
    public boolean isTokenValidWithUserId(String token) {
        try {
            Claims claims = getClaims(token);
            return claims.get("userId") != null;
        } catch (JwtException | IllegalArgumentException e) {
            return false;
        }
    }

    // Extraer claims
    private Claims getClaims(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(getSigningKey())
                .build()
                .parseClaimsJws(token)
                .getBody();
    }

    // Extraer token del request
    public String extractTokenFromRequest(HttpServletRequest request) {
        String authHeader = request.getHeader("Authorization");
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            return authHeader.substring(7);
        }
        throw new RuntimeException("Token JWT no proporcionado");
    }

    // Extraer userId directamente del request
    public Long extractUserIdFromRequest(HttpServletRequest request) {
        String token = extractTokenFromRequest(request);
        return extractUserId(token);
    }

    // Extraer username directamente del request
    public String extractUsernameFromRequest(HttpServletRequest request) {
        String token = extractTokenFromRequest(request);
        return extractUsername(token);
    }
}