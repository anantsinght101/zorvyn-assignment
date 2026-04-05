package com.zorvyn.assignment.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import com.zorvyn.assignment.entity.Role;

public interface RoleRepository extends JpaRepository <Role, Long> {    

    java.util.Optional<Role> findByName(String name);
}
