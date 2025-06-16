package com.example.demo.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.servers.Server;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.Components;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

/**
 * Swagger OpenAPI 3.0 설정 (JWT 인증 포함)
 * JWT 토큰을 통한 API 인증 테스트 지원
 *
 * @author 참깨라면팀
 * @since 1.0
 */
@Configuration
public class SwaggerConfig {

    private static final String SECURITY_SCHEME_NAME = "bearerAuth";

    /**
     * OpenAPI 3.0 설정 (JWT 인증 포함)
     */
    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
                .info(apiInfo())
                .servers(List.of(
                        new Server().url("http://localhost:8090").description("로컬 개발 서버")
                ))
                .components(new Components()
                        .addSecuritySchemes(SECURITY_SCHEME_NAME, createAPIKeyScheme()))
                .addSecurityItem(new SecurityRequirement().addList(SECURITY_SCHEME_NAME));
    }

    /**
     * API 기본 정보 설정
     */
    private Info apiInfo() {
        return new Info()
                .title("MODi Family Space API")
                .description("""
                    """)
                .version("1.0.0")
                .contact(new Contact()
                        .name("참깨라면팀")
                        .email("pillow12360@gmail.com")
                        .url("https://github.com/cham-ggae"))
                .license(new License()
                        .name("MIT License")
                        .url("https://opensource.org/licenses/MIT"));
    }

    /**
     * JWT Bearer Token 인증 스키마 생성
     */
    private SecurityScheme createAPIKeyScheme() {
        return new SecurityScheme()
                .type(SecurityScheme.Type.HTTP)
                .scheme("bearer")
                .bearerFormat("JWT")
                .in(SecurityScheme.In.HEADER)
                .name("Authorization")
                .description("""
                    JWT 토큰을 입력하세요.
                    
                    **형식**: Bearer {token}
                    **예시**: Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...
                    
                    **토큰 획득 방법**:
                    1. 프론트 측에서 API로 카카오 로그인
                    2. 아무 요청 보내고 요청 헤더 정보에 Bearer 토큰 복사
                    3. 여기에 전체 값 붙여넣기
                    """);
    }
}