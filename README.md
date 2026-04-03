# Certi-Folio Backend

> **자격증·포트폴리오 관리 및 멘토링 플랫폼 백엔드 서버**

---

## 📌 프로젝트 소개

Certi-Folio는 사용자가 자격증, 교육이력, 경력, 프로젝트, 활동 정보 등을 통합 관리하고, 포트폴리오를 생성하며 멘토와 매칭될 수 있는 플랫폼입니다.  
본 레포지토리는 **Spring Boot 기반의 백엔드 API 서버**입니다.

---

## 🛠️ 기술 스택

| 분류 | 기술 |
|---|---|
| Language | Java 21 |
| Framework | Spring Boot 4.0.0 |
| Build Tool | Gradle |
| Database | H2 (파일 기반, 개발용) |
| ORM | Spring Data JPA / Hibernate |
| 인증 | Spring Security + OAuth2 (Google, Naver, Kakao) + JWT |
| 실시간 통신 | WebSocket (STOMP) |
| 유틸리티 | Lombok |

---

## 📁 프로젝트 구조

```
src/main/java/com/certifolio/server/
├── config/                  # 보안, WebSocket 설정
├── auth/                    # JWT 인증, OAuth2 핸들러
│   ├── jwt/
│   ├── handler/
│   ├── service/
│   ├── userinfo/
│   └── util/
├── User/                    # 사용자 관리
├── Form/                    # 포트폴리오 폼 항목 관리
│   ├── Activity/            # 대외활동
│   ├── Career/              # 경력
│   ├── Certificate/         # 자격증
│   ├── CodingTest/          # 코딩테스트 (Solved.ac 연동)
│   ├── Education/           # 교육이력
│   └── Project/             # 프로젝트
├── Mentoring/               # 멘토링 (멘토, 신청, 세션, 채팅)
├── Notification/            # 알림 (자격증 만료 등 스케줄러)
└── Analytics/               # 통계 분석
```

---

## ⚙️ 환경 설정

### 1. `application-secret.yml` 생성

`src/main/resources/` 경로에 `application-secret.yml` 파일을 생성하고 아래 내용을 채워주세요.

```yaml
spring:
  security:
    oauth2:
      client:
        registration:
          naver:
            client-id: YOUR_NAVER_CLIENT_ID
            client-secret: YOUR_NAVER_CLIENT_SECRET
          kakao:
            client-id: YOUR_KAKAO_CLIENT_ID
            client-secret: YOUR_KAKAO_CLIENT_SECRET
          google:
            client-id: YOUR_GOOGLE_CLIENT_ID
            client-secret: YOUR_GOOGLE_CLIENT_SECRET

jwt:
  secret: YOUR_BASE64_ENCODED_SECRET_KEY
```

> ⚠️ `application-secret.yml`은 `.gitignore`에 포함되어 있으며 **절대 커밋하지 마세요.**

### 2. 데이터베이스

개발 환경에서는 **H2 파일 기반 DB**를 사용합니다. 별도 설치 없이 서버 실행 시 자동 생성됩니다.

- 파일 경로: `./data/certifoliodb`
- H2 콘솔: [http://localhost:8080/h2-console](http://localhost:8080/h2-console)
  - JDBC URL: `jdbc:h2:file:./data/certifoliodb`
  - Username: `sa`
  - Password: (없음)

---

## 🚀 실행 방법

### Gradle Wrapper 사용

```bash
# Windows
./gradlew.bat bootRun

# macOS / Linux
./gradlew bootRun
```

서버 기본 포트: **8080**

---

## 🔐 인증 방식

- **OAuth2 소셜 로그인**: Google, Naver, Kakao
- 로그인 후 **JWT 토큰** 발급 → 이후 API 요청 시 `Authorization: Bearer <token>` 헤더로 전달
- 프론트엔드 OAuth2 콜백 URI: `http://localhost:3000/auth/callback`

---

## 📡 주요 API 엔드포인트

| 기능 | 메서드 | 경로 |
|---|---|---|
| 사용자 정보 | GET | `/api/users/me` |
| 자격증 CRUD | GET/POST/PUT/DELETE | `/api/certificates` |
| 교육이력 CRUD | GET/POST/PUT/DELETE | `/api/educations` |
| 경력 CRUD | GET/POST/PUT/DELETE | `/api/careers` |
| 프로젝트 CRUD | GET/POST/PUT/DELETE | `/api/projects` |
| 대외활동 CRUD | GET/POST/PUT/DELETE | `/api/activities` |
| 코딩테스트 CRUD | GET/POST/PUT/DELETE | `/api/coding-tests` |
| 포트폴리오 생성 | POST | `/api/portfolios` |
| 멘토 목록 | GET | `/api/mentors` |
| 멘토링 신청 | POST | `/api/applications` |
| 멘토링 세션 | GET/POST | `/api/sessions` |
| 채팅 | WebSocket | `/ws/chat` |
| 알림 목록 | GET | `/api/notifications` |

---

## 💬 실시간 채팅

WebSocket을 이용한 실시간 채팅 기능을 제공합니다.

- **연결 엔드포인트**: `ws://localhost:8080/ws`
- **메시지 발행**: `/app/chat.send`
- **구독**: `/topic/chatroom/{roomId}`

---

## 📬 알림

`CertificateNotificationScheduler`를 통해 자격증 만료 임박 시 자동 알림이 생성됩니다.

---

## 🗂️ 관련 레포지토리

- **프론트엔드**: https://github.com/Jong0128/Certi-Folio-Front_v3.git

---

## 👥 팀 정보

| 이름 | 역할 |
|---|---|
| 임종훈 | Backend Developer |
| 남윤성 | Backend Developer |

---

## 📄 커밋 컨벤션

feat: 새로운 기능 추가
fix: 버그 수정
docs: 문서 수정
style: 코드 스타일 변경 (코드 포매팅, 세미콜론 누락 등)
design: 사용자 UI 디자인 변경 (CSS 등)
test: 테스트 코드, 리팩토링 (Test Code)
refactor: 리팩토링 (Production Code)
build: 빌드 파일 수정
ci: CI 설정 파일 수정
perf: 성능 개선
chore: 자잘한 수정이나 빌드 업데이트
rename: 파일 혹은 폴더명을 수정만 한 경우
remove: 파일을 삭제만 한 경우


--
## 📄 라이선스

본 프로젝트는 내부 학습/개발 목적으로 작성되었습니다.
