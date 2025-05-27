package com.example.demo.login.service;

import com.example.demo.provider.JwtProvider;
import io.github.cdimascio.dotenv.Dotenv;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import java.util.Map;

@Service
@RequiredArgsConstructor
public class KakaoLoginService implements KakaoLogin {
    private final JwtProvider jwtProvider;
    private final RestTemplate restTemplate;
    private final Dotenv dotenv = Dotenv.configure().load();
    @Override
    public String handleKakaoLogin(String code) {
        String clientId = dotenv.get("KAKAO");
        String redirectUri = "http://localhost:8090/oauth2/callback/kakao";

        // 1. access_token 요청
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

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

        String accessToken = (String) response.getBody().get("access_token");

        // 2. 사용자 정보 요청
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
        String email = (String) kakaoAccount.get("email");

        // 3. JWT 발급
        return jwtProvider.createToken(email);
    }
}
