# User Management Backend (PoC)

사용자 관리의 Auth / User / Permission 세 도메인 담당자가 **자기 영역의 Backend와 Frontend를 함께 개발**하는 PoC용 Backend 저장소입니다.

- 공통 계약: https://github.com/00700lsm/user-management-contract/blob/main/openapi.yaml
- AA 설계 기준: https://github.com/00700lsm/user-management-contract/tree/main/docs
- Frontend: https://github.com/00700lsm/user-management-frontend

## 담당
| 개발자 | Backend | 연결되는 같은 담당자 Frontend |
|---|---|---|
| A | Auth | 로그인/비밀번호 |
| B | User | 가입/프로필/사용자 관리 |
| C | Permission | Role/권한 화면 |

## 모듈 경계
- `auth/`: 로그인, 인증, 비밀번호/토큰 관련 로직
- `user/`: 회원가입, 프로필, 사용자 관리, 상태
- `permission/`: Role/사용자 권한
- 타 모듈 데이터 직접 수정 금지. PoC에서는 **Spring Boot 단일 앱(논리 모듈 3개)**으로 확정.

**계약 우선:** 구현 시작 시 Contract Git SHA를 기록합니다. API 스키마/엔드포인트/상태코드를 변경하려면 Contract 저장소 PR을 먼저 반영합니다.

이 저장소에는 **Java 21 + Spring Boot 4.1.1 + JPA/H2/Flyway 초기 실행 골격**이 추가됐습니다. 그러나 실제 도메인 API/JWT 구현은 미완료이며, CI Gradle/H2 컨텍스트 테스트는 통과했으며 로컬 bootRun과 실제 업무 API는 별도 검증이 필요합니다.

## AA 설계 인계

[Backend AA 인계서](docs/aa/README.md)에 서비스 책임·DB 소유권·개발 착수/검증 체크리스트가 있습니다. 공통 정본(OpenAPI/요구사항/ERD)은 Contract 저장소를 참조합니다.

## Backend Agent Skill

[단계별 Backend 개발 스킬](.agents/skills/backend-workflow/SKILL.md)과 [Claude Code Adapter](.claude/skills/backend-workflow/SKILL.md)를 참고하세요. 공통 스킬 작성 규칙은 [Contract 가이드](https://github.com/00700lsm/user-management-contract/blob/dev/docs/governance/agent-skills-guide.md)가 정본입니다. Sibling Contract는 `../user-management-contract`, API 버전은 `contract.sha`로 고정합니다.

## Backend 기술 스택 (Accepted)

[Backend 기술 스택·H2 로컬 DB 가이드](docs/aa/tech-stack.md)에서 Java 21 / Spring Boot 4.1.x / JPA(Hibernate) / **H2 Embedded File** / Flyway / Gradle을 권장합니다. Gradle·Entity·Flyway V1·H2 Profile 기반이 추가됐으며 GitHub Actions의 Gradle/H2 테스트가 통과했습니다. [구현 상태/실행 방법](docs/implementation-status.md)을 확인하세요.

## 시작하기

```bash
cd user-management-backend
mkdir -p .local-data
./gradlew test
./gradlew bootRun
# 별도 터미널에서 curl http://localhost:8080/actuator/health
```

인증이 완성되기 전에는 보호 API를 401/403으로 차단합니다. 앱 기동과 API 기능 구현은 별개입니다.

## 로컬 실행 및 확인

**요구 환경:** Java 21, Bash, curl, Gradle 의존성 다운로드용 인터넷. H2는 내장형으로 별도 설치하지 않습니다. Workspace에서 실행한다면 `cd repos/user-management-backend`로 이동합니다.

```bash
mkdir -p .local-data
./gradlew test
./gradlew bootRun
```

별도 터미널에서 기동 상태를 확인합니다.

```bash
curl http://localhost:8080/actuator/health
```

- 기본 포트: **8080**. `local` Profile에서 H2 파일 DB(`.local-data/user-management.mv.db`)를 사용합니다. 테스트는 H2 메모리 DB를 사용하며 Flyway V1 마이그레이션이 실행됩니다.
- `./gradlew test`는 GitHub Actions의 Java 21 환경에서 통과했습니다. **로컬 `bootRun` 기동 자체는 아직 별도 확인 전**입니다.
- 현재 JWT 발급/검증과 Auth·User·Permission 업무 API는 미구현입니다. `/actuator/health` 확인은 로그인·권한·API 기능 검증을 의미하지 않습니다.
- Frontend는 별도 저장소에서 실행하며 기본 주소는 `http://localhost:5173`입니다.
