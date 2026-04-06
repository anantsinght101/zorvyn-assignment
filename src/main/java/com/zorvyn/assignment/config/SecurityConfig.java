package com.zorvyn.assignment.config;

import java.io.PrintWriter;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import com.zorvyn.assignment.security.JwtFilter;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class SecurityConfig {

    private final JwtFilter jwtFilter;

    public SecurityConfig(JwtFilter jwtFilter) {
        this.jwtFilter = jwtFilter;
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .csrf(csrf -> csrf.disable())
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/auth/signup", "/auth/login",
                                "/signup.html", "/login.html", "/",
                                "/dashboard.html", "/css/**", "/js/**",
                                "/favicon.ico" ,"/style.css", "/script.js")
                        .permitAll()
                        .requestMatchers("/users/**").hasRole("ADMIN")
                        .requestMatchers(org.springframework.http.HttpMethod.POST, "/api/transactions/summary").hasAnyRole("ADMIN", "ANALYST")
                        .requestMatchers(org.springframework.http.HttpMethod.GET, "/api/transactions/deleted").hasRole("ADMIN")
                        .requestMatchers(org.springframework.http.HttpMethod.GET, "/api/transactions/count").hasAnyRole("ADMIN", "ANALYST", "VIEWER")
                        .requestMatchers(org.springframework.http.HttpMethod.GET, "/api/transactions/recent").hasAnyRole("ADMIN", "ANALYST", "VIEWER")
                        .requestMatchers(org.springframework.http.HttpMethod.GET, "/api/transactions/**").hasAnyRole("ADMIN", "ANALYST", "VIEWER")
                        .requestMatchers(org.springframework.http.HttpMethod.POST, "/api/transactions").hasRole("ADMIN")
                        .requestMatchers(org.springframework.http.HttpMethod.PUT, "/api/transactions/**").hasRole("ADMIN")
                        .requestMatchers(org.springframework.http.HttpMethod.DELETE, "/api/transactions/**").hasRole("ADMIN")
                        .anyRequest().authenticated())
                .exceptionHandling(ex -> ex
                        .accessDeniedHandler((request, response, accessDeniedException) -> {
                            response.setStatus(403);
                            response.setContentType("application/json");
                            PrintWriter out = response.getWriter();
                            out.print("{\"status\":403,\"message\":\"Access denied\",\"timestamp\":\"" + java.time.LocalDateTime.now()
                                    + "\"}");
                            out.flush();
                        })
                        .authenticationEntryPoint(unauthorizedEntryPoint()))
                .addFilterBefore(jwtFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    @Bean
    AuthenticationEntryPoint unauthorizedEntryPoint() {
        return (request, response, authException) -> {
            response.setStatus(401);
            response.setContentType("application/json");
            PrintWriter out = response.getWriter();
            out.print("{\"status\":401,\"message\":\"Unauthorized\",\"timestamp\":\"" + java.time.LocalDateTime.now() + "\"}");
            out.flush();
        };
    }

    @Bean
    PasswordEncoder passwordEncoder() {
        return new org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder();
    }
}