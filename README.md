# vibe-coding-practice

> AI 툴(Cursor, Windsurf)을 활용해 Spring Boot 프로젝트를 개발하며, 일관성 있는 코드를 만들기 위해 직접 설계한 개발 컨벤션을 담은 프로젝트입니다.

<br />

## 프로젝트 소개

바이브 코딩(Vibe Coding)을 접하면서 느낀 건, AI가 코드를 잘 만들어주긴 하는데
**일관성이 없다**는 것이었습니다.

같은 기능을 요청해도 매번 다른 방식으로 구현하고,
조금 전에 만든 코드 스타일을 다음 요청에서는 따르지 않기도 했습니다.

그래서 **명확한 컨벤션을 먼저 설계하고**, 그 컨벤션을 AI 툴에 규칙으로 전달해
일관성 있는 코드를 작성하는 방식으로 개발했습니다.

<br />

## Convention (개발 규칙)

단순한 규칙 나열이 아닌, **왜 이런 결정을 했는지**에 초점을 맞췄습니다.

### 목차

1. [기술 스택](#1-기술-스택)
2. [패키지 구조](#2-패키지-구조)
3. [API 응답 구조](#3-api-응답-구조)
4. [예외 처리](#4-예외-처리)
5. [Controller 컨벤션](#5-controller-컨벤션)
6. [DTO / Request 컨벤션](#6-dto--request-컨벤션)
7. [Service 컨벤션](#7-service-컨벤션)
8. [JPA / 영속성 컨벤션](#8-jpa--영속성-컨벤션)
9. [테스트 전략](#9-테스트-전략)

<br />

### 1. 기술 스택

| 분류 | 기술 |
|---|---|
| Language | Java 17 |
| Framework | Spring Boot 3.3.x |
| Build | Gradle (Kotlin DSL) |
| ORM | Spring Data JPA / Hibernate |
| DB (local/test) | H2 (in-memory) |
| DB (dev/prod) | MySQL 8.x |
| Utility | Lombok |
| Test | JUnit 5, Mockito, AssertJ |

<br />

### 2. 패키지 구조

도메인 중심의 레이어드 아키텍처를 사용합니다.

```
src/main/java/com/example/{프로젝트명}/
├── domain/
│   └── todo/
│       ├── controller/
│       ├── service/
│       ├── repository/
│       ├── entity/
│       └── dto/
│           ├── request/    # TodoCreateRequest.java, TodoCreateServiceRequest.java
│           └── response/   # TodoResponse.java
├── global/
│   ├── config/
│   ├── exception/
│   ├── response/
│   └── util/
└── {프로젝트명}Application.java
```

**왜 도메인 중심인가?**

레이어 중심(`controller/`, `service/`, `repository/`)으로 구성하면 기능이 늘어날수록 각 패키지가 비대해지고, 하나의 도메인을 파악하기 위해 여러 패키지를 오가야 합니다. 도메인 중심으로 구성하면 관련 코드가 한 곳에 모여 응집도가 높아지고, 도메인 단위로 코드를 이해하기 쉬워집니다.

<br />

### 3. API 응답 구조

모든 API 응답은 `ApiResponse<T>`로 통일합니다.

```json
// 성공
{
  "status": 200,
  "success": true,
  "data": { "id": 1, "title": "할일" }
}

// 실패
{
  "status": 404,
  "success": false,
  "error": {
    "code": "TODO_NOT_FOUND",
    "message": "존재하지 않는 할일입니다."
  }
}
```

**왜 공통 응답 래퍼를 사용하는가?**

클라이언트가 성공/실패 여부를 일관된 구조로 처리할 수 있습니다. `success` 필드 하나로 분기 처리가 가능하고, 에러 발생 시 `code`와 `message`를 통해 원인을 명확히 전달할 수 있습니다.

<br />

### 4. 예외 처리

예외는 `CustomException(ErrorCode)` 하나로 통일합니다. 도메인/에러별로 예외 클래스를 각각 정의하지 않습니다.

```java
// ✅ Good
throw new CustomException(ErrorCode.TODO_NOT_FOUND);

// ❌ Bad
throw new TodoNotFoundException(id);
```

**왜 예외 클래스를 하나로 통일하는가?**

에러마다 예외 클래스를 정의하면 클래스 수가 급격히 늘어나고 `ExceptionHandler`도 분산됩니다. `ErrorCode` Enum 하나로 모든 에러의 상태 코드, 코드명, 메시지를 중앙 관리하면 새로운 에러 추가 시 Enum에만 항목을 추가하면 됩니다.

<br />

**HTTP 상태 코드 기준**

| 상황 | 상태 코드 |
|---|---|
| 성공 (조회/생성/수정/삭제) | 200 OK |
| 유효성 검사 실패 | 400 Bad Request |
| 인증 실패 | 401 Unauthorized |
| 권한 없음 | 403 Forbidden |
| 리소스 없음 | 404 Not Found |
| 데이터 중복 / 상태 충돌 | 409 Conflict |
| 비즈니스 로직 / 정책 위반 | 422 Unprocessable Entity |
| 서버 내부 오류 | 500 Internal Server Error |

<br />

### 5. Controller 컨벤션

#### Request → ServiceRequest 변환

Controller에서 받은 Request DTO는 Service로 직접 전달하지 않습니다. 반드시 ServiceRequest로 변환 후 전달합니다.

```java
// ✅ Good
public ApiResponse<TodoResponse> createTodo(@RequestBody TodoCreateRequest request) {
    return ApiResponse.ok(todoService.createTodo(TodoCreateServiceRequest.from(request)));
}

// ❌ Bad
public ApiResponse<TodoResponse> createTodo(@RequestBody TodoCreateRequest request) {
    return ApiResponse.ok(todoService.createTodo(request));
}
```

**파라미터 성격에 따른 기준**

| 파라미터 유형 | 처리 방식 |
|---|---|
| `@RequestBody` 복합 객체 | ServiceRequest로 변환 후 전달 |
| `@PathVariable`, `@RequestParam` 단일 파라미터 | 직접 전달 |
| `@RequestParam` 여러 개 | ServiceRequest로 묶어서 전달 |

<br />

**왜 ServiceRequest를 분리하는가?**

Request DTO에는 `@NotBlank`, `@RequestBody` 등 웹 레이어에 종속된 관심사가 담겨 있습니다. 이를 그대로 Service로 전달하면 Service가 웹 레이어에 의존하게 됩니다.

<br />

**왜 변환 메서드를 ServiceRequest에 두는가?**

`ServiceRequest.from(request)` 방식을 사용합니다. ServiceRequest가 자신이 어떻게 만들어지는지 스스로 알고 있는 것이 자연스럽고, 진입점이 여러 개일 때 `from()`을 오버로드하여 생성 책임을 한 곳에 응집시킬 수 있습니다. 이는 프로젝트 전반의 `from()` 컨벤션과도 일치합니다.

<br />

### 6. DTO / Request 컨벤션

#### Entity와 DTO 분리

Entity를 Controller 레이어에서 직접 반환하지 않습니다. Entity → DTO 변환은 DTO 내부의 정적 팩토리 메서드 `from()`을 사용합니다.

```java
// ✅ Good - DTO 내부에서 변환 책임
public static TodoResponse from(Todo todo) { ... }

// ❌ Bad - Service 내부에 변환 메서드 정의
private TodoResponse toResponse(Todo todo) { ... }
```

**왜 변환 메서드를 DTO에 두는가?**

변환의 결과물인 DTO가 자신이 어떻게 만들어지는지 아는 것이 응집도 측면에서 자연스럽습니다. Service에 변환 메서드가 흩어지면 Service의 책임이 늘어나고 메서드 수가 증가합니다.

#### Request DTO 용도별 분리

등록/수정 등 용도별로 Request 클래스를 분리합니다. `Validation groups`를 이용한 단일 클래스 통합 방식은 사용하지 않습니다.

**왜 용도별로 분리하는가?**

`groups`를 사용하면 하나의 클래스에 등록/수정 상황이 뒤섞여 가독성이 떨어집니다. 용도별로 분리하면 각 클래스의 역할이 명확하고, 공통 필드가 많은 경우 상위 클래스로 추출해 중복을 제거할 수 있습니다.

#### 정적 팩토리 메서드 네이밍

Effective Java 기준을 따릅니다.

| 메서드명 | 사용 상황 |
|---|---|
| `from(X x)` | 다른 타입을 변환하여 인스턴스 생성 (e.g., `TodoResponse.from(todo)`) |
| `of(A a, B b)` | 동일 레벨의 값 여러 개를 묶어 생성 (e.g., `ErrorBody.of(code, message)`) |

<br />

### 7. Service 컨벤션

- 트랜잭션은 Service 레이어에서 관리합니다. 조회 전용 메서드에는 `@Transactional(readOnly = true)`를 적용합니다.
- 생성자 주입을 원칙으로 합니다. 필드 주입(`@Autowired`)은 사용하지 않습니다. Lombok `@RequiredArgsConstructor`와 함께 사용하면 보일러플레이트 없이 불변 객체를 유지할 수 있습니다.

<br />

### 8. JPA / 영속성 컨벤션

- Lombok `@Data`는 프로젝트 전체에서 사용하지 않습니다. `@Getter`, `@Builder`, `@RequiredArgsConstructor` 등 필요한 어노테이션만 사용합니다. JPA Entity에서 `@Setter`가 열려있으면 의도치 않은 상태 변경이 발생할 수 있고, `@EqualsAndHashCode`는 연관관계가 있는 Entity에서 순환참조 문제를 일으킬 수 있습니다.
- Entity 간 연관관계 편의 메서드는 연관관계의 주인 쪽에 작성합니다.
- N+1 문제를 방지하기 위해 컬렉션 조회 시 `fetch join` 또는 `@EntityGraph`를 활용합니다.

<br />

### 9. 테스트 전략

#### 레이어별 테스트 방식

| 레이어 | 방식 | 사용 클래스 |
|---|---|---|
| Controller | `@WebMvcTest` 슬라이스 테스트 | `ControllerTestSupport` |
| Service | `@SpringBootTest` + 실제 DB 연동 | `IntegrationTestSupport` |
| Repository | `@DataJpaTest` 슬라이스 테스트 | `RepositoryTestSupport` |
| REST Docs | `standaloneSetup` 문서화 테스트 | `RestDocsSupport` |
| E2E | `@SpringBootTest` 전체 플로우 | `IntegrationTestSupport` |

**왜 Service 테스트에 Mock을 사용하지 않는가?**

Mockito로 Repository를 Mock하면 JPA 영속성 컨텍스트, 트랜잭션 전파, 지연 로딩 같은 실제 동작을 검증할 수 없습니다. Mock이 실제와 다르게 동작해 테스트는 통과하지만 운영에서 문제가 발생하는 거짓 양성(false positive) 위험이 있습니다. 실제 DB와 연동한 통합 테스트로 이 문제를 방지합니다.

<br />

**왜 E2E 통합 테스트는 기본 작성 대상이 아닌가?**

`@SpringBootTest`는 Spring Context 전체를 로딩하므로 테스트가 느립니다. Controller, Service, Repository 각 레이어 테스트로 충분히 커버되는 케이스에 E2E 테스트까지 작성하면 중복 검증이 됩니다. E2E 테스트는 명시적으로 요청이 있을 때만 작성합니다.

<br />

#### 테스트 패키지 구조

```
src/test/java/com/example/{프로젝트명}/
├── support/
│   ├── ControllerTestSupport.java
│   ├── IntegrationTestSupport.java
│   ├── RepositoryTestSupport.java
│   └── RestDocsSupport.java
├── domain/
│   └── todo/
│       ├── controller/
│       │   ├── TodoControllerTest.java       # extends ControllerTestSupport
│       │   └── TodoControllerDocsTest.java   # extends RestDocsSupport
│       ├── service/
│       │   └── TodoServiceTest.java          # extends IntegrationTestSupport
│       └── repository/
│           └── TodoRepositoryTest.java       # extends RepositoryTestSupport
└── integration/
    └── TodoIntegrationTest.java              # extends IntegrationTestSupport
```

**왜 테스트 패키지도 도메인 기준으로 구성하는가?**

메인 소스 패키지 구조를 미러링하면 테스트 대상 클래스와 테스트 클래스를 쉽게 대응시킬 수 있습니다. 레이어 기준으로 구성하면 `controller/` 패키지에 모든 도메인의 Controller 테스트가 모여 파악하기 어려워집니다.

<br />

**왜 REST Docs 테스트를 `controller/` 패키지에 함께 두는가?**

같은 Controller를 다루는 테스트는 같은 패키지에 모아야 응집도가 높아집니다. `docs/` 패키지를 별도로 만들면 도메인이 늘어날수록 패키지 수만 증가합니다.
