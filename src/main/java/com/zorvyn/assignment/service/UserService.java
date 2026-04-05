package com.zorvyn.assignment.service;

import com.zorvyn.assignment.entity.Role;
import com.zorvyn.assignment.repository.RoleRepository;
import org.springframework.stereotype.Service; 
import com.zorvyn.assignment.entity.User;
import com.zorvyn.assignment.repository.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import com.zorvyn.assignment.security.JwtUtil;

@Service
public class UserService {

    private UserRepository userRepository;
    private JwtUtil jwtUtil;
    private RoleRepository roleRepository;
    private PasswordEncoder passwordEncoder;

    public UserService(UserRepository userRepository, JwtUtil jwtUtil, RoleRepository roleRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.jwtUtil = jwtUtil;
        this.roleRepository = roleRepository;
        this.passwordEncoder = passwordEncoder;
    }




    public User createUser(User user) {
        return userRepository.save(user);
        
    }   

    public User getUserById(Long id) {
        return userRepository.findById(id).orElse(null);
    }

    public User getUserByEmail(String email) {
        return userRepository.findByEmail(email).orElse(null);
    }

    public Iterable<User> getAllUsers() {
        return userRepository.findAll();
    }

    public User updateUser(Long id, User updatedUser) {
        User existingUser = userRepository.findById(id).orElse(null);
        if (existingUser != null) {
            existingUser.setName(updatedUser.getName());
            existingUser.setEmail(updatedUser.getEmail());
            existingUser.setPassword(updatedUser.getPassword());
            existingUser.setRole(updatedUser.getRole());
            return userRepository.save(existingUser);
        }
        return null;
    }

     public boolean deleteUser(Long id) {
        if (userRepository.existsById(id)) {
            userRepository.deleteById(id);
            return true;
        }
        return false;
    }

    




    


        public User signup(User user) {
    if (userRepository.findByEmail(user.getEmail()).isPresent()) {
        throw new RuntimeException("Email already exists");
    }

    String roleName = user.getRole().getName();
    Role role = roleRepository.findByName(roleName)
            .orElseThrow(() -> new RuntimeException("Role not found"));

    user.setRole(role);
    user.setPassword(passwordEncoder.encode(user.getPassword()));
    return userRepository.save(user);
}
    


   public String login(String email, String password) {
    User user = userRepository.findByEmail(email)
            .orElseThrow(() -> new RuntimeException("User not found"));

    if (!passwordEncoder.matches(password, user.getPassword())) {
        throw new RuntimeException("Invalid credentials");
    }

    return jwtUtil.generateToken(user);
}

}
