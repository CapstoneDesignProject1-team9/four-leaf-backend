# 🍀 four-leaf-backend

> **Four-Leaf** 프로젝트의 백엔드 서비스입니다.  
> 사용자 인증, 데이터 관리, AI 서비스와의 연동을 담당하는 RESTful API 서버입니다.

---

## 🏗 기술 스택

| 분류 | 기술 |
|------|------|
| 프레임워크 | Spring Boot 3.3.4 |
| 언어 | Java 21 |
| 빌드 도구 | Maven 3.9 |
| 데이터베이스 | PostgreSQL 16 |
| 헬스체크 | Spring Boot Actuator |
| 컨테이너 | Docker (eclipse-temurin JRE 21 alpine) |
| CI | GitHub Actions |

---

## 🏛 아키텍처

```
클라이언트 (Frontend / AI 서비스)
      │
      ▼
Nginx /api/ 경로 → Backend :8080
      │
      ├── REST API Layer  (Controller)
      ├── Service Layer   (Business Logic)
      ├── Repository Layer (Spring Data JPA)
      │
      ├── PostgreSQL :5432  (데이터 영속화)
      └── Redis      :6379  (캐시 / 세션)
```

### Docker 이미지 구조 (멀티스테이지)

```
Stage 1: eclipse-temurin:21-jdk-alpine
  → mvn package -DskipTests → target/*.jar

Stage 2: eclipse-temurin:21-jre-alpine (JRE만 포함, 이미지 경량화)
  → non-root 사용자로 실행
  → JVM 컨테이너 메모리 자동 인식 (-XX:+UseContainerSupport)
```

---

## 📁 프로젝트 구조

```
four-leaf-backend/
├── .github/
│   └── workflows/
│       ├── ci.yml            # PR/push 시 Maven build + test
│       └── trigger-cd.yml    # main push 시 ECR push → infra dispatch
├── src/
│   ├── main/
│   │   ├── java/com/fourleaf/backend/
│   │   │   ├── BackendApplication.java    # 메인 클래스
│   │   │   └── controller/
│   │   │       └── HealthController.java  # GET /api/hello
│   │   └── resources/
│   │       └── application.yml            # dev / prod / test 프로파일
│   └── test/
│       └── java/com/fourleaf/backend/
│           └── BackendApplicationTests.java  # 컨텍스트 로드 테스트
├── Dockerfile                # 프로덕션 (JRE21, non-root)
├── Dockerfile.dev            # 로컬 개발용
└── pom.xml                   # Maven 의존성
```

---

## 📦 의존성

| 의존성 | 용도 |
|--------|------|
| `spring-boot-starter-web` | REST API |
| `spring-boot-starter-actuator` | 헬스체크 (`/actuator/health`) |
| `spring-boot-starter-data-jpa` | ORM / DB 연동 |
| `postgresql` | PostgreSQL 드라이버 (runtime) |
| `h2` | 인메모리 DB (test scope만) |

---

## 🔄 CI/CD 파이프라인

### CI (`ci.yml`) — PR + main push

```
mvn compile
  → mvn test (test 프로파일 / H2 인메모리 DB)
  → mvn package -DskipTests
```

> **test 프로파일**: PostgreSQL 없이 H2 인메모리 DB로 테스트 → GitHub Actions에서 별도 DB 없이 실행 가능

### CD (`trigger-cd.yml`) — main push만

```
CI 통과 (mvn verify)
  → mvn package -DskipTests (JAR 생성)
  → Docker image build
  → AWS ECR push (SHA tag + latest)
  → repository_dispatch → four-leaf-infra
    → EC2에 자동 배포
```

---

## 🌐 API 엔드포인트

| Method | 경로 | 설명 |
|--------|------|------|
| `GET` | `/api/hello` | 기본 동작 확인 |
| `GET` | `/actuator/health` | 헬스체크 (CD 파이프라인 사용) |

---

## 🔧 Spring 프로파일

| 프로파일 | 사용 환경 | DB |
|----------|-----------|-----|
| `dev` | 로컬 Docker (`docker-compose.dev.yml`) | PostgreSQL (컨테이너) |
| `prod` | EC2 프로덕션 | PostgreSQL (환경변수 기반) |
| `test` | CI / 단위테스트 | H2 인메모리 |

---

## 💻 로컬 개발

### Maven으로 직접 실행

```bash
# Java 21, Maven 필요
export JAVA_HOME=$(/usr/libexec/java_home -v 21)

# 테스트 포함 빌드
mvn verify

# 개발 서버 실행 (dev 프로파일, PostgreSQL 별도 필요)
mvn spring-boot:run -Dspring-boot.run.profiles=dev
```

### Docker로 실행 (전체 스택)

```bash
cd ../four-leaf-infra
cp .env.dev.example .env.dev
docker compose -f docker-compose.dev.yml up -d --build
# http://localhost/api/hello
```

---

## 🌍 환경변수 (prod 프로파일)

| 변수 | 설명 |
|------|------|
| `SPRING_DATASOURCE_URL` | `jdbc:postgresql://db:5432/fourleaf` |
| `SPRING_DATASOURCE_USERNAME` | DB 유저명 |
| `SPRING_DATASOURCE_PASSWORD` | DB 비밀번호 |
| `JWT_SECRET` | JWT 서명 키 (32자 이상) |

---

## 🔐 GitHub Secrets (CD 사용 시)

| Secret | 설명 |
|--------|------|
| `AWS_ACCESS_KEY_ID` | ECR push 권한 IAM 키 |
| `AWS_SECRET_ACCESS_KEY` | IAM 시크릿 |
| `INFRA_DISPATCH_TOKEN` | infra 레포 트리거용 PAT |