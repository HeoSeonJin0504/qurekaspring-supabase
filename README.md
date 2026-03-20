# Qureka Backend (Spring Boot)

강의 자료(PDF, PPTX)를 업로드하면 OpenAI API가 이를 분석하여  
요약본과 문제를 자동으로 생성하는 학습 플랫폼의 **백엔드 서버**입니다.

---

## 주요 기능

- 회원가입 / 로그인 (JWT Access Token + Refresh Token)
- PDF · PPTX 파일 업로드 및 텍스트 추출
- OpenAI API 기반 요약본 자동 생성 (학습 수준 · 전공 분야 선택 가능)
- OpenAI API 기반 문제 자동 생성 (객관식, 순서 배열, 참/거짓, 빈칸 채우기, 단답형, 서술형)
- 요약본 · 문제 저장, 조회, 수정, 삭제
- 즐겨찾기 폴더별 문제 관리
- SQL Injection 방어, Rate Limiting, 입력값 검증

---

## 기술 스택

| 구분 | 기술 |
|------|------|
| Language | Java 17 |
| Framework | Spring Boot 3.5 |
| Database | PostgreSQL (Supabase) · Spring Data JPA |
| Auth | JWT (jjwt 0.12) · BCrypt |
| File Parsing | Apache PDFBox 3.x · Apache POI 5.x |
| AI | Spring AI · OpenAI API (gpt-4o-mini) |
| Security | Spring Security · Bucket4j (Rate Limiting) |
| Build | Gradle |

---

## 프로젝트 구조

```
src/main/java/com/qureka/
├── domain/
│   ├── ai/
│   │   ├── AiController.java          # 파일 업로드 → 요약 / 문제 생성
│   │   ├── AiService.java
│   │   ├── FileExtractService.java    # PDF · PPTX 텍스트 추출
│   │   └── PromptManager.java        # 프롬프트 조합 관리
│   ├── auth/
│   │   ├── AuthController.java        # 토큰 갱신, 로그아웃, 검증
│   │   ├── AuthService.java
│   │   ├── RefreshToken.java
│   │   └── RefreshTokenRepository.java
│   ├── user/
│   │   ├── UserController.java        # 회원가입, 로그인, 아이디 중복 확인
│   │   ├── UserService.java
│   │   ├── User.java
│   │   └── UserRepository.java
│   ├── summary/
│   │   ├── SummaryController.java
│   │   ├── SummaryService.java
│   │   ├── UserSummary.java
│   │   └── SummaryRepository.java
│   ├── question/
│   │   ├── QuestionController.java
│   │   ├── QuestionService.java
│   │   ├── UserQuestion.java
│   │   └── QuestionRepository.java
│   └── favorite/
│       ├── FavoriteController.java
│       ├── FavoriteService.java
│       ├── FavoriteFolder.java
│       ├── FavoriteQuestion.java
│       └── dto/
│           └── FavoriteQuestionResponse.java
└── global/
    ├── config/
    │   ├── AppConfig.java             # ChatClient Bean 등록
    │   ├── CorsConfig.java
    │   ├── SecurityConfig.java
    │   ├── RateLimitFilter.java
    │   └── HealthController.java
    ├── security/
    │   ├── JwtAuthenticationFilter.java
    │   ├── JwtTokenProvider.java
    │   └── UserPrincipal.java
    ├── exception/
    │   ├── GlobalExceptionHandler.java
    │   ├── CustomException.java
    │   └── ErrorCode.java
    └── common/
        ├── ApiResponse.java
        └── ValidationUtil.java
```

---

## API 엔드포인트

### AI 생성
| 메서드 | 엔드포인트 | 인증 | 설명 |
|--------|-----------|:----:|------|
| POST | `/api/ai/summarize` | ✅ | 파일(PDF·PPTX) 업로드 후 요약 생성 |
| POST | `/api/ai/generate` | ✅ | 요약 텍스트 기반 문제 생성 |

### 인증
| 메서드 | 엔드포인트 | 인증 | 설명 |
|--------|-----------|:----:|------|
| POST | `/api/users/register` | | 회원가입 |
| POST | `/api/users/login` | | 로그인 |
| POST | `/api/users/check-userid` | | 아이디 중복 확인 |
| POST | `/api/auth/refresh-token` | | 액세스 토큰 갱신 |
| POST | `/api/auth/logout` | | 로그아웃 |
| GET | `/api/auth/verify` | ✅ | 토큰 검증 |

### 요약본
| 메서드 | 엔드포인트 | 인증 | 설명 |
|--------|-----------|:----:|------|
| POST | `/api/summaries` | ✅ | 요약본 저장 |
| GET | `/api/summaries/user/:userId` | | 사용자별 목록 조회 |
| GET | `/api/summaries/search/:userId` | | 요약본 검색 |
| GET | `/api/summaries/user/:userId/meta` | | 메타데이터 조회 |
| GET | `/api/summaries/:id` | | 상세 조회 |
| PATCH | `/api/summaries/:id/name` | ✅ | 이름 변경 |
| DELETE | `/api/summaries/:id` | ✅ | 삭제 |

### 문제
| 메서드 | 엔드포인트 | 인증 | 설명 |
|--------|-----------|:----:|------|
| POST | `/api/questions` | ✅ | 문제 저장 |
| GET | `/api/questions/user/:userId` | | 사용자별 목록 조회 |
| GET | `/api/questions/search/:userId` | | 문제 검색 |
| GET | `/api/questions/:id` | | 상세 조회 |
| PATCH | `/api/questions/:id/name` | ✅ | 이름 변경 |
| DELETE | `/api/questions/:id` | ✅ | 삭제 |

### 즐겨찾기
| 메서드 | 엔드포인트 | 인증 | 설명 |
|--------|-----------|:----:|------|
| GET | `/api/favorites/folders/:userId` | | 폴더 목록 조회 |
| POST | `/api/favorites/folders` | ✅ | 폴더 생성 |
| POST | `/api/favorites/folders/ensure-default` | ✅ | 기본 폴더 보장 |
| GET | `/api/favorites/folders/default/:userId` | | 기본 폴더 조회/생성 |
| DELETE | `/api/favorites/folders/:folderId` | ✅ | 폴더 삭제 |
| POST | `/api/favorites/questions` | ✅ | 즐겨찾기 추가 |
| DELETE | `/api/favorites/questions/:favoriteId` | ✅ | 즐겨찾기 제거 |
| GET | `/api/favorites/check/:userId/:questionId` | | 즐겨찾기 여부 확인 |
| POST | `/api/favorites/check-multiple/:userId` | | 여러 문제 즐겨찾기 상태 확인 |
| GET | `/api/favorites/questions/all/:userId` | | 전체 즐겨찾기 문제 조회 |
| GET | `/api/favorites/folders/:folderId/questions/:userId` | | 폴더별 문제 조회 |

---

## 데이터베이스 구조

### users
| 컬럼 | 타입 | 설명 |
|------|------|------|
| userindex | SERIAL PK | 사용자 고유 ID |
| userid | VARCHAR(20) UNIQUE | 로그인 아이디 |
| password | VARCHAR(255) | bcrypt 해시 |
| name | VARCHAR(50) | 이름 |
| age | SMALLINT | 나이 |
| gender | VARCHAR(10) | male / female |
| phone | VARCHAR(20) UNIQUE | 전화번호 |
| email | VARCHAR(100) UNIQUE | 이메일 (선택) |
| created_at | TIMESTAMPTZ | 가입일시 |

### user_summaries
| 컬럼 | 타입 | 설명 |
|------|------|------|
| selection_id | SERIAL PK | |
| user_id | INTEGER FK | |
| file_name | VARCHAR(255) | 원본 파일명 |
| summary_name | VARCHAR(255) | 요약본 이름 |
| summary_type | summary_type_enum | basic \| key_points \| topic \| outline \| keywords |
| summary_text | TEXT | 요약 내용 |
| created_at | TIMESTAMPTZ | |

### user_questions
| 컬럼 | 타입 | 설명 |
|------|------|------|
| selection_id | SERIAL PK | |
| user_id | INTEGER FK | |
| file_name | VARCHAR(255) | 원본 파일명 |
| question_name | VARCHAR(255) | 문제 세트 이름 |
| question_type | question_type_enum | multiple_choice \| sequence \| fill_in_the_blank \| true_false \| short_answer \| descriptive |
| question_data | JSONB | 문제 데이터 |
| created_at | TIMESTAMPTZ | |

### favorite_folders / favorite_questions
즐겨찾기 폴더와 문제를 관리합니다. `(user_id, question_id, question_index)` 조합으로 중복 추가를 방지합니다.

---

## 설치 및 실행

### 1. 저장소 클론
```bash
git clone <repository-url>
cd qurekaspring-supabase
```

### 2. 환경 설정
```bash
cp application-local.example.yml src/main/resources/application-local.yml
# application-local.yml 을 열어 DB · JWT · OpenAI 값 입력
```

### 3. 실행

**IntelliJ**
```
Run/Debug Configurations > Active profiles > local 입력 후 실행
```

**Gradle**
```bash
./gradlew bootRun --args='--spring.profiles.active=local'
```

서버가 실행되면 `http://localhost:8080` 에서 접근 가능합니다.

---

## 환경 설정 항목 (`application-local.yml`)

| 항목 | 필수 | 설명 |
|------|:----:|------|
| `spring.datasource.url` | ✅ | Supabase PostgreSQL Connection Pooler URL |
| `spring.datasource.username` | ✅ | DB 사용자명 |
| `spring.datasource.password` | ✅ | DB 비밀번호 |
| `spring.ai.openai.api-key` | ✅ | OpenAI API 키 |
| `jwt.access-token-secret` | ✅ | JWT 액세스 토큰 비밀키 (32자 이상) |
| `jwt.refresh-token-secret` | ✅ | JWT 리프레시 토큰 비밀키 (32자 이상) |
| `app.test-userid` | | 형식 검사 생략 테스트 계정 ID |
| `cors.allowed-origins` | ✅ | 허용할 프론트엔드 URL |

---

## 보안

- **SQL Injection 방어**: Spring Data JPA Parameterized Query
- **비밀번호 보안**: BCrypt 해싱
- **JWT 인증**: Access Token (1시간) + Refresh Token (7일)
- **Rate Limiting**: Bucket4j 기반 엔드포인트별 요청 제한
  - 회원가입 · 로그인: 15분당 5회
  - AI 생성: 30분당 10회
  - 저장: 10분당 10회
  - 토큰 갱신: 15분당 30회
  - 일반 API: 15분당 120회
- **CORS**: 허용된 도메인만 접근 가능

---

## 저장소

본 프로젝트는 4개의 저장소로 구성되어 있습니다.

- **백엔드 (Spring Boot)** — 현재 저장소
- **프론트엔드 (React)** — https://github.com/HeoSeonJin0504/qurekafront.git
- **백엔드 (Node.js)** — https://github.com/HeoSeonJin0504/qurekanode-supabase.git
- **AI 서버 (FastAPI)** — https://github.com/hanataba227/qureka-fastapi.git

> **백엔드는 Spring Boot 또는 Node.js 중 하나만 선택하여 사용하면 됩니다.**  
> 두 서버는 동일한 기능을 제공하며 프론트엔드와 독립적으로 연동됩니다.  
> FastAPI 서버는 OpenAI API 호출 및 프롬프트 처리 전용으로 구현되어 있으며,  
> 현재 Spring Boot / Node.js 백엔드에 해당 기능(FastAPI)이 통합되어 있어 **별도 실행 없이도 동작합니다.**
