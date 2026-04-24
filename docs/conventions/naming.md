# 네이밍 규칙

## 기본 네이밍

- 클래스: PascalCase
- 메서드 / 변수: camelCase
- 상수: UPPER_SNAKE_CASE
- 패키지: lowercase
- 테스트 클래스: `{대상클래스명}Test`

## 테스트 메서드 네이밍

- 테스트 메서드명은 `{methodUnderTest}_{condition}_{expectedBehavior}` 형식을 따른다.
- 통합 테스트나 플로우 테스트처럼 단일 메서드가 대상이 아닌 경우 `{flowOrFeature}_{condition}_{expectedBehavior}` 형식을 사용한다.

### 구성 요소

| 구성 요소 | 설명 | 작성 기준 |
|---|---|---|
| `{methodUnderTest}` | 테스트하는 메서드명 또는 기능 단위 | 실제 메서드명과 동일하게 camelCase로 작성 |
| `{flowOrFeature}` | 통합 테스트에서 검증하는 플로우 또는 기능 단위 | 단일 메서드명이 아닌 경우에만 사용 |
| `{condition}` | 테스트 조건 또는 입력 상태 | 어떤 상황인지를 명확히 서술 |
| `{expectedBehavior}` | 검증하려는 동작 또는 결과 | `returns~`, `throws~`, `persists~`, `updates~`, `marks~`, `generates~` 등 동사로 시작 |

## 정적 팩토리 메서드

| 메서드명 | 의미 | 사용 예시 |
|---|---|---|
| `from(X x)` | 다른 타입을 변환하여 인스턴스 생성 | `TodoResponse.from(todo)` |
| `of(A a, B b, ...)` | 여러 값을 묶어 인스턴스 생성 | `ErrorBody.of(code, message)` |
| `valueOf(X x)` | 의미 변환에 가까운 생성 | `BigInteger.valueOf(100L)` |
| `getInstance()` / `instance()` | 싱글톤 또는 캐시 인스턴스 반환 | `Calendar.getInstance()` |
| `create()` / `newInstance()` | 항상 새 인스턴스 생성 | `Array.newInstance(type, length)` |
| `getType()` / `newType()` | 반환 클래스가 다른 팩토리 | `Files.newBufferedReader(path)` |

## 프로젝트 적용 기준

- Entity -> DTO 변환: `from()`
- 여러 파라미터 조합 생성: `of()`
- 변환 소스가 여러 타입일 때만 `from()` 오버로드 허용
