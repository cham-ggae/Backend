package com.example.demo.provider;

import io.github.cdimascio.dotenv.Dotenv;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import org.springframework.stereotype.Component;

import java.util.Date;

@Component
public class JwtProvider {
    static Dotenv dotenv = Dotenv.configure().load();

    private static final String secret = dotenv.get("jwtKey"); // dotenv로 빼도 됨
    // 액세스 토큰: 10분
//    private static final long ACCESS_VALIDITY  = 1000L * 60 * 60 * 2;
    private static final long ACCESS_VALIDITY  = 1000L * 60 * 10;
    // 리프레시 토큰: 14일
    private static final long REFRESH_VALIDITY = 1000L * 60 * 60 * 24 * 14;

    /** 액세스 토큰 생성 */
    public String createAccessToken(String email) {
        Date now = new Date();
        return Jwts.builder()
                .setSubject(email)
                .setIssuedAt(now)
                .setExpiration(new Date(now.getTime() + ACCESS_VALIDITY))
                .signWith(SignatureAlgorithm.HS256, secret)
                .compact();
    }

    /** 리프레시 토큰 생성 */
    public String createRefreshToken(String email) {
        Date now = new Date();
        return Jwts.builder()
                .setSubject(email)
                .setIssuedAt(now)
                .setExpiration(new Date(now.getTime() + REFRESH_VALIDITY))
                .signWith(SignatureAlgorithm.HS256, secret)
                .compact();
    }

    /** 토큰에서 이메일(Subject) 추출 */
    public String getEmail(String token) {
        return Jwts.parser()
                .setSigningKey(secret)
                .parseClaimsJws(token)
                .getBody()
                .getSubject();
    }

    /** 토큰 유효성 검증 */
    public boolean validateToken(String token) {
        try {
            Jwts.parser().setSigningKey(secret).parseClaimsJws(token);
            return true;
        } catch (JwtException | IllegalArgumentException e) {
            return false;
        }
    }
}
