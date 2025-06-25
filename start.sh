#!/bin/bash

# Create logs directory if it doesn't exist
mkdir -p logs

# Render Free Tier 메모리 최적화 (512MB RAM 제한)
export JAVA_OPTS="-Xmx350m -Xms150m \
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
echo "PORT: $PORT"

# 프로덕션 환경으로 애플리케이션 시작
java $JAVA_OPTS -Dspring.profiles.active=prod -jar build/libs/chamggae.jar 