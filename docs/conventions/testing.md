# 테스트 규칙

## 일반 원칙

- 기본 작성 대상은 Controller 슬라이스 테스트, Service 통합 테스트, Repository 슬라이스 테스트다.
- Service 레이어는 `IntegrationTestSupport`를 상속하여 실제 DB와 연동한 테스트를 작성한다.
- Mock 기반 단위 테스트는 기본 선택지가 아니다.
- 테스트 픽스처는 별도 `fixture` 패키지 또는 `TestFixture` 클래스로 분리한다.
- 하나의 테스트 메서드에서는 하나의 동작만 검증한다.

## 테스트 패키지 구조

```text
src/test/java/com/example/{프로젝트명}/
├── support/
│   ├── ControllerTestSupport.java
│   ├── IntegrationTestSupport.java
│   ├── RepositoryTestSupport.java
│   └── RestDocsSupport.java
├── domain/
│   └── todo/
│       ├── controller/
│       │   ├── TodoControllerTest.java
│       │   └── TodoControllerDocsTest.java
│       ├── service/
│       │   └── TodoServiceTest.java
│       ├── repository/
│       │   └── TodoRepositoryTest.java
│       └── entity/
│           └── TodoTest.java
└── integration/
    └── TodoIntegrationTest.java
```

## Support 클래스 기준

| 클래스 | 용도 |
|---|---|
| `ControllerTestSupport` | Spring Security 포함 Controller 슬라이스 테스트 |
| `IntegrationTestSupport` | Service + Repository 실제 연동 테스트 및 E2E 통합 테스트 |
| `RepositoryTestSupport` | JPA / Querydsl Repository 슬라이스 테스트 |
| `RestDocsSupport` | Spring REST Docs API 문서화 테스트 |

### IntegrationTestSupport

- `@SpringBootTest` + `@ActiveProfiles("test")`
- 부모 클래스에 `@Transactional`을 선언하지 않는다.
- 자식 테스트 클래스에서 `@Transactional`을 명시한다.

### RepositoryTestSupport

- `@DataJpaTest` + `@ActiveProfiles("test")`
- Querydsl 사용 시 `@Import(QuerydslConfig.class)` 추가
- `@Import(JpaAuditingConfig.class)`를 반드시 포함한다.

### ControllerTestSupport

- `@WebMvcTest` 사용
- 공통 `MockBean`은 부모 클래스에서 관리한다.

### RestDocsSupport

- `@WebMvcTest` 없이 `@ExtendWith(RestDocumentationExtension.class)`만 사용한다.
- `MockMvcBuilders.standaloneSetup(initController())`으로 대상 Controller만 등록한다.
- 스니펫 경로는 `{class-name}/{method-name}` 패턴을 사용한다.
- `CharacterEncodingFilter("UTF-8", true)`를 추가한다.
- `ObjectMapper`는 `JavaTimeModule`과 날짜 직렬화 설정을 포함한 프로젝트 표준 설정을 사용한다.
- `LocalDateTime`을 포함한 Java Time 타입은 REST Docs 응답 본문에서 배열이 아닌 ISO-8601 문자열로 직렬화되어야 한다.
- 이를 위해 `RestDocsSupport`의 `ObjectMapper`는 `JavaTimeModule`을 등록하고 `WRITE_DATES_AS_TIMESTAMPS`를 비활성화한 뒤, `MappingJackson2HttpMessageConverter`로 `MockMvc`에 주입한다.

#### RestDocsSupport 직렬화 기준

```java
protected ObjectMapper objectMapper = new ObjectMapper()
        .registerModule(new JavaTimeModule())
        .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);

this.mockMvc = MockMvcBuilders.standaloneSetup(initController())
        .addFilters(new CharacterEncodingFilter("UTF-8", true))
        .setMessageConverters(new MappingJackson2HttpMessageConverter(objectMapper))
        .apply(documentationConfiguration(provider))
        .alwaysDo(document)
        .build();
```

## REST Docs descriptor 기준

- `pathParameters`, `requestFields`, `responseFields`의 모든 descriptor에는 `.type()`을 명시한다.
- 필수 필드는 `.attributes(key("required").value("true"))`
- 선택 필드는 `.optional().attributes(key("required").value("false"))`
- 문서에서 필수/선택 여부가 드러나도록 설명 또는 템플릿에 반영한다.
- `LocalDateTime` 필드는 descriptor에서 `JsonFieldType.STRING`으로 명시하고, 설명에 `ISO-8601` 문자열 형식임을 드러낸다.

## AsciiDoc 문서 구조

- `src/docs/asciidoc/index.adoc`는 문서 진입점으로만 사용하고, 개별 리소스/공통 문서는 별도 `.adoc` 파일로 분리한다.
- 기능별 문서는 `src/docs/asciidoc/sections/` 아래에 생성하고, `index.adoc`에서 `include::...[]`로 조합한다.
- 공통 응답 예시처럼 snippet 의존성이 낮고 고정 포맷이 필요한 내용은 include snippet 대신 하드코딩된 JSON 예시를 사용한다.
- 문서 파일이 비대해지면 `overview`, `common-response`, `todo`처럼 주제별로 분리하는 것을 기본 원칙으로 한다.

### index.adoc 구성 원칙

```adoc
= Todo API Documentation

include::src/docs/asciidoc/sections/overview.adoc[]
include::src/docs/asciidoc/sections/common-response.adoc[]
include::src/docs/asciidoc/sections/todo.adoc[]
```
