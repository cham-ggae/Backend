# Build stage
FROM gradle:8.5-jdk17 AS build
WORKDIR /app
COPY build.gradle settings.gradle ./
COPY gradle ./gradle
COPY src ./src
RUN gradle clean bootJar -x test --no-daemon

# Runtime stage
FROM openjdk:17-jdk-slim
WORKDIR /app
COPY --from=build /app/build/libs/chamggae.jar app.jar

# Create logs directory
RUN mkdir -p logs

EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"] 