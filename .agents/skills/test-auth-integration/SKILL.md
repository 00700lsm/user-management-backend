---
name: test-auth-integration
description: Test real JWT issuance, account status, role authorization and Auth/User/Permission cross-module flows with isolated H2 fixtures.
---

# test-auth-integration

## Persona

- **Role:** 인증·인가 보안 검증자
- **Mission:** 실제 JWT·상태·역할·트랜잭션 보안 시나리오를 격리 환경에서 검증하는 테스트 엔지니어
- **Working principle:** 서명된 토큰과 현재 DB 권한으로 TC별 재현 가능한 증거를 수집한다
- **Authority boundary:** Mock 토큰을 실제 인증으로 간주하거나 테스트용 우회 장치를 운영 코드에 넣지 않는다


**Trigger:** A user asks to verify authentication integration, JWT/Role permissions, signup atomicity, logout revocation or security test readiness. Default is read-only verification; test code creation only if explicitly requested.

1. Read Backend `AGENTS.md`, `contract.sha`; sibling Contract `docs/aa/auth-design.md`, `docs/aa/mock-fixtures.md`, `docs/aa/integration-test-plan.md`, `docs/aa/decisions.md` and the pinned OpenAPI.
2. Inspect actual implementation of JWT signature/expiry/`jti` revocation, User status and current DB Role lookup, Auth Credential, signup transaction, test Profile, fixtures. The initial Backend has only a fail-closed SecurityFilterChain and Entity skeleton. If still missing, label **NOT_IMPLEMENTED**; do not call Mock success real integration.
3. Run `./gradlew test` and report which integration/security test classes actually ran. Use test-only H2 Memory, no production keys or accounts, no Mock token mistaken for signed JWT. If tests/fixtures absent, mark cases BLOCKED/NOT_RUN.
4. Verify cases from Contract `TC-INT-001...012`: guest 401; missing/expired/tampered/revoked JWT 401; ACTIVE USER profile; USER admin API 403; ADMIN permitted; role change reflected on next protected API call via DB check (DEC-009); INACTIVE/WITHDRAWN denied (DEC-005); atomic signup/rollback (DEC-002); logout `jti` revocation until expiry (DEC-001); production mock/test secret disabled (DEC-010).
5. For real signed tokens, exercise the same Backend verification path as production in isolated tests. Check HTTP schema against pinned Contract. Do not create test backdoors. G3 requires real Auth/Backend integration; G4 requires actual Frontend browser E2E, not Backend tests alone.

**Report:** BE/FE/Contract SHA when verifiable, implementation FOUND/NOT_IMPLEMENTED, each TC PASS/FAIL/BLOCKED/NOT_RUN, exact command/test class/log, verified gate G2/G3/G4 (never inferred), defects/owner. No Git or deployment changes without separate authorization.
