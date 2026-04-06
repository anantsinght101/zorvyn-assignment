package com.zorvyn.assignment.controller;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.zorvyn.assignment.dto.LoginRequestDTO;
import com.zorvyn.assignment.dto.LoginResponseDTO;
import com.zorvyn.assignment.dto.SignUpRequestDTO;
import com.zorvyn.assignment.dto.SignUpResponseDTO;
import com.zorvyn.assignment.entity.User;
import com.zorvyn.assignment.service.UserService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/auth")
public class AuthController {

    private final UserService userService;

    public AuthController(UserService userService) {
        this.userService = userService;
    }

    @PostMapping("/login")
    public LoginResponseDTO login(@RequestBody LoginRequestDTO loginRequest) {
        String token = userService.login(loginRequest.getEmail(), loginRequest.getPassword());

        LoginResponseDTO response = new LoginResponseDTO();
        response.setMessage("Login successful");
        response.setToken(token);

        return response;
    }

    @PostMapping("/signup")
    public SignUpResponseDTO signup(@RequestBody @Valid SignUpRequestDTO signUpRequest) {
        User user = new User();
        user.setName(signUpRequest.getName());
        user.setEmail(signUpRequest.getEmail());
        user.setPassword(signUpRequest.getPassword());

        // Extract the role name, defaulting to VIEWER if the request is missing it
        String roleName = signUpRequest.getRole() != null ? signUpRequest.getRole().toUpperCase() : "VIEWER";

        // Pass both the user object and the role string to the service
        User savedUser = userService.signup(user, roleName);

        SignUpResponseDTO response = new SignUpResponseDTO();
        response.setName(savedUser.getName());
        response.setEmail(savedUser.getEmail());
        response.setRole(savedUser.getRole().getName());

        return response;
    }
}