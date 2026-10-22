# Backend 개발 규칙
공통 규칙 정본: https://github.com/00700lsm/user-management-contract/blob/main/docs/governance/git-workflow.md

## 기본 흐름
1. Auth=A, User=B, Permission=C (같은 담당자가 각 도메인의 FE+BE 구현)
2. `contract.sha`가 지정한 OpenAPI를 `bash scripts/sync-contract.sh`로 다운로드
3. 도메인별 branch/PR에서 구현; API 변경은 **Contract Repository PR 선행**
4. 각 PR에 요구사항 ID, Frontend 관련 PR, 계약 SHA, 테스트 근거 기재

현재 CI는 승인 계약 다운로드/구문 검사 및 Gradle/H2/Flyway Context Test를 수행합니다. 실제 Backend Controller 요청/응답·인증 통합 테스트는 기능 구현에 맞춰 추가해야 합니다.

명시적인 Backend 개발·검증 Skill: [README.md](./README.md#backend-ai-agent-skills). 항상 적용되는 규칙은 [AGENTS.md](./AGENTS.md)입니다.
