#!/bin/bash

# Create logs directory if it doesn't exist
mkdir -p logs

# RDS 환경 메모리 최적화
export JAVA_OPTS="-Xmx400m -Xms200m \
  -server \
  -XX:+UseG1GC \
  -XX:MaxGCPauseMillis=100 \
  -XX:+UseContainerSupport \
  -XX:MaxMetaspaceSize=128m \
  -XX:CompressedClassSpaceSize=32m \
  -Dfile.encoding=UTF-8"

# 환경 변수 확인 (디버깅용)
echo "=== Environment Check ==="
echo "SPRING_PROFILES_ACTIVE: $SPRING_PROFILES_ACTIVE"
echo "DATABASE_URL exists: $(if [ -n "$DATABASE_URL" ]; then echo "YES"; else echo "NO"; fi)"
echo "DB_USERNAME exists: $(if [ -n "$DB_USERNAME" ]; then echo "YES"; else echo "NO"; fi)"
echo "DB_PASSWORD exists: $(if [ -n "$DB_PASSWORD" ]; then echo "YES"; else echo "NO"; fi)"
echo "PORT: $PORT"

# 프로덕션 환경으로 애플리케이션 시작
java $JAVA_OPTS -Dspring.profiles.active=prod -jar build/libs/chamggae.jar 