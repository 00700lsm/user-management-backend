---
name: backend-workflow
description: Guide backend project setup, domain implementation, API contract validation, test-only Auth fixtures and full integration for User Management tasks.
---

# Backend Workflow — setup to integration

Use on an explicit Backend setup, development or test task. Do not claim runnable server, build or test scripts already exist.

## 0. Resolve source of truth
1. Find own `user-management-backend` repo and sibling `../user-management-contract/`. If started from Workspace root, use `repos/user-management-contract/`. If absent, stop and request Workspace init.
2. Read Contract `docs/aa/README.md`, `docs/architecture.md`, `docs/db-design.md`, `docs/requirements.md`, `docs/aa/decisions.md`, `docs/aa/auth-design.md`, `docs/aa/integration-test-plan.md`, `docs/governance/git-workflow.md`.
3. Read own `docs/aa/README.md`, `docs/aa/service-boundaries.md`, `docs/aa/data-handoff.md`, `CONTRIBUTING.md`, `contract.sha`. Compare Frontend's contract.sha and identify the approved baseline.
4. For external API shape use **openapi.yaml at contract.sha**, not latest dev head. `bash scripts/sync-contract.sh` downloads the pinned file.

## Recommended stack proposal — check before setup
- **Read own `docs/aa/tech-stack.md` and sibling Contract `docs/aa/tech-stack-proposal.md`** before project setup or infrastructure-related implementation.
- Recommended (not yet approved): Java 21 + Spring Boot 4.1.x + Spring Web MVC/Security + Spring Data JPA (Hibernate ORM) + H2 file local / in-memory tests + Flyway + Gradle + JUnit/MockMvc; modular single application.
- Use local isolated H2 database files rather than trying to share one DB file across developer machines. Version schema with Flyway, not committed DB binaries. Check DEC-003/011 for approval.

## 1. Project initialization — only when approved
- Inspect existing `auth/`, `user/`, `permission/` folders; they are logical boundary placeholders, not runnable modules.
- Before selecting a build system, framework, DBMS or physical service split, check Contract `docs/aa/decisions.md` and ask AA/PL for unresolved decisions.
- With approved stack, prepare build/runtime/test configs, migrations and development DB. Never overwrite current code, save secrets or fabricate CI success.
- Keep domain data ownership: Auth=AUTH_CREDENTIAL, User=USER, Permission=ROLE/USER_ROLE.

## 2. Implement domain operations
- A=Auth Login/Logout/Password; B=User Signup/Profile/Admin CRUD/Status; C=Permission Role/Assignment.
- Trace REQ → operationId/request/response/status → logical entity/owner → tests. Do not directly write another domain's data.
- For cross-domain Signup and token Principal/Role handling, use Contract Auth design and DEC-002/003/009. If unresolved, present a proposal, do not silently invent internal APIs.
- Implement 401 unauthenticated, 403 unauthorized, standard ApiError and admin access per approved contract. Report missing declared error responses for Contract PR.

## 3. Independently test protected APIs
- Use common test-only Guest/USER/ADMIN/expired/inactive/withdrawn scenarios from Contract `docs/aa/mock-fixtures.md`.
- Coordinate a test-auth fixture with A and actual verification mechanism; use isolated DB and test configuration. **No hardcoded production auth bypass or committed signing secrets.**
- Test invalid/missing token, unauthorized Role, user status, malformed request, duplicate data, rollback consistency. Mock/fixtures do not replace real Auth integration.
- Validate **runtime responses** against the pinned OpenAPI. A schema syntax check alone is not a contract test.

## 4. Connect real Auth / E2E
- Take A's real login token through the agreed common authentication verification path in User/Permission APIs.
- Test integration gates G2→G4 and TC-INT cases from Contract `docs/aa/integration-test-plan.md`, especially signup→login, role changes, 401/403, status and logout.
- Record FE SHA, BE SHA, Contract SHA, test data profile, execution commands and results. Test/Mock code must be unavailable in production mode.

## 5. Git / delivery
- Own Repo feature branch from `dev`; PR into `dev`. Separate FE/BE PRs, links to each other. No work commits in Workspace root.
- Contract-changing behavior requires approved Contract PR before implementation and coordinated contract.sha change.
- Do not pull, change branches, commit, push or merge without explicit task. Report completed and missing checks truthfully.
