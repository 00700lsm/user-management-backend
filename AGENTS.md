# Backend Repository — Agent rules

This repository owns the Spring Boot Backend implementation. Read the sibling Contract before changing cross-repository behavior. Do not write application code to Workspace root.

## Always-on implementation rules

1. **Contract:** Read `../user-management-contract/docs/aa/README.md`, `docs/aa/decisions.md`, `docs/aa/traceability.md` and `docs/contract-governance.md` as relevant. Resolve and use **the exact OpenAPI commit at this repository's `contract.sha`**, not moving Contract dev HEAD. Use `bash scripts/sync-contract.sh` if pinned contract needs downloading.
2. **Accepted stack:** Java 21 + Spring Boot 4.1.x, Spring Security, JPA/Hibernate, H2 Embedded File (local)/Memory (test), Flyway SQL, Gradle/JUnit; one modular Spring Boot process (DEC-003/011). `docs/aa/tech-stack.md` explains physical structure. This stack is **Accepted**, not a proposal.
3. **Ownership:** A owns Auth/AUTH_CREDENTIAL; B owns User/USER; C owns Permission/ROLE/USER_ROLE. Do not change another module's data or external API shape without a reviewed Contract PR and related owner coordination.
4. **Security:** Follow Accepted DEC-001/002/005/007/009/010: signed JWT, revocation on logout, current DB User status/roles for backend authorization, atomic signup, no production mock bypass. 401 unauthenticated, 403 forbidden, standard `ApiError {code,message}`. Do not trust FE role values.
5. **Database:** Never change an already-applied Flyway migration. New changes require approved versioned migration and matching Entity updates; `spring.jpa.hibernate.ddl-auto=validate`. Do not commit H2 DB files or real passwords/secrets.
6. **Validation:** Differentiate H2 `contextLoads`, actual Controller/MockMvc contract tests, real Auth integration and Frontend E2E. Report commands/exit codes and `PASS/FAIL/NOT_RUN/BLOCKED` accurately.
7. **Git:** Scope commits/PRs to this Backend repository, feature branches from `dev`; no automatic pull, checkout, reset, merge, push or commit without explicit user authorization.

## Explicit callable Backend Skills

- `.agents/skills/implement-api-operation/SKILL.md` — implement a named Backend API
- `.agents/skills/create-db-migration/SKILL.md` — create approved Flyway/JPA schema change
- `.agents/skills/validate-backend-api/SKILL.md` — verify runtime/safe HTTP security behavior (read-only)
- `.agents/skills/test-auth-integration/SKILL.md` — verify real Auth/Role/account status (read-only by default)

Claude Code adapters with matching names live in `.claude/skills/`; don't create redundant all-purpose workflow skills.
