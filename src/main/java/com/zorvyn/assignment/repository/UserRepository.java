package com.zorvyn.assignment.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.zorvyn.assignment.entity.User;

public interface UserRepository extends JpaRepository<User, Long> {

    java.util.Optional<User> findByEmail(String email);
    
    
}
