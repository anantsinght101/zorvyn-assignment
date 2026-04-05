package com.zorvyn.assignment.security;

import java.security.Key;
import java.util.Date;

import org.springframework.stereotype.Component;

import com.zorvyn.assignment.entity.User;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;

@Component
public class JwtUtil {

    private final String SECRET = "mysecretkeymysecretkeymysecretkey";
    private final Key key = Keys.hmacShaKeyFor(SECRET.getBytes());


        public String generateToken(User user) {
    return Jwts.builder()
            .setSubject(user.getEmail())
            .claim("role", user.getRole().getName())
            .setIssuedAt(new Date())
            .setExpiration(new Date(System.currentTimeMillis() + 1000 * 60 * 60))
            .signWith(key, SignatureAlgorithm.HS256)
            .compact();
        }

    public boolean validateToken(String token) {
        try {
            Jwts.parserBuilder()
                    .setSigningKey(key)
                    .build()
                    .parseClaimsJws(token);
            return true;
        } catch (Exception e) {
            return false;
        }
    }



    public String getEmailFromToken(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(key)
                .build()
                .parseClaimsJws(token)
                .getBody()
                .getSubject();
    }
}

            