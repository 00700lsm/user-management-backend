# Backend Skeleton 상태

## 생성됨
- Java 21, Spring Boot 4.1.1 + WebMVC/Security/JPA/Actuator
- H2 File(local) / H2 Memory(test), Flyway V1 데이터 구조 및 USER/ADMIN 기본 Role
- Auth/User/Permission Entity 골격, 자동 DDL 생성 금지(validate)
- **기본 거부** SecurityFilterChain: 건강 점검 공개, 나머지 보호 API는 JWT 구현 전 401
- JUnit contextLoads 템플릿, Gradle 9.3.1 wrapper 부트스트랩 텍스트 스크립트(공식 배포 SHA256 검증)

## 아직 구현되지 않음
- 로그인/로그아웃/JWT 서명·만료·차단 목록/역할·계정상태 Principal, 비밀번호 해시
- 회원가입, User CRUD, Permission API, 등록 트랜잭션, 계약 실행 검증, E2E
- JPA Entity는 데이터 모델 골격으로 JPA/서버 기동 검증 전입니다.
- 테스트용 계정/고정 관리자 토큰을 서비스에 심지 않았습니다.

## 실행 (Java 21 + Bash + curl, 외부 Maven Central/Gradle 접근 가능 필요)

```bash
mkdir -p .local-data
./gradlew test
./gradlew bootRun
curl http://localhost:8080/actuator/health
```

첫 `./gradlew` 실행은 Gradle 9.3.1 Wrapper JAR을 공식 배포 URL에서 다운로드해 SHA-256을 확인합니다. **Java 21을 설치해야** 하며 Gradle 배포 파일과 의존성을 다운로드할 수 있어야 합니다. `.local-data/`는 Git 무시 대상.

정식 인증·가입 API는 미구현이므로 404/401을 예상해야 하며 제품 기능 검증으로 간주하지 않습니다.


## GitHub Actions 검증 결과 (2026-10-20)

- `main`과 `dev`의 **Gradle `test` 통과**: Java 21, Spring Boot 4.1.1, Flyway V1 마이그레이션, H2 in-memory, JPA schema validate, Application Context smoke.
- [Main CI](https://github.com/00700lsm/user-management-backend/actions/runs/39347117079) / [Dev CI](https://github.com/00700lsm/user-management-backend/actions/runs/39347081748)
- `bootRun` 로컬 기동, JWT/API Runtime Contract, 보호 API/권한 및 FE 연동은 **검증 전**.
