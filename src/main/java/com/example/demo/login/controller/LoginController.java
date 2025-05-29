package com.example.demo.login.controller;

import com.example.demo.login.dto.KakaoLogoutRes;
import com.example.demo.login.dto.KakaoUserInfo;
import com.example.demo.login.service.KakaoLoginService;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.net.URI;
import java.util.HashMap;
import java.util.Map;

/**
 * 로그인 관련 컨트롤러.
 *
 * <p>카카오 로그인, 로그아웃, 액세스 토큰 갱신 API 엔드포인트를 제공한다.
 */
@RestController
@Slf4j
@RequiredArgsConstructor
public class LoginController {

    private final KakaoLoginService kakaoLoginService;

    @Value("${kakao.client-id}")
    private String clientId;
    @Value("${kakao.redirect-uri}")
    private String redirectUri;
    @GetMapping("/authorize")
    public void redirectToKakaoAuth(HttpServletResponse response) throws IOException {

        String kakaoAuthUrl = "https://kauth.kakao.com/oauth/authorize"
                + "?client_id=" + clientId
                + "&redirect_uri=" + redirectUri
                + "&response_type=code";

        response.sendRedirect(kakaoAuthUrl);
    }
    /**
     * 카카오 로그인 요청 처리.
     *
     * @param body            JSON으로 전달된 인가 코드 ({@code {"code":"..."}})
     * @param servletResponse 리프레시 토큰을 쿠키에 담기 위한 응답 객체
     * @return 이메일, 닉네임과 함께 Authorization 헤더에 액세스 토큰이 설정된 응답
     */
    @PostMapping("/kakao")
    public ResponseEntity<Map<String,Object>> loginWithKakao(
            @RequestBody Map<String,String> body,
            HttpServletResponse servletResponse
    ) {
        String code = body.get("code");
        log.debug(code);
        // 프론트로부터 인가 코드 받아서 토큰 받아온 후 유저 정보까지 받아오는 로직 여기서 실행함
        KakaoUserInfo info = kakaoLoginService.handleKakaoLogin(code);

        // 받아온 refreshtoken을 쿠키로 보냄
        Cookie cookie = new Cookie("refreshToken", info.getRefreshToken());
        cookie.setHttpOnly(true);
        cookie.setSecure(true);
        cookie.setPath("/");
        cookie.setMaxAge((int) info.getRefreshTokenExpiresIn());
        servletResponse.addCookie(cookie);
        //받아온 accessToken은 authorization 헤더에 집어넣음
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(info.getAccessToken());
        // 나머지 정보는 json 형식으로 파싱함
        Map<String,Object> result = new HashMap<>();
        result.put("email",       info.getEmail());
        result.put("nickname",    info.getNickname());
        return ResponseEntity
                .ok()
                .headers(headers)
                .body(result);
    }

    /**
     * 카카오 로그아웃 요청 처리.
     *
     * @param authorization Authorization 헤더에 담긴 액세스 토큰
     * @param response      로그아웃 시 쿠키 삭제 등을 위한 응답 객체
     * @return 로그아웃 결과를 담은 {@link KakaoLogoutRes} DTO
     */
    @PostMapping("/logout")
    public ResponseEntity<?> kakaoLogout(@RequestHeader("Authorization") String authorization, HttpServletResponse response) {
        // 로그아웃 요청 함
        KakaoLogoutRes result =  kakaoLoginService.handleKakaoLogout(authorization, response);
        // 응답은 KakaoLogoutRes dto 참고
        return ResponseEntity.ok(result);
    }

    @PostMapping("/refresh")
    public ResponseEntity<?> refreshAccessToken(HttpServletRequest request, HttpServletResponse response) {
        return kakaoLoginService.refreshToken(request, response);
    }
}
