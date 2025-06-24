package com.example.demo;

import io.github.cdimascio.dotenv.Dotenv;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import java.util.Objects;

@SpringBootApplication
public class DemoApplication {

	public static void main(String[] args) {
		// .env 파일이 존재하는 경우에만 로드 (로컬 개발용)
		Dotenv dotenv = Dotenv.configure()
				.ignoreIfMissing() // 파일이 없어도 에러 발생하지 않음
				.load();
		
		// 데이터베이스 설정
		setSystemPropertyIfExists(dotenv, "DB_PASSWORD");
		setSystemPropertyIfExists(dotenv, "DB_PATH");
		
		// 서버 포트 설정
		setSystemPropertyIfExists(dotenv, "PORT");
		
		// 카카오 로그인 설정
		setSystemPropertyIfExists(dotenv, "KAKAO");
		setSystemPropertyIfExists(dotenv, "KAKAO_REDIRECT_URI");
		setSystemPropertyIfExists(dotenv, "KAKAO_BROWSER_REDIRECT_URI");
		
		// OpenAI 설정
		setSystemPropertyIfExists(dotenv, "OPENAI_API_KEY");
		setSystemPropertyIfExists(dotenv, "OPENAI_MODEL");
		
		// Google Cloud 설정
		setSystemPropertyIfExists(dotenv, "GOOGLE_CREDENTIALS_PATH");
		setSystemPropertyIfExists(dotenv, "GOOGLE_BUCKET_NAME");
		
		SpringApplication.run(DemoApplication.class, args);
	}

	private static void setSystemPropertyIfExists(Dotenv dotenv, String key) {
		String value = dotenv.get(key);
		if (value != null) {
			System.setProperty(key, value);
		}
	}

}
