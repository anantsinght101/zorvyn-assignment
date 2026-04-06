package com.zorvyn.assignment.service;

import com.zorvyn.assignment.dto.CreateUserRequestDTO;
import com.zorvyn.assignment.dto.CreateUserResponseDTO;
import com.zorvyn.assignment.entity.Role;
import com.zorvyn.assignment.repository.RoleRepository;
import org.springframework.stereotype.Service;
import com.zorvyn.assignment.entity.User;
import com.zorvyn.assignment.exception.AccountInactiveException;
import com.zorvyn.assignment.exception.DuplicateResourceException;
import com.zorvyn.assignment.exception.InvalidCredentialsException;
import com.zorvyn.assignment.exception.ResourceNotFoundException;
import com.zorvyn.assignment.repository.UserRepository;

import java.util.List;

import org.springframework.security.crypto.password.PasswordEncoder;
import com.zorvyn.assignment.security.JwtUtil;

@Service
public class UserService {

    private final UserRepository userRepository;
    private final JwtUtil jwtUtil;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;

    public UserService(UserRepository userRepository, JwtUtil jwtUtil, RoleRepository roleRepository,
            PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.jwtUtil = jwtUtil;
        this.roleRepository = roleRepository;
        this.passwordEncoder = passwordEncoder;
    }

   public CreateUserResponseDTO createUser(CreateUserRequestDTO request) {

    if (userRepository.findByEmail(request.getEmail()).isPresent()) {
        throw new DuplicateResourceException("Email already exists");
    }

    Role role = roleRepository.findByName(request.getRole())
            .orElseThrow(() -> new ResourceNotFoundException("Role not found"));

    User user = new User();
    user.setName(request.getName());
    user.setEmail(request.getEmail());
    user.setRole(role);

    String plainPassword = (request.getPassword() == null || request.getPassword().isEmpty())
            ? "defaultPassword1"
            : request.getPassword();

    user.setPassword(passwordEncoder.encode(plainPassword));
    user.setActive(true);

    User savedUser = userRepository.save(user);

    CreateUserResponseDTO response = new CreateUserResponseDTO();
    response.setName(savedUser.getName());
    response.setEmail(savedUser.getEmail());
    response.setRole(savedUser.getRole().getName());
    response.setTemporaryPassword(plainPassword);

    return response;
}

    public User getUserById(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "User not found with id: " + id));
    }

    public User getUserByEmail(String email) {
        return userRepository.findByEmail(email).orElse(null);
    }

    public List<User> getAllUsers() {
        return userRepository.findAll();
    }

    public User updateUser(Long id, User updatedUser) {
        User existing = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "User not found with id: " + id));

        existing.setName(updatedUser.getName());
        existing.setEmail(updatedUser.getEmail());
        
        if (updatedUser.getPassword() != null && !updatedUser.getPassword().isEmpty() && !updatedUser.getPassword().equals("defaultPassword1")) {
            existing.setPassword(passwordEncoder.encode(updatedUser.getPassword()));
        }
        
        if (updatedUser.getRole() != null && updatedUser.getRole().getName() != null) {
            Role role = roleRepository.findByName(updatedUser.getRole().getName())
                    .orElseThrow(() -> new ResourceNotFoundException("Role not found"));
            existing.setRole(role);
        }
        existing.setActive(updatedUser.isActive());
        
        return userRepository.save(existing);
    }

    public void deleteUser(Long id) {
        if (userRepository.existsById(id)) {
            userRepository.deleteById(id);
        }
    
    }

   public User signup(User user, String roleName) {
        if (userRepository.findByEmail(user.getEmail()).isPresent()) {
            throw new DuplicateResourceException("Email already exists");
        }

       
        Role role = roleRepository.findByName(roleName)
                .orElseThrow(() -> new ResourceNotFoundException("Role not found"));

        user.setRole(role);
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        user.setActive(true);
        return userRepository.save(user);
    }

    public String login(String email, String password) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        if (!user.isActive()) {
            throw new AccountInactiveException("User account is inactive");
        }

        if (!passwordEncoder.matches(password, user.getPassword())) {
            throw new InvalidCredentialsException("Invalid credentials");
        }

        return jwtUtil.generateToken(user);

    }

    public void setUserActiveStatus(Long userId, boolean isActive) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        user.setActive(isActive);
        userRepository.save(user);
    }

}
