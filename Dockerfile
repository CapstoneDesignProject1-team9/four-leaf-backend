# ─────────────────────────────────────────
# Stage 1: Build (Maven)
# ─────────────────────────────────────────
FROM eclipse-temurin:21-jdk-alpine AS builder

WORKDIR /app

# Maven 설치
RUN apk add --no-cache maven

# pom.xml 먼저 복사 → 의존성 캐시 레이어 활용
COPY pom.xml ./

# 의존성 다운로드 (소스 변경 시 재다운로드 방지)
RUN mvn dependency:go-offline -B

# 소스 복사 & 빌드 (테스트 제외)
COPY src ./src
RUN mvn package -DskipTests -B && \
    ls target/*.jar

# ─────────────────────────────────────────
# Stage 2: Run (JRE only)
# ─────────────────────────────────────────
FROM eclipse-temurin:21-jre-alpine

WORKDIR /app

# 보안: non-root 사용자로 실행
RUN addgroup -S spring && adduser -S spring -G spring
USER spring

# 빌드 결과물 복사
COPY --from=builder /app/target/*.jar app.jar

EXPOSE 8080

# JVM 옵션: 컨테이너 메모리 제한 인식
ENTRYPOINT ["java", \
  "-XX:+UseContainerSupport", \
  "-XX:MaxRAMPercentage=75.0", \
  "-Djava.security.egd=file:/dev/./urandom", \
  "-jar", "app.jar"]
