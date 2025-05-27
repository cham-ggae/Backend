package com.example.demo.login.controller;

import com.example.demo.login.service.KakaoLoginService;
import com.example.demo.provider.JwtProvider;
import io.github.cdimascio.dotenv.Dotenv;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.*;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.LinkedHashMap;
import java.util.Map;

@RestController
@Slf4j
@RequiredArgsConstructor
public class LoginController {
    Dotenv dotenv = Dotenv.configure().load();
    String clientId = dotenv.get("KAKAO");
    private final KakaoLoginService kakaoLoginService;
    @GetMapping("/authorize")
    public void redirectToKakaoAuth(HttpServletResponse response) throws IOException {

        String redirectUri = URLEncoder.encode("http://localhost:8090/oauth2/callback/kakao", StandardCharsets.UTF_8);

        String kakaoAuthUrl = "https://kauth.kakao.com/oauth/authorize"
                + "?client_id=" + clientId
                + "&redirect_uri=" + redirectUri
                + "&response_type=code";

        response.sendRedirect(kakaoAuthUrl);
    }

    @GetMapping("/oauth2/callback/kakao")
    public void kakaoCallback(@RequestParam String code, HttpServletResponse servletResponse) throws IOException {
        log.info(code);
        // 3. JWT 발급
        String jwt = kakaoLoginService.handleKakaoLogin(code);

        Cookie cookie = new Cookie("token", jwt);
        cookie.setHttpOnly(true);
        cookie.setSecure(true); // HTTPS 환경에서만 동작
        cookie.setPath("/");
        cookie.setMaxAge(60 * 60 * 2);
        servletResponse.addCookie(cookie);

        // 4. 리디렉션
        servletResponse.sendRedirect("http://localhost:3000");
    }
}
