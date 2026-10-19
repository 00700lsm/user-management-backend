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
- 타 모듈 데이터 직접 수정 금지. 실제 물리 서비스 분할은 추후 결정.

**계약 우선:** 구현 시작 시 Contract Git SHA를 기록합니다. API 스키마/엔드포인트/상태코드를 변경하려면 Contract 저장소 PR을 먼저 반영합니다.

이 저장소는 **초기 구조/문서만 제공**합니다. 기술 스택 및 실행 가능한 서버는 아직 결정/구현되지 않았습니다.

## AA 설계 인계

[Backend AA 인계서](docs/aa/README.md)에 서비스 책임·DB 소유권·개발 착수/검증 체크리스트가 있습니다. 공통 정본(OpenAPI/요구사항/ERD)은 Contract 저장소를 참조합니다.

## Backend Agent Skill

[단계별 Backend 개발 스킬](.agents/skills/backend-workflow/SKILL.md)과 [Claude Code Adapter](.claude/skills/backend-workflow/SKILL.md)를 참고하세요. 공통 스킬 작성 규칙은 [Contract 가이드](https://github.com/00700lsm/user-management-contract/blob/dev/docs/governance/agent-skills-guide.md)가 정본입니다. Sibling Contract는 `../user-management-contract`, API 버전은 `contract.sha`로 고정합니다.

## 추천 Backend 기술 스택 (AA 승인 전)

[Backend 기술 스택·H2 로컬 DB 가이드](docs/aa/tech-stack.md)에서 Java 21 / Spring Boot 4.1.x / JPA(Hibernate) / **H2 Embedded File** / Flyway / Gradle을 권장합니다. 현재 실제 빌드·DB 구현은 아직 하지 않았습니다. AA 결정 DEC-003/011을 확인하세요.
