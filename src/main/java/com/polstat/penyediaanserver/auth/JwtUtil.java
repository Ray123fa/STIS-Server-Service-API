package com.polstat.penyediaanserver.auth;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.SignatureException;
import io.jsonwebtoken.UnsupportedJwtException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;
import java.io.Serializable;
import java.util.Collections;
import java.util.Date;
import java.util.List;

@Component
public class JwtUtil implements Serializable {
    @Value("${jwt.secret}")
    private String SECRET_KEY;
    @Value("${jwt.expiration}")
    private long EXPIRE_DURATION;

    public String generateAccessToken(Authentication authentication) {
        UserDetails userDetails = (UserDetails) authentication.getPrincipal();
        String role = userDetails.getAuthorities().stream()
                .findFirst() // Asumsikan hanya satu peran untuk setiap pengguna
                .orElseThrow(() -> new RuntimeException("Peran tidak ditemukan"))
                .getAuthority().replace("ROLE_", ""); // Hilangkan prefix "ROLE_"

        return Jwts.builder()
                .setSubject(userDetails.getUsername())
                .claim("role", role) // Menambahkan peran ke klaim
                .setIssuer("Polstat")
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + EXPIRE_DURATION))
                .signWith(SignatureAlgorithm.HS512, SECRET_KEY)
                .compact();
    }

    public boolean validateAccessToken(String token) {
        try {
            Jwts.parser().setSigningKey(SECRET_KEY).parseClaimsJws(token);
            return true;
        } catch (ExpiredJwtException ex) {
            System.out.println("JWT expired" + ex.getMessage());
        } catch (IllegalArgumentException ex) {
            System.out.println("Token is null, empty or only whitespace" + ex.getMessage());
        } catch (MalformedJwtException ex) {
            System.out.println("JWT is invalid" + ex);
        } catch (UnsupportedJwtException ex) {
            System.out.println("JWT is not supported" + ex);
        } catch (SignatureException ex) {
            System.out.println("Signature validation failed");
        }
        return false;
    }

    public String getSubject(String token) {
        return parseClaims(token).getSubject();
    }

    private Claims parseClaims(String token) {
        return Jwts.parser().setSigningKey(SECRET_KEY).parseClaimsJws(token).getBody();
    }

    public String getRoleFromToken(String token) {
        Claims claims = parseClaims(token);
        return claims.get("role", String.class);
    }

    public List<GrantedAuthority> getAuthoritiesFromToken(String token) {
        String role = getRoleFromToken(token);
        if (role != null) {
            return Collections.singletonList(new SimpleGrantedAuthority("ROLE_" + role));
        }
        return Collections.emptyList();
    }
}
