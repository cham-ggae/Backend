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

    private static final long REFRESH_VALIDITY = 1000L * 60 * 60 * 24 * 14;
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
        Map<String, Object> profileMap = (Map<String, Object>) kakaoAccount.get("profile");

        String nickname = null;
        String profileImageUrl = null;
        String thumbnailUrl = null;

        if (profileMap != null) {
            nickname       = (String) profileMap.get("nickname");
            profileImageUrl = (String) profileMap.get("profile_image_url");
            thumbnailUrl    = (String) profileMap.get("thumbnail_image_url");
        }

// 3) optional 필드: 동의 여부 체크 후 파싱
        String gender    = null;
        String ageRange  = null;

        Boolean hasGender    = (Boolean) kakaoAccount.getOrDefault("has_gender", false);
        Boolean hasAgeRange  = (Boolean) kakaoAccount.getOrDefault("has_age_range", false);

        if (hasGender) {
            gender = (String) kakaoAccount.get("gender");
        }
        if (hasAgeRange) {
            ageRange = (String) kakaoAccount.get("age_range");
        }

        User isMember = userDao.findByEmail(email);
        if (isMember == null) {
            userDao.joinMembership(email, accessToken, refreshToken, gender, ageRange, nickname, profileImageUrl);
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
        // 1) 기존 refreshToken 쿠키에서 꺼내기
        String oldRefresh = null;
        if (request.getCookies() != null) {
            for (Cookie c : request.getCookies()) {
                if ("refreshToken".equals(c.getName())) {
                    oldRefresh = c.getValue();
                    break;
                }
            }
        }

        // 2) 없거나 유효하지 않으면 → 쿠키 삭제 후 401
        if (oldRefresh == null || !jwtProvider.validateToken(oldRefresh)) {
            // 기존 쿠키 지우기
            Cookie deleteCookie = new Cookie("refreshToken", null);
            deleteCookie.setHttpOnly(true);
            deleteCookie.setSecure(true);
            deleteCookie.setPath("/");
            deleteCookie.setMaxAge(0);
            response.addCookie(deleteCookie);

            // 재로그인 안내
            return ResponseEntity
                    .status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("error", "Refresh Token이 만료되었습니다. 다시 로그인해 주세요."));
        }

        // 3) 새 토큰 발급
        String email           = jwtProvider.getEmail(oldRefresh);
        String newAccessToken  = jwtProvider.createAccessToken(email);
        String newRefreshToken = jwtProvider.createRefreshToken(email);

        // 4) 새 refreshToken 쿠키로 교체
        Cookie refreshCookie = new Cookie("refreshToken", newRefreshToken);
        refreshCookie.setHttpOnly(true);
        refreshCookie.setSecure(true);
        refreshCookie.setPath("/");
        refreshCookie.setMaxAge((int)(REFRESH_VALIDITY / 1000)); // 14일
        response.addCookie(refreshCookie);

        // 5) 새 accessToken은 Authorization 헤더로
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(newAccessToken);

        // 6) 응답
        return ResponseEntity
                .ok()
                .headers(headers)
                .body(Map.of(
                        "message", "토큰 재발급 성공",
                        "accessToken", newAccessToken
                ));
    }
}
