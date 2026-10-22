# Claude Code — Backend

Follow `AGENTS.md` and the owning Contract's Accepted decisions. This Backend is a modular Spring Boot 4.1.x/JPA/H2/Flyway project; `contract.sha` pins the approved external OpenAPI.

**Call the task-specific Skill** rather than a generic workflow:
- `/implement-api-operation` — implement a specific approved API operation and tests
- `/create-db-migration` — add an authorized Flyway migration and JPA updates
- `/validate-backend-api` — read-only Backend tests, HTTP/contract checks
- `/test-auth-integration` — read-only real JWT/Auth/User/Permission integration checks

The canonical procedures are `.agents/skills/<skill-name>/SKILL.md`, with adapter files under `.claude/skills/`. Never silently update Contract SHA, fabricate JWT credentials or claim missing API tests passed. No unrequested Git changes.
