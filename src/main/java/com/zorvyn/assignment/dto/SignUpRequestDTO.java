package com.zorvyn.assignment.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class SignUpRequestDTO {


    private String name;
    private String email;
    private String password;
    private String role;
    
    
}
