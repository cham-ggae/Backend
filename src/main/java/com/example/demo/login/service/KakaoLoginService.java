package com.example.demo.login.service;

import com.example.demo.login.dao.UserDao;
import com.example.demo.login.dto.KakaoLogoutRes;
import com.example.demo.login.dto.KakaoUserInfo;
import com.example.demo.login.dto.User;
import com.example.demo.provider.JwtProvider;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import java.sql.SQLException;
import java.util.Map;

/**
 * 카카오 로그인, 로그아웃, 토큰 갱신 기능을 제공하는 서비스.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class KakaoLoginService implements KakaoLogin {
    /** JWT 토큰 발급 및 검증 제공자 */
    private final JwtProvider jwtProvider;
    /** 카카오 API 호출용 RestTemplate */
    private final RestTemplate restTemplate;
    private final UserDao userDao;
    /** 카카오 API 클라이언트 ID (형상관리) */
    @Value("${kakao.client-id}")
    private String clientId;
    /** 카카오 인가 코드 콜백 URI */
    @Value("${kakao.redirect-uri}")
    private String redirectUri;

    /**
     * 카카오 인가 코드를 사용해 액세스 토큰과 리프레시 토큰을 요청하고,
     * 사용자 정보를 조회하여 {@link KakaoUserInfo}에 담아 반환.
     *
     * @param code 카카오 인가 코드
     * @return 사용자 이메일, 닉네임 및 토큰 정보를 포함한 DTO
     */
    @Override
    public KakaoUserInfo handleKakaoLogin(String code) throws SQLException {
        // 1. access_token 요청
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

        // 공식문서대로 작성했으니 참고 https://developers.kakao.com/docs/latest/ko/kakaologin/rest-api#before-you-begin-process
        MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
        params.add("grant_type", "authorization_code");
        params.add("client_id", clientId);
        params.add("redirect_uri", redirectUri);
        params.add("code", code);

        HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<>(params, headers);

        ResponseEntity<Map> response = restTemplate.postForEntity(
                "https://kauth.kakao.com/oauth/token",
                request,
                Map.class
        );

        //  카카오 인증 서버로부터 토큰 응답 수신
        String accessToken = (String) response.getBody().get("access_token");
        String refreshToken = (String) response.getBody().get("refresh_token");
        Long refreshExpiresIn = ((Number) response.getBody().get("refresh_token_expires_in")).longValue();

        // 액세스 토큰으로 사용자 정보 조회
        HttpHeaders userInfoHeaders = new HttpHeaders();
        userInfoHeaders.setBearerAuth(accessToken);
        HttpEntity<?> userInfoRequest = new HttpEntity<>(userInfoHeaders);

        ResponseEntity<Map> userInfoResponse = restTemplate.exchange(
                "https://kapi.kakao.com/v2/user/me",
                HttpMethod.GET,
                userInfoRequest,
                Map.class
        );

        Map<String, Object> userInfo = userInfoResponse.getBody();
        Map<String, Object> kakaoAccount = (Map<String, Object>) userInfo.get("kakao_account");
        // 받아온 유저 정보들
        String email = (String) kakaoAccount.get("email");
        String nickname = (String) kakaoAccount.get("nickname");

        User isMember = userDao.findByEmail(email);
        if (isMember == null) {
            userDao.joinMembership(email, accessToken, refreshToken);
        }
        String newAccessToken = jwtProvider.createAccessToken(email);
        String newRefreshToken = jwtProvider.createRefreshToken(email);
        // 사용자 정보 및 토큰을 DTO로 반환
        return new KakaoUserInfo(email, nickname, newAccessToken, newRefreshToken, refreshExpiresIn);
    }

    @Override
    public KakaoLogoutRes handleKakaoLogout(String authorization, HttpServletResponse response) {
        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", authorization);

        HttpEntity<Void> request = new HttpEntity<>(headers);
        ResponseEntity<Map> kakaoResp = restTemplate.postForEntity(
                "https://kapi.kakao.com/v1/user/logout",
                request, Map.class
        );

        Cookie tokenCookie = new Cookie("refreshToken", null);
        tokenCookie.setHttpOnly(true);
        tokenCookie.setSecure(true);
        tokenCookie.setPath("/");
        tokenCookie.setMaxAge(0);
        response.addCookie(tokenCookie);

        Number idNum = (Number) kakaoResp.getBody().get("id");
        Long id = idNum == null ? null : idNum.longValue();
        return new KakaoLogoutRes(id, "카카오 로그아웃 완료");
    }

    @Override
    public ResponseEntity<?> refreshToken(HttpServletRequest request, HttpServletResponse response) {
        String refreshToken = null;
        if (request.getCookies() != null) {
            for (Cookie cookie : request.getCookies()) {
                if ("refreshToken".equals(cookie.getName())) {
                    refreshToken = cookie.getValue();
                    break;
                }
            }
        }

        if (refreshToken == null || !jwtProvider.validateToken(refreshToken)) {
            return ResponseEntity.status(401).body("Refresh Token이 유효하지 않음");
        }

        String email = jwtProvider.getEmail(refreshToken);
        String newAccessToken = jwtProvider.createAccessToken(email);
        String newRefreshToken = jwtProvider.createRefreshToken(email);

        Cookie accessCookie = new Cookie("refreshToken", newRefreshToken);
        accessCookie.setHttpOnly(true);
        accessCookie.setSecure(true);
        accessCookie.setPath("/");
        accessCookie.setMaxAge(60 * 30); // 30분
        response.addCookie(accessCookie);

        return ResponseEntity.ok("Access Token 재발급 완료");
    }
}
