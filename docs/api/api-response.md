# API 응답 구조

## 공통 응답 래퍼

모든 API 응답은 `ApiResponse<T>` 구조를 따른다.

- `data`와 `error`는 `@JsonInclude(NON_NULL)`로 필요한 필드만 직렬화한다.
- 에러 응답은 `ErrorCode` 기반의 `fail()` 메서드로 수렴한다.
- 성공 응답 팩토리 메서드에서는 `ApiResponse.<T>` 타입 파라미터를 명시한다.
- `LocalDateTime`을 포함한 Java Time 타입은 API 응답에서 배열이 아닌 ISO-8601 문자열로 직렬화한다.
- 이를 위해 프로젝트 전역 Jackson 설정에서 `WRITE_DATES_AS_TIMESTAMPS`를 비활성화한다.

## 날짜 / 시간 응답 규칙

- `LocalDateTime`, `LocalDate`, `LocalTime` 등 Java Time 타입은 문자열로 응답한다.
- 기본 포맷은 Jackson의 ISO-8601 표준 직렬화 형식을 따른다.
- REST Docs descriptor에서도 동일하게 `JsonFieldType.STRING`으로 문서화한다.

예시:

```json
{
  "createDateTime": "2026-04-24T15:12:00",
  "modifiedDateTime": "2026-04-24T15:30:00"
}
```

## 성공 응답 예시

```json
{
  "status": 200,
  "success": true,
  "data": {
    "id": 1,
    "name": "홍길동",
    "email": "hong@example.com"
  }
}
```

## 실패 응답 예시

```json
{
  "status": 422,
  "success": false,
  "error": {
    "code": "LENGTH_EXCEEDED",
    "message": "글자 수 제한을 초과하였습니다. [최대 100자]"
  }
}
```

## HTTP 상태 코드 규칙

| 상황 | 상태 코드 |
|---|---|
| 조회 / 생성 / 수정 / 삭제 성공 | 200 OK |
| 잘못된 요청 (유효성 검사 실패) | 400 Bad Request |
| 인증 실패 | 401 Unauthorized |
| 권한 없음 | 403 Forbidden |
| 리소스 없음 | 404 Not Found |
| 데이터 중복 및 상태 충돌 | 409 Conflict |
| 비즈니스 로직 / 정책 위반 | 422 Unprocessable Entity |
| 서버 내부 오류 | 500 Internal Server Error |

## 에러 코드 / 예외 처리

- 에러 코드는 `ErrorCode` Enum으로 관리한다.
- 예외 응답은 `CustomException` + `GlobalExceptionHandler` 조합으로 처리한다.
- 동적 메시지가 필요한 경우 `ErrorCode#formatMessage(...)`와 `CustomException(ErrorCode, Object...)`를 사용한다.
- 인증 실패 응답은 보안 컴포넌트가 직접 제어한다.
- JWT 토큰이 존재하는 경우의 검증 실패는 `JwtAuthenticationFilter`가 `TOKEN_INVALID`, `TOKEN_EXPIRED`, `TOKEN_TYPE_INVALID`, `TOKEN_BLACKLISTED` 중 하나로 반환한다.
- 로그아웃처럼 인증 이후 서비스 레이어에서 토큰 소유자 정합성을 검증하는 경우 `TOKEN_OWNER_MISMATCH`를 반환할 수 있다.
- Authorization 헤더가 없는 경우는 `JwtAuthenticationEntryPoint`가 `TOKEN_MISSING`으로 반환한다.
