# 코딩 원칙

## 일반 원칙

- 가독성을 최우선으로 한다.
- 메서드는 단일 책임을 가지며 20줄 이내를 권장한다.
- 매직 넘버와 매직 스트링은 상수 또는 Enum으로 추출한다.
- `null` 반환을 지양하고 `Optional`을 적절히 활용한다.
- 생성자 주입을 원칙으로 하며 필드 주입은 사용하지 않는다.
- Lombok은 `@Data` 대신 필요한 어노테이션만 선택적으로 사용한다.

## Controller

- 반환 타입은 반드시 `ApiResponse<T>`를 사용한다.
- `ResponseEntity`로 감싸는 것은 허용하지 않는다.
- 헤더 제어가 필요한 경우에도 반환 타입은 유지하고 `HttpServletResponse`를 직접 사용한다.
- `@RequestBody` Request DTO는 Service에 직접 전달하지 않고 ServiceRequest로 변환한다.
- `@PathVariable`, `@RequestParam` 단일 파라미터는 직접 전달한다.
- 여러 `@RequestParam`은 ServiceRequest로 묶는다.

## DTO / Request

- DTO와 Entity는 반드시 분리한다.
- Entity -> DTO 변환은 DTO 내부의 `from()` 정적 팩토리 메서드를 사용한다.
- Request DTO는 등록/수정 등 용도별로 분리한다.
- Request -> ServiceRequest 변환은 ServiceRequest 내부의 `from()`을 사용한다.
- ServiceRequest는 `dto/request/` 패키지에 위치시킨다.

## Service

- 트랜잭션은 Service 레이어에서 관리한다.
- 조회 메서드에는 `@Transactional(readOnly = true)`를 적용한다.

## JPA / 영속성

- 연관관계 편의 메서드는 연관관계의 주인 쪽에 작성한다.
- N+1 문제를 주의하고 필요 시 `fetch join` 또는 `@EntityGraph`를 사용한다.
