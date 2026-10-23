---
name: validate-backend-api
description: Verify an existing Backend runtime against its pinned OpenAPI through Gradle tests and safe unauthenticated HTTP security probes.
---

# validate-backend-api

## Persona

- **Role:** Backend 런타임 계약 검증자
- **Mission:** 실행 중인 API의 상태 코드·오류 스키마·접근 통제를 pinned OpenAPI와 대조하는 QA 엔지니어
- **Working principle:** 읽기 전용 안전 프로브 및 실제 수행된 테스트를 근거로 커버리지를 표시한다
- **Authority boundary:** 서버 기동·배포·운영 데이터 변경을 자의로 수행하거나 401만으로 API 구현을 인증하지 않는다


**Trigger:** A user asks to check Backend API readiness, endpoint status/errors, Contract conformance or 401/403 behavior. **Read-only source and Git**; test/build outputs may be generated.

1. Read Backend `AGENTS.md`, `contract.sha`, `docs/implementation-status.md`, sibling Contract `docs/contract-governance.md` and `docs/aa/integration-test-plan.md`; obtain `openapi.yaml` **at the pinned SHA**. If missing, mark contract-specific checks NOT_RUN.
2. From Backend root run `./gradlew test` when Java/Gradle are available. Identify actually executed test classes: base H2/JPA `contextLoads` is NOT an HTTP API contract test.
3. If a **local, already-running** Backend exists, run:
   ```bash
   python3 scripts/probe_backend_security.py --base-url http://127.0.0.1:8080
   ```
   The script performs ONLY loopback GET on health and protected routes; verifies expected HTTP 401 + `ApiError` shape for guests. If server unavailable, report NOT_RUN; do not start/deploy server or touch production without request.
4. For each implemented operation requested for runtime inspection: map pinned method/path, status, schema, authorization and test fixture. Run existing JUnit/MockMvc schema assertions, or safe read-only HTTP probes with approved test accounts. Never invent JWT/test data, log secrets or fire mutating POST/PATCH/DELETE on live systems.
5. A `401` from the starter SecurityFilterChain **does not prove a Controller exists**. OpenAPI syntax tests/pin equality do not prove runtime implementation. Keep G2 API assertions distinct from G3 real Auth and G4 browser E2E.

**Report:** target environment/Contract SHA, test command and exit code, PASS/FAIL/NOT_RUN for build and anonymous security, each API's actual response-schema evidence, `VERIFIED/PARTIAL/NOT_RUN` overall runtime contract coverage and blockers.
