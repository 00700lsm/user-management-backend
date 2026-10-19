# Backend 개발 규칙
공통 규칙 정본: https://github.com/00700lsm/user-management-contract/blob/main/docs/governance/git-workflow.md

## 기본 흐름
1. Auth=A, User=B, Permission=C (같은 담당자가 각 도메인의 FE+BE 구현)
2. `contract.sha`가 지정한 OpenAPI를 `bash scripts/sync-contract.sh`로 다운로드
3. 도메인별 branch/PR에서 구현; API 변경은 **Contract Repository PR 선행**
4. 각 PR에 요구사항 ID, Frontend 관련 PR, 계약 SHA, 테스트 근거 기재

현재 CI는 **계약 버전 다운로드 + OpenAPI 구조 검증**만 수행합니다.
서버가 준비되면 인증/권한 포함 실제 런타임 응답 Contract Test를 추가합니다.
