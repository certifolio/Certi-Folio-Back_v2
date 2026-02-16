# Certi-Folio 백엔드 개선 진행 기록

> **최종 업데이트**: 2026-02-16
> 이 파일은 백엔드 기능 수정/추가 시 참고용 기록입니다.

---

## ✅ 완료된 작업 (2026-02-16)

### 1. 코드 리팩토링

#### `AuthUtils` 공통 유틸리티 추출
- **파일**: `src/main/java/com/certifolio/server/auth/util/AuthUtils.java` (신규)
- **내용**: 4개 컨트롤러에 중복된 `getUser(Object principal)` 로직을 `AuthUtils.resolveUser()`로 통합
- **적용 대상**:
  - `MentorController`
  - `MentoringApplicationController`
  - `MentoringSessionController`
  - `UserController`
  - `PortfolioController` (`getUserId()` 메서드도 리팩토링)

#### `GlobalExceptionHandler` 추가
- **파일**: `src/main/java/com/certifolio/server/config/GlobalExceptionHandler.java` (신규)
- **내용**: `@RestControllerAdvice`로 전역 예외를 JSON 형식으로 통일 응답
  - `IllegalArgumentException` → 400 Bad Request
  - `RuntimeException` → 상황별 (401/403/404/500)
  - 기타 `Exception` → 500 Internal Server Error

### 2. 포트폴리오 CRUD 완성

#### 수정(PUT) 엔드포인트 추가
| 엔드포인트 | 설명 |
|---|---|
| `PUT /api/portfolio/certificates/{id}` | 자격증 수정 |
| `PUT /api/portfolio/projects/{id}` | 프로젝트 수정 |
| `PUT /api/portfolio/activities/{id}` | 대외활동 수정 |
| `PUT /api/portfolio/careers/{id}` | 경력 수정 |
| `PUT /api/portfolio/educations/{id}` | 학력 수정 |

#### 삭제(DELETE) 엔드포인트 추가
| 엔드포인트 | 설명 |
|---|---|
| `DELETE /api/portfolio/activities/{id}` | 대외활동 삭제 |
| `DELETE /api/portfolio/careers/{id}` | 경력 삭제 |
| `DELETE /api/portfolio/educations/{id}` | 학력 삭제 |

> 기존에 있던 `DELETE /certificates/{id}`, `DELETE /projects/{id}`는 유지

#### 관련 수정 파일
- **도메인 엔티티** (5개 모두 `update()` 메서드 추가):
  - `Certificate.java`, `Project.java`, `Activity.java`, `Career.java`, `Education.java`
- **서비스**: `PortfolioServiceImpl.java` (update/delete 메서드 8개 추가)
- **컨트롤러**: `PortfolioController.java` (PUT/DELETE 엔드포인트 8개 추가)

### 3. SecurityConfig 수정
- `/api/mentoring-requests` → `/api/mentoring-applications`으로 경로 수정
- Swagger UI 접근 허용 설정 추가 (`/swagger-ui/**`, `/v3/api-docs/**`)

### 4. 패키지 케이스 수정 (추가 발견)
- `Auth` → `auth`로 패키지 선언 수정 (5개 파일)
  - `OAuth2UserInfo.java`, `GoogleOAuth2UserInfo.java`
  - `JwtAuthenticationFilter.java`, `JwtTokenProvider.java`
  - `OAuth2SuccessHandler.java`

---

## 📋 남은 작업 (TODO)

### 프론트엔드 필요 작업
- [ ] 멘토링 타입 camelCase ↔ snake_case 통일
- [ ] 정보 필드 초기화 로직 (제출 성공 시)
- [ ] 빈 값일 때 UI 텍스트 처리

### 중간 우선순위
- [ ] 알림 설정 기능
- [ ] 긴급 알림 버튼
- [ ] UI 블러 효과
- [ ] 자격증 요약 개수 제한
- [ ] 다크 모드

### 낮은 우선순위
- [ ] 캘린더 리디자인
- [ ] 역량 분석 구조

### 배포
- [ ] AWS EC2 배포
- [ ] S3 연동
- [ ] CORS 설정 (운영 도메인 추가)
- [ ] ngrok 의존성 제거
- [ ] 환경변수 관리
