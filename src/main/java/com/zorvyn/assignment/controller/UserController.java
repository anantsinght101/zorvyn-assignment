package com.zorvyn.assignment.controller;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import com.zorvyn.assignment.dto.LoginRequestDTO;
import com.zorvyn.assignment.dto.LoginResponseDTO;
import com.zorvyn.assignment.dto.SignUpRequestDTO;
import com.zorvyn.assignment.dto.SignUpResponseDTO;
import com.zorvyn.assignment.entity.User;
import com.zorvyn.assignment.service.UserService;
import com.zorvyn.assignment.entity.Role;

@RestController
public class UserController {

    private UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }   

    @PostMapping("/users")
    public User create(@RequestBody User user) {
        return userService.createUser(user);
        
    }

    @PostMapping("/users/update")
    public User update(@RequestBody User user) {
        return userService.updateUser(user.getId(), user);
        
    }

    @PostMapping("/users/delete")
    public boolean delete(@RequestBody User user) {
        return userService.deleteUser(user.getId());
        
    }

    @PostMapping("/users/get")
    public User getById(@RequestBody User user) {
        return userService.getUserById(user.getId());
        
    }

@PostMapping("/users/getAll")
    public Iterable<User> getAll() {
        return userService.getAllUsers();
        
    }   





   @PostMapping("/users/signup")
public SignUpResponseDTO signup(@RequestBody SignUpRequestDTO signUpRequest) {
    User user = new User();
    user.setName(signUpRequest.getName());
    user.setEmail(signUpRequest.getEmail());
    user.setPassword(signUpRequest.getPassword());

    Role role = new Role();
    role.setName(signUpRequest.getRole());
    user.setRole(role);

    User savedUser = userService.signup(user);

    SignUpResponseDTO response = new SignUpResponseDTO();
    response.setName(savedUser.getName());
    response.setEmail(savedUser.getEmail());
    response.setRole(savedUser.getRole().getName());

    return response;
}

  @PostMapping("/users/login")
public LoginResponseDTO login(@RequestBody LoginRequestDTO loginRequest) {
    String token = userService.login(loginRequest.getEmail(), loginRequest.getPassword());

    LoginResponseDTO response = new LoginResponseDTO();
    response.setMessage("Login successful");
    response.setToken(token);

    return response;
}

}
