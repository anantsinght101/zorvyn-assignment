package com.zorvyn.assignment.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class LoginResponseDTO {
    private String email;
    private String role;
    private String token;
    private String message;
    private String name;

}
