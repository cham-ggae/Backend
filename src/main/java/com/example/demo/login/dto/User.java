package com.example.demo.login.dto;

import lombok.Data;

@Data
public class User {
    private Long id;
    private String email;
    private String access_token;
    private String refreshToken;
    private String role;
}
