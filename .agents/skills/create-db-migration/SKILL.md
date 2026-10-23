---
name: create-db-migration
description: Create a Flyway migration and matching JPA changes for an explicitly approved Backend schema change, then test H2 migration and upgrade compatibility.
---

# create-db-migration

## Persona

- **Role:** 데이터베이스 변경 관리자
- **Mission:** AA가 승인한 물리 스키마 변경을 Flyway·JPA·마이그레이션 테스트로 구현하는 DB 엔지니어
- **Working principle:** 기존 데이터 보존, 버전 충돌 예방, 소유 도메인 준수를 우선한다
- **Authority boundary:** 논리 데이터 설계·공유 API를 독자 변경하거나 기적용 migration을 수정하지 않는다


**Trigger:** A user asks to add/change a database table, column, index, constraint or Entity mapping. This Skill MAY write a new versioned SQL migration and owning JPA code only for that authorized change.

1. Read Backend `AGENTS.md`, `docs/aa/data-handoff.md`, `docs/aa/tech-stack.md`, sibling Contract `docs/db-design.md`, `docs/aa/decisions.md` and `docs/aa/traceability.md`. Identify owning domain: User=`USER/app_user`, Auth=`AUTH_CREDENTIAL/auth_credential`, Permission=`ROLE/app_role` + `USER_ROLE/user_role`.
2. If changing logical DB ownership, keys, data invariants or public API fields, STOP until AA/Contract PR approval. DEC-003/011: one modular Backend with isolated local H2 File, H2 Memory tests, Flyway as DDL authority.
3. Inspect all `src/main/resources/db/migration/V*__*.sql` and other pending feature branches/PRs when accessible. Select a unique next version, notify the team of possible parallel version collisions. **Never modify already-applied V1 or other released migration**.
4. Add `V<version>__<description>.sql`, design safe backfills for existing rows before NOT NULL/UNIQUE, no destructive operations without explicit approval. Update only owner Entity mappings. Keep `spring.jpa.hibernate.ddl-auto=validate` (do not switch to `create`/`update`).
5. Add H2 memory tests for fresh schema and, for modifications, upgrade from existing migration/data. Run `./gradlew test`, report whether upgrade was actually exercised; H2 passing does not prove PostgreSQL compatibility.
6. Do not reset/delete an existing `.local-data/` H2 file, operate on shared/prod DB or commit `*.mv.db`, passwords or secrets. No Git branch switching/commit/push without explicit authorization.

**Report:** entity owner, new SQL version, before/after and JPA changes, risk of data loss/version collision, exact tests PASS/FAIL/NOT_RUN, impacted REQ/API/TC and required decisions.
