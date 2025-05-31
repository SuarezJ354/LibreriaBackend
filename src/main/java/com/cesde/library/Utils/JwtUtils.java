package com.cesde.library.Utils;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import org.springframework.stereotype.Component;

import jakarta.servlet.http.HttpServletRequest;
import java.security.Key;
import java.util.Date;

@Component
public class JwtUtils {

    private final String SECRET_KEY = "mi_clave_secreta_super_segura_y_larga_12345678901234567890123456789012"; // Reemplaza por una más segura
    private final long EXPIRATION_TIME = 1000 * 60 * 60 * 24; // 24 horas

    private Key getSigningKey() {
        return Keys.hmacShaKeyFor(SECRET_KEY.getBytes());
    }

    // Generar token
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

    // Validar token
    public boolean isTokenValid(String token) {
        try {
            getClaims(token); // lanza excepción si es inválido
            return true;
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
}
