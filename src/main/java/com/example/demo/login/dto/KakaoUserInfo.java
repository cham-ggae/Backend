package com.example.demo.login.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class KakaoUserInfo {
    private String email;
    private String nickname;
    private String accessToken;
    private String refreshToken;
    private long refreshTokenExpiresIn;
}
