package com.zorvyn.assignment.entity;


import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;



@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "users")
public class User {

    @Id 
    @GeneratedValue
    private Long id;
    private String name;
    private String email;
    private String password;
    
    @ManyToOne
    @JoinColumn(name = "role_id", nullable = false)
    private Role role;
    
}
