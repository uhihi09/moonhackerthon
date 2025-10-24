# ===================================
# PING - SOS 긴급 구조 시스템
# 스프링부트 백엔드 Dockerfile (Render 배포용)
# ===================================

# Stage 1: Build (빌드 단계)
FROM gradle:8.5-jdk17 AS builder

# 작업 디렉토리 설정
WORKDIR /app

# Gradle 캐시 최적화를 위해 먼저 의존성 다운로드
COPY build.gradle settings.gradle ./
COPY gradle ./gradle
COPY gradlew ./

# 의존성 다운로드 (캐시 활용)
RUN ./gradlew dependencies --no-daemon

# 소스 코드 복사
COPY src ./src

# 빌드 실행 (테스트 제외)
RUN ./gradlew clean build -x test --no-daemon

# Stage 2: Runtime (실행 단계)
FROM eclipse-temurin:17-jre-alpine

# 작업 디렉토리
WORKDIR /app

# 빌드 결과물 복사 (Stage 1에서)
COPY --from=builder /app/build/libs/*.jar app.jar

# 환경 변수 기본값
ENV SPRING_PROFILES_ACTIVE=prod
ENV PORT=8080

# 포트 노출
EXPOSE ${PORT}

# 헬스체크 (Render가 서버 상태 확인)
HEALTHCHECK --interval=30s --timeout=3s --start-period=40s --retries=3 \
  CMD wget --quiet --tries=1 --spider http://localhost:${PORT}/ || exit 1

# 실행 명령어
ENTRYPOINT ["java", \
  "-Djava.security.egd=file:/dev/./urandom", \
  "-Dserver.port=${PORT}", \
  "-Dspring.profiles.active=${SPRING_PROFILES_ACTIVE}", \
  "-jar", \
  "app.jar"]