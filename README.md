# ai-backend-conventions

> AI 에이전트가 일관성 있는 Spring Boot 백엔드 코드를 작성하도록  
> 프로젝트 규칙을 설계하고, 실제 기능 구현에 적용하며 검증하는 토이 프로젝트입니다.

## 프로젝트 목표

이 프로젝트는 단순히 Todo API를 만드는 예제가 아닙니다.

AI 에이전트를 활용해 백엔드 기능을 개발할 때 매 요청마다 구조와 스타일이 흔들리는 문제를 줄이기 위해, 먼저 프로젝트 규칙을 정의하고 그 규칙을 실제 코드에 적용해 검증하는 것을 목표로 합니다.

즉, 이 프로젝트는 다음 질문들을 실제로 검증하는 실험입니다.

- AI 에이전트가 일관성 있는 코드를 작성하려면 어떤 규칙이 필요한가?
- 규칙이 코드, 테스트, 문서까지 실제로 반영되도록 하려면 어떻게 설계해야 하는가?
- Spring Boot 백엔드에서 재사용 가능한 수준의 규칙 체계를 어떻게 만들 수 있는가?

## 현재 구현 범위

현재 프로젝트에는 `Todo`와 `Auth` 도메인이 구현되어 있습니다.

### Todo

- Todo 생성
- Todo 단건 조회
- Todo 목록 조회
- Todo 수정
- Todo 완료 처리
- Todo 삭제

### Auth

- 회원가입
- 로그인
- 토큰 재발급
- 로그아웃
- JWT 기반 인증 / 인가

## 이 프로젝트에서 검증한 규칙

현재 코드에는 아래와 같은 규칙이 실제로 반영되어 있습니다.

- 도메인 중심 패키지 구조
- `ApiResponse<T>` 기반 공통 응답 구조
- `CustomException(ErrorCode)` 기반 예외 처리
- Controller의 `Request DTO -> ServiceRequest` 변환
- DTO 내부 `from()` 정적 팩토리 메서드 사용
- Service 레이어에서 트랜잭션 관리
- `MemberService`와 `AuthService`의 책임 분리
- JWT access / refresh 토큰 타입 구분
- `MemberPrincipal` 기반 인증 principal 계약 통일
- Spring REST Docs 기반 API 문서화
- `LocalDateTime`의 ISO-8601 문자열 응답 / 문서화
- REST Docs `Required` 컬럼 및 섹션 순서 표준화

## 문서 구조

프로젝트 규칙은 하나의 도구 전용 파일이 아니라, 역할별 문서로 분리해 관리합니다.

```text
AGENTS.md
docs/
  architecture/
    tech-stack.md
    layers.md
  conventions/
    coding.md
    naming.md
    exceptions.md
    testing.md
  api/
    api-response.md
```

에이전트는 [AGENTS.md](./AGENTS.md)를 진입점으로 삼아 위 문서를 순서대로 참고합니다.

## 테스트 전략

현재 테스트는 아래 계층으로 구분되어 있습니다.

- `ControllerTest`: `@WebMvcTest` 기반 슬라이스 테스트
- `ServiceTest`: Service + Repository 실제 연동 테스트
- `RepositoryTest`: JPA 슬라이스 테스트
- `RestDocsTest`: REST Docs 문서화 테스트
- `integration/*Test`: `MockMvc` 기반으로 HTTP 요청부터 Controller, Service, Repository까지 관통하는 전체 플로우 테스트

## API 문서

Spring REST Docs와 AsciiDoc으로 문서를 생성합니다.

- 문서 진입점: [src/docs/asciidoc/index.adoc](./src/docs/asciidoc/index.adoc)
- 기능 문서:
    - [auth.adoc](./src/docs/asciidoc/sections/auth.adoc)
    - [todo.adoc](./src/docs/asciidoc/sections/todo.adoc)

`build` 시 문서가 생성되고 정적 리소스로 복사되도록 설정되어 있습니다.

## 기술 스택

| 분류 | 기술 |
|---|---|
| Language | Java 17 |
| Framework | Spring Boot 3.3.5 |
| Build | Gradle |
| Data | Spring Data JPA, H2, Redis |
| Security | Spring Security, JWT |
| Docs | Spring REST Docs, AsciiDoc |
| Test | JUnit 5, Mockito, AssertJ, Spring Security Test |

## 실행 환경 변수

민감한 설정은 환경변수로 주입합니다.

| 변수명 | 설명 |
|---|---|
| `JWT_SECRET` | JWT 서명 시크릿 |
| `REDIS_HOST` | Redis 호스트 (기본값 `localhost`) |
| `REDIS_PORT` | Redis 포트 (기본값 `6379`) |
| `REDIS_PASSWORD` | Redis 비밀번호 |

예시:

```env
JWT_SECRET=replace-with-random-secret
REDIS_HOST=localhost
REDIS_PORT=6379
REDIS_PASSWORD=replace-with-redis-password
```

## 실행 방법

터미널에서 `.env`를 로드한 뒤 실행할 수 있습니다.

```bash
set -a
source .env
set +a
./gradlew bootRun
```

테스트와 문서 생성도 같은 방식으로 실행합니다.

```bash
set -a
source .env
set +a
./gradlew test
./gradlew asciidoctor
```