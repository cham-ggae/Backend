package com.example.demo.config;

import com.example.demo.provider.JwtFilter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.Arrays;
import java.util.List;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
@Slf4j
public class SecurityConfig {
    private final JwtFilter jwtFilter;

    @Autowired
    private Environment environment;

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                .csrf(AbstractHttpConfigurer::disable)
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth
                        // 기존 인증 없이 접근 가능한 엔드포인트
                        .requestMatchers("/authorize", "/oauth2/callback/kakao", "/logout", "/kakao", "/refresh","/ws/**").permitAll()

                        // Swagger UI 관련 경로들 (개발 환경용)
                        .requestMatchers("/swagger-ui/**", "/swagger-ui.html").permitAll()
                        .requestMatchers("/v3/api-docs/**", "/api-docs/**").permitAll()
                        .requestMatchers("/swagger-resources/**").permitAll()
                        .requestMatchers("/webjars/**").permitAll()

                        // 사용자 추가 정보 업데이트 엔드포인트 (인증 필요)
                        .requestMatchers("/api/user/additional-info").authenticated()

                        // 기타 모든 요청
                        .anyRequest().authenticated()
                )
                .addFilterBefore(jwtFilter, UsernamePasswordAuthenticationFilter.class)
                .formLogin(AbstractHttpConfigurer::disable) // 기본 로그인 페이지 비활성화
                .logout(AbstractHttpConfigurer::disable);   // 기본 로그아웃 비활성화

        return http.build();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration config = new CorsConfiguration();
        config.setAllowCredentials(true);
        
        // 환경별 허용 URL 설정
        boolean isProd = Arrays.asList(environment.getActiveProfiles()).contains("prod");
        String[] activeProfiles = environment.getActiveProfiles();
        
        log.info("Active profiles: {}", Arrays.toString(activeProfiles));
        log.info("Is production environment: {}", isProd);
        
        if (isProd) {
            // 프로덕션 환경 - 백엔드 도메인과 개발용 localhost 모두 허용
            List<String> allowedOrigins = List.of(
                "https://modi-backend-th1n.onrender.com",
                "http://localhost:3000"
            );
            config.setAllowedOrigins(allowedOrigins);
            log.info("Production CORS allowed origins: {}", allowedOrigins);
        } else {
            // 개발 환경
            List<String> allowedOrigins = List.of(
                "http://localhost:3000"
            );
            config.setAllowedOrigins(allowedOrigins);
            log.info("Development CORS allowed origins: {}", allowedOrigins);
        }
        
        config.setAllowedHeaders(List.of("*"));
        config.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "OPTIONS", "PATCH"));
        config.setExposedHeaders(List.of("Authorization", "Content-Type"));
        config.setMaxAge(3600L); // preflight 요청 캐시 시간 (1시간)
        
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);
        return source;
    }
}