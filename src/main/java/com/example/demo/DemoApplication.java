package com.example.demo;

import io.github.cdimascio.dotenv.Dotenv;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import java.util.Objects;

@SpringBootApplication
public class DemoApplication {

	public static void main(String[] args) {
		Dotenv dotenv = Dotenv.configure().load();
		
		// 데이터베이스 설정
		System.setProperty("DB_PASSWORD", Objects.requireNonNull(dotenv.get("DB_PASSWORD")));
		System.setProperty("DB_PATH", Objects.requireNonNull(dotenv.get("DB_PATH")));
		
		// 서버 포트 설정
		if (dotenv.get("PORT") != null) {
			System.setProperty("PORT", dotenv.get("PORT"));
		}
		
		// 카카오 로그인 설정
		if (dotenv.get("KAKAO") != null) {
			System.setProperty("KAKAO", dotenv.get("KAKAO"));
		}
		if (dotenv.get("KAKAO_REDIRECT_URI") != null) {
			System.setProperty("KAKAO_REDIRECT_URI", dotenv.get("KAKAO_REDIRECT_URI"));
		}
		if (dotenv.get("KAKAO_BROWSER_REDIRECT_URI") != null) {
			System.setProperty("KAKAO_BROWSER_REDIRECT_URI", dotenv.get("KAKAO_BROWSER_REDIRECT_URI"));
		}
		
		// OpenAI 설정
		if (dotenv.get("OPENAI_API_KEY") != null) {
			System.setProperty("OPENAI_API_KEY", dotenv.get("OPENAI_API_KEY"));
		}
		if (dotenv.get("OPENAI_MODEL") != null) {
			System.setProperty("OPENAI_MODEL", dotenv.get("OPENAI_MODEL"));
		}
		
		// Google Cloud 설정
		if (dotenv.get("GOOGLE_CREDENTIALS_PATH") != null) {
			System.setProperty("GOOGLE_CREDENTIALS_PATH", dotenv.get("GOOGLE_CREDENTIALS_PATH"));
		}
		if (dotenv.get("GOOGLE_BUCKET_NAME") != null) {
			System.setProperty("GOOGLE_BUCKET_NAME", dotenv.get("GOOGLE_BUCKET_NAME"));
		}
		
		SpringApplication.run(DemoApplication.class, args);
	}

}
