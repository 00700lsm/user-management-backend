---
name: implement-api-operation
description: Implement a specifically requested Backend OpenAPI operation with controller/service/repository behavior and tests under the approved pinned Contract.
---

# implement-api-operation

**Trigger:** A user explicitly asks to implement/fix a named HTTP method/path, operationId or REQ ID. This Skill MAY change Backend source and tests within the requested scope; it never independently commits, pushes, merges or switches branches.

1. Resolve the Backend Git root; find sibling `../user-management-contract` (from Workspace `repos/user-management-contract`). Read Backend `AGENTS.md`, `docs/aa/service-boundaries.md`, `docs/aa/data-handoff.md` and Contract `docs/aa/decisions.md`, `docs/requirements.md`, `docs/aa/traceability.md` and `docs/contract-governance.md`.
2. Read `contract.sha`. Obtain `openapi.yaml` **at that commit**, not current Contract `dev`: `git -C ../user-management-contract show "$(cat contract.sha):openapi.yaml"` (if Git object exists), else `bash scripts/sync-contract.sh` to the ignored cache (network required). If no approved operation exists, STOP and propose a Contract PR.
3. Match REQ→operationId/path/verb→request/response/error status→data owner→planned TC; identify Auth(A), User(B) or Permission(C) and dependent Frontend screen. Approved architecture DEC-003 is one Spring Boot app with 3 logical modules.
4. Inspect `src/main/java/dev/usermanagement`, Entity, SecurityConfig and Flyway migrations. Implement only the owning Controller/Service/Repository/DTO plus relevant shared interfaces. Do not directly modify another module's owned data or invent external fields.
5. Use accepted DEC-001/002/005/007/009/010 for JWT, current DB roles/status, password hashing, atomic signup, test/prod isolation. Enforce 401/403 and `ApiError {code,message}`; Frontend-provided role claims are not authorization.
6. Add meaningful JUnit/MockMvc cases for success, invalid inputs, 401/403, errors/duplicate/rollback relevant to the operation. `./gradlew test`; if runtime environment available, verify status + JSON against pinned OpenAPI. `contextLoads` alone does not prove endpoint behavior.
7. If physical schema must change, coordinate `create-db-migration`; if shared contract changes, stop for Contract PR. Preserve unrelated edits and never report unrun tests as passing.

**Report:** target REQ/HTTP/operationId, Contract SHA, files changed, actual tested endpoints/test classes, commands+exit codes PASS/FAIL/NOT_RUN, missing/blocked dependencies and FE cross-team impacts.
