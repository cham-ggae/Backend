# Chamggae Backend

Spring Boot 기반 백엔드 애플리케이션

## 🗄️ 데이터베이스 설정

### 로컬 개발 환경 (MariaDB)

```bash
# 환경 변수 설정
export SPRING_PROFILES_ACTIVE=local
export DB_PATH=jdbc:mariadb://localhost:3306/demo
export DB_MARIA_USERNAME=root
export DB_MARIA_PASSWORD=your_password

# 애플리케이션 실행
./gradlew bootRun --args='--spring.profiles.active=local'
```

### 프로덕션 환경 (PostgreSQL)

- Render에서 자동으로 PostgreSQL 데이터베이스 제공
- `DATABASE_URL` 환경 변수 자동 설정
- Profile: `prod`

## 🚀 실행 방법

### 로컬 실행

```bash
# Gradle로 실행
./gradlew bootRun --args='--spring.profiles.active=local'

# JAR로 실행
./gradlew bootJar
java -Dspring.profiles.active=local -jar build/libs/chamggae.jar
```

### 프로덕션 배포

```bash
# Render에 배포 (자동)
git push origin main
```

## 🔍 헬스체크

### 로컬 테스트

```bash
curl http://localhost:8080/actuator/health
# 응답에서 "database.product": "MariaDB" 확인
```

### 프로덕션 테스트

```bash
curl https://your-app.onrender.com/actuator/health
# 응답에서 "database.product": "PostgreSQL" 확인
```

## 📁 프로젝트 구조

```
src/main/resources/
├── application.yml          # 기본 설정
├── application-local.yml    # 로컬 MariaDB 설정
└── application-prod.yml     # 프로덕션 PostgreSQL 설정
```

## 🔧 주요 설정

### 데이터베이스 연결 풀 (HikariCP)

- **로컬**: 최대 10개 연결, 최소 5개 유휴
- **프로덕션**: 최대 3개 연결, 최소 1개 유휴 (Render Free tier 최적화)

### JVM 옵션 (프로덕션)

- 최대 힙: 350MB
- 초기 힙: 150MB
- GC: G1GC 사용
- 메타스페이스: 128MB

## 📝 환경 변수

### 필수 환경 변수

- `SPRING_PROFILES_ACTIVE`: 활성 프로파일 (local/prod)
- `DATABASE_URL`: PostgreSQL 연결 URL (프로덕션)
- `DB_PATH`: MariaDB 연결 URL (로컬)
- `DB_MARIA_USERNAME`: MariaDB 사용자명 (로컬)
- `DB_MARIA_PASSWORD`: MariaDB 비밀번호 (로컬)

### API 키

- `KAKAO`: 카카오 API 키
- `OPENAI_API_KEY`: OpenAI API 키
- `GOOGLE_CREDENTIALS_PATH`: Google Cloud 인증 파일 경로
- `jwtKey`: JWT 시크릿 키
