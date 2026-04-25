# 예외 처리 규칙

## 기본 원칙

- 예외는 `CustomException(ErrorCode)` 하나로 수렴시킨다.
- 도메인/에러별 개별 예외 클래스는 정의하지 않는다.

## 애플리케이션 내부 계층 규칙

- Entity, Service, Repository, Controller에서는 `IllegalArgumentException`, `IllegalStateException`, `NullPointerException` 등 표준 예외를 직접 던지지 않는다.
- 예외가 필요한 경우 반드시 `CustomException(ErrorCode)`를 사용한다.

## 프레임워크 SPI / 어댑터 예외

- Spring Security, JPA, Bean Validation 등 프레임워크의 SPI/어댑터 구현체는 프레임워크가 기대하는 예외 계약을 우선한다.
- 예: `UserDetailsService#loadUserByUsername`는 `UsernameNotFoundException` 유지
- 이런 예외는 프레임워크 경계에서만 사용하고, 애플리케이션 서비스/도메인 계층으로 전파하지 않도록 어댑터 또는 Provider에서 `CustomException(ErrorCode)`로 변환한다.

## 인증 / 보안 예외 응답

- JWT 인증 필터는 Authorization 헤더에 Bearer 토큰이 존재하는 경우만 동작한다.
- 토큰이 존재할 때의 검증 실패는 `JwtAuthenticationFilter`가 직접 응답한다.
- 이 경우 `TOKEN_INVALID`, `TOKEN_EXPIRED`, `TOKEN_TYPE_INVALID`, `TOKEN_BLACKLISTED` 중 하나로 반환한다.
- `JwtAuthenticationEntryPoint`는 토큰이 없거나 인증 자체가 시작되지 못한 경우만 처리한다.
- Authorization 헤더가 없으면 `TOKEN_MISSING`, 헤더 형식이 Bearer 규약에 맞지 않으면 `TOKEN_INVALID`를 반환한다.

## 적용 이유

- 표준 예외는 일관된 API 에러 응답을 만들기 어렵다.
- `CustomException(ErrorCode)`는 HTTP 상태 코드, 에러 코드, 메시지를 일관되게 관리한다.
- SPI 구현체는 프레임워크 계약을 지켜야 확장 포인트와 테스트 유틸과 자연스럽게 호환된다.
