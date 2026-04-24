# 레이어드 아키텍처

## 패키지 구조 기준

```text
src/main/java/com/example/{프로젝트명}/
├── domain/
│   ├── user/
│   │   ├── controller/
│   │   ├── service/
│   │   ├── repository/
│   │   ├── entity/
│   │   └── dto/
│   │       ├── request/
│   │       └── response/
│   └── order/
│       ├── controller/
│       ├── service/
│       ├── repository/
│       ├── entity/
│       └── dto/
│           ├── request/
│           └── response/
├── global/
│   ├── config/
│   ├── exception/
│   ├── response/
│   └── util/
└── {프로젝트명}Application.java
```

## 규칙

- 도메인 간 직접 의존은 금지한다. 필요 시 Service를 통해 접근한다.
- `global` 패키지는 특정 도메인에 종속되지 않는 공통 코드만 위치한다.
- 설정 파일은 `application.yml` -> `application-{profile}.yml` 구조로 관리한다.

## 레이어 책임

- `controller`: HTTP 요청/응답 처리, 입력 검증, Service 호출
- `service`: 트랜잭션 경계와 비즈니스 로직 담당
- `repository`: 영속성 인터페이스
- `entity`: 상태와 도메인 규칙 보유
- `dto`: 외부 입출력과 레이어 간 전달 모델
