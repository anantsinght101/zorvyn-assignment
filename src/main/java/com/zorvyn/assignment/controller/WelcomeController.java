package com.zorvyn.assignment.controller;

import java.io.IOException;

import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import jakarta.servlet.http.HttpServletResponse;

@RestController
public class WelcomeController {

    @GetMapping("/")
    public void root(HttpServletResponse response) throws IOException {
        response.sendRedirect("/signup.html");
    }

    @GetMapping("/dashboard")
    public String dashboard() {
        String email = SecurityContextHolder.getContext()
                .getAuthentication()
                .getName();
        return "Hello, " + email + "! You are authenticated.";
    }
}