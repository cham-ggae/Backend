package com.example.demo.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

/**
 * 개발용 Swagger OpenAPI 3.0 설정
 * 인증 없이 모든 API 테스트 가능
 *
 * @author 참깨라면팀
 * @since 1.0
 */
@Configuration
public class SwaggerConfig {

    /**
     * OpenAPI 3.0 설정 (개발용 - 인증 비활성화)
     */
    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
                .info(apiInfo())
                .servers(List.of(
                        new Server().url("http://localhost:8090").description("로컬 개발 서버")
                ));
    }

    /**
     * API 기본 정보 설정
     */
    private Info apiInfo() {
        return new Info()
                .title("MODi API (개발용)")
                .description("""
                    MODi: 모두의 디지털 - 가족 스페이스 API 문서
                    
                    **개발 모드**: 모든 API가 인증 없이 테스트 가능합니다.
                    
                    ## 사용 방법
                    1. API 선택
                    2. "Try it out" 클릭
                    3. X-User-Id 헤더에 테스트용 사용자 ID 입력 (예: 1, 2, 3)
                    4. Request Body 입력 후 "Execute"
                    
                    ## 테스트 사용자 ID
                    - 사용자 1: uid = 1
                    - 사용자 2: uid = 2
                    - 사용자 3: uid = 3
                    """)
                .version("1.0.0-dev")
                .contact(new Contact()
                        .name("참깨라면팀")
                        .email("pillow12360@gmail.com")
                        .url("https://github.com/cham-ggae"))
                .license(new License()
                        .name("MIT License")
                        .url("https://opensource.org/licenses/MIT"));
    }
}