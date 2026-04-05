package com.zorvyn.assignment.config;

import com.zorvyn.assignment.repository.RoleRepository;

import org.springframework.stereotype.Component;

import com.zorvyn.assignment.entity.Role;

@Component
public class StartupInitializer implements org.springframework.boot.CommandLineRunner {

    private RoleRepository roleRepository;

    public StartupInitializer(RoleRepository roleRepository) {
        this.roleRepository = roleRepository;
    }

    @Override
    public void run(String... args) throws Exception {
        if (roleRepository.findByName("ADMIN").isEmpty()) {
            Role adminRole = new Role();
            adminRole.setName("ADMIN");
            adminRole.setDescription("Administrator role with full permissions.");
            roleRepository.save(adminRole);
        }

        if (roleRepository.findByName("VIEWER").isEmpty()) {
            Role viewerRole = new Role();
            viewerRole.setName("VIEWER");
            viewerRole.setDescription("Viewer role with read-only permissions.");
            roleRepository.save(viewerRole);
        }

            if (roleRepository.findByName("ANALYST").isEmpty()) {
                Role analystRole = new Role();
                analystRole.setName("ANALYST");
                analystRole.setDescription("Analyst role with permissions to analyze data.");
                roleRepository.save(analystRole);
            }
    }
    
}
