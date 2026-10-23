# Backend Skeleton 상태

## 생성됨
- Java 21, Spring Boot 4.1.1 + WebMVC/Security/JPA/Actuator
- H2 File(local) / H2 Memory(test), Flyway V1 데이터 구조 및 USER/ADMIN 기본 Role
- Auth/User/Permission Entity 골격, 자동 DDL 생성 금지(validate)
- **기본 거부** SecurityFilterChain: 건강 점검 공개, 나머지 보호 API는 JWT 구현 전 401
- JUnit contextLoads 템플릿, Gradle 9.3.1 wrapper 부트스트랩 텍스트 스크립트(공식 배포 SHA256 검증)

## 아직 구현되지 않음
- (구현됨: 아래 "Auth 구현" 참고)
- 회원가입, User CRUD, Permission API, 등록 트랜잭션, 계약 실행 검증, E2E
- JPA Entity는 데이터 모델 골격으로 JPA/서버 기동 검증 전입니다.
- 테스트용 계정/고정 관리자 토큰을 서비스에 심지 않았습니다.

## 실행 (Java 21 + Bash + curl, 외부 Maven Central/Gradle 접근 가능 필요)

```bash
mkdir -p .local-data
./gradlew test
export JWT_SECRET="<32바이트 이상 임의 문자열>"  # 필수, 커밋 금지
./gradlew bootRun
curl http://localhost:8080/actuator/health
```

첫 `./gradlew` 실행은 Gradle 9.3.1 Wrapper JAR을 공식 배포 URL에서 다운로드해 SHA-256을 확인합니다. **Java 21을 설치해야** 하며 Gradle 배포 파일과 의존성을 다운로드할 수 있어야 합니다. `.local-data/`는 Git 무시 대상.

정식 인증·가입 API는 미구현이므로 404/401을 예상해야 하며 제품 기능 검증으로 간주하지 않습니다.


## GitHub Actions 검증 결과 (2026-10-20)

- `main`과 `dev`의 **Gradle `test` 통과**: Java 21, Spring Boot 4.1.1, Flyway V1 마이그레이션, H2 in-memory, JPA schema validate, Application Context smoke.
- [Main CI](https://github.com/00700lsm/user-management-backend/actions/runs/39347117079) / [Dev CI](https://github.com/00700lsm/user-management-backend/actions/runs/39347081748)
- `bootRun` 로컬 기동, JWT/API Runtime Contract, 보호 API/권한 및 FE 연동은 **검증 전**.

## Auth 구현 (feat/auth-jwt)
- 구현: `POST /api/auth/login`, `/logout`, `/password/change`, HS256 JWT(15분, iss=`user-management`), 요청별 DB 상태/Role 재조회 필터, 메모리 `jti` 폐기 목록(재시작 시 소실), BCrypt(10) 및 비밀번호 정책, `CredentialRegistrar`, `ApiError` 통일.
- 로컬 실행에 환경변수 `JWT_SECRET`(UTF-8 32바이트 이상)이 필수이며 없으면 기동 실패합니다.
- `UserAccountLookup`/`RoleLookup`은 A 초안(읽기 전용) — B/C 합의 필요.
