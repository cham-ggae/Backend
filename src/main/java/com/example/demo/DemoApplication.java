package com.example.demo;

import io.github.cdimascio.dotenv.Dotenv;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class DemoApplication {

	public static void main(String[] args) {
		// .env 파일이 존재하는 경우에만 로드 (로컬 개발용)
		Dotenv dotenv = Dotenv.configure()
				.ignoreIfMissing() // 파일이 없어도 에러 발생하지 않음
				.load();

		// Render 환경 감지 및 프로덕션 프로파일 강제 설정
		String renderEnv = System.getenv("RENDER");
		String databaseUrl = System.getenv("DATABASE_URL");

		if (renderEnv != null || databaseUrl != null) {
			System.setProperty("spring.profiles.active", "prod");
			System.setProperty("SPRING_PROFILES_ACTIVE", "prod");
			System.out.println("=== PRODUCTION MODE DETECTED ===");
			System.out.println("Active Profile: prod");
		} else {
			System.out.println("=== LOCAL MODE DETECTED ===");
			System.out.println("Active Profile: " + System.getProperty("spring.profiles.active", "local"));
		}

		// 환경변수 설정 (프로덕션 우선)
		setEnvironmentVariable("DATABASE_URL", dotenv);
		setEnvironmentVariable("DB_USERNAME", dotenv);
		setEnvironmentVariable("DB_PASSWORD", dotenv);

		// 로컬 환경 변수들
		setEnvironmentVariable("DB_MARIA_PASSWORD", dotenv);
		setEnvironmentVariable("DB_MARIA_USERNAME", dotenv);
		setEnvironmentVariable("DB_PATH", dotenv);

		// 서버 포트 설정
		setEnvironmentVariable("PORT", dotenv);

		// 카카오 로그인 설정
		setEnvironmentVariable("KAKAO", dotenv);
		setEnvironmentVariable("KAKAO_REDIRECT_URI", dotenv);
		setEnvironmentVariable("KAKAO_BROWSER_REDIRECT_URI", dotenv);

		// OpenAI 설정
		setEnvironmentVariable("OPENAI_API_KEY", dotenv);
		setEnvironmentVariable("OPENAI_MODEL", dotenv);

		// Google Cloud 설정
		setEnvironmentVariable("GOOGLE_CREDENTIALS_PATH", dotenv);
		setEnvironmentVariable("GOOGLE_BUCKET_NAME", dotenv);

		// JWT 설정
		setEnvironmentVariable("jwtKey", dotenv);

		// 시작 전 환경변수 확인
		printEnvironmentInfo();

		SpringApplication.run(DemoApplication.class, args);
	}

	private static void setEnvironmentVariable(String key, Dotenv dotenv) {
		// 시스템 환경변수가 우선 (Render 환경)
		String systemValue = System.getenv(key);
		if (systemValue != null) {
			System.setProperty(key, systemValue);
			return;
		}

		// .env 파일에서 값 가져오기 (로컬 환경)
		String dotenvValue = dotenv.get(key);
		if (dotenvValue != null) {
			System.setProperty(key, dotenvValue);
		}
	}

	private static void printEnvironmentInfo() {
		System.out.println("=== Environment Variables Check ===");
		System.out.println("DATABASE_URL: " + (System.getenv("DATABASE_URL") != null ? "SET" : "NOT SET"));
		System.out.println("DB_USERNAME: " + (System.getenv("DB_USERNAME") != null ? "SET" : "NOT SET"));
		System.out.println("DB_PASSWORD: " + (System.getenv("DB_PASSWORD") != null ? "SET" : "NOT SET"));
		System.out.println("SPRING_PROFILES_ACTIVE: " + System.getProperty("spring.profiles.active"));
		System.out.println("PORT: " + System.getProperty("PORT", "8080"));
		System.out.println("=====================================");
	}
}