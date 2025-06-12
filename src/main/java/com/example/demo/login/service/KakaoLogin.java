package com.example.demo.login.service;

import com.example.demo.login.dto.KakaoLogoutRes;
import com.example.demo.login.dto.KakaoUserInfo;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.ResponseEntity;

import java.sql.SQLException;

public interface KakaoLogin {
    public KakaoUserInfo handleKakaoLogin(String code) throws SQLException;
    public KakaoLogoutRes handleKakaoLogout(String authorization, HttpServletResponse response);
    public ResponseEntity<?> refreshToken(HttpServletRequest request, HttpServletResponse response);
}
