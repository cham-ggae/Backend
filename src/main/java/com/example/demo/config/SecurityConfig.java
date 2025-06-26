package com.example.demo.config;

import com.example.demo.provider.JwtFilter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
import org.springframework.http.HttpMethod;
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
                        .requestMatchers("/authorize", "/oauth2/callback/kakao", "/logout", "/kakao", "/refresh", "/ws/**").permitAll()
                        .requestMatchers(HttpMethod.GET, "/surveyResult/*").permitAll()

                        // WebSocket 관련
                        .requestMatchers("/ws/**").permitAll()

                        // Actuator health check
                        .requestMatchers("/actuator/health").permitAll()

                        // Swagger UI 관련 경로들 (개발 환경용)
                        .requestMatchers("/swagger-ui/**", "/swagger-ui.html").permitAll()
                        .requestMatchers("/v3/api-docs/**", "/api-docs/**").permitAll()
                        .requestMatchers("/swagger-resources/**").permitAll()
                        .requestMatchers("/webjars/**").permitAll()

                        // CORS preflight 요청 허용 (모든 경로)
                        .requestMatchers(org.springframework.http.HttpMethod.OPTIONS, "/**").permitAll()

                        // 에러 페이지
                        .requestMatchers("/error").permitAll()

                        // 사용자 추가 정보 업데이트 엔드포인트 (인증 필요)
                        .requestMatchers("/api/user/additional-info").authenticated()

                        // 사용자 정보 조회 (인증 필요)
                        .requestMatchers("/user").authenticated()

                        // 기타 모든 요청
                        .anyRequest().authenticated()
                )
                // JWT 필터를 UsernamePasswordAuthenticationFilter 앞에 배치
                // 이렇게 하면 CORS 필터 → JWT 필터 → UsernamePasswordAuthenticationFilter 순서가 됨
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
                    "https://modi-peach.vercel.app",
                    "https://modi-backend-th1n.onrender.com",
                    "http://localhost:3000"
            );
            config.setAllowedOrigins(allowedOrigins);
            log.info("Production CORS allowed origins: {}", allowedOrigins);
        } else {
            // 개발 환경
            List<String> allowedOrigins = List.of(
                    "http://localhost:3000",
                    "http://localhost:8080",
                    "http://localhost:8090"
            );
            config.setAllowedOrigins(allowedOrigins);
            log.info("Development CORS allowed origins: {}", allowedOrigins);
        }

        // 모든 헤더 허용
        config.setAllowedHeaders(List.of("*"));

        // 모든 필요한 HTTP 메서드 허용
        config.setAllowedMethods(List.of(
                "GET", "POST", "PUT", "DELETE", "OPTIONS", "PATCH", "HEAD"
        ));

        // 클라이언트에서 읽을 수 있는 헤더 설정
        config.setExposedHeaders(List.of(
                "Authorization",
                "Content-Type",
                "X-Requested-With",
                "Accept",
                "Origin",
                "Access-Control-Request-Method",
                "Access-Control-Request-Headers"
        ));

        // preflight 요청 캐시 시간 (1시간)
        config.setMaxAge(3600L);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);
        return source;
    }
}