---
name: prepare-contract-pr
description: Before opening a Backend feature pull request against dev, compare the pinned contract.sha with the latest Contract/dev OpenAPI, assess compatibility and affected operations, update the pin only after verified safe, and run repository tests. Invoke explicitly at PR submission time.
---

# Prepare Contract PR — Backend

## Persona

- **Role:** Backend 기능 PR 계약 호환성 사전 점검자
- **Mission:** PR 직전 최신 Contract/dev에 대한 변경 영향·pin·실제 테스트 증거를 확보한다
- **Working principle:** 차이의 존재와 호환성 판정을 구분하고, 기존 개발 작업을 보존한다
- **Authority boundary:** Contract를 개정하거나 타인의 브랜치·PR을 수정하지 않으며 승인·병합하지 않는다

## Invocation and preconditions

Call explicitly immediately before creating a Backend feature PR to `dev`. Read `AGENTS.md`, `contract.sha`, `scripts/sync-contract.sh` and the Contract repo's `docs/governance/git-workflow.md` and `docs/contract-governance.md`. Work inside the current feature branch; **never switch branches**. Do not apply to an existing PR automatically.

1. Check `git status --short` and `git branch --show-current`. Require a clean working tree before modifying `contract.sha`; otherwise report `BLOCKED` and request that the developer commit or safely preserve their work first. Never stash, reset, clean, force-push or discard changes.
2. Read the current pinned SHA (exactly 40 lowercase hexadecimal characters). Resolve **Contract/dev HEAD** with an authenticated GitHub read or `git ls-remote https://github.com/00700lsm/user-management-contract.git refs/heads/dev`. Record both full SHAs and retrieval time. If unavailable or invalid, stop; do not silently treat an unreachable Contract repo as compatible.
3. Retrieve `openapi.yaml` **at both exact commits**, without mutating the Contract checkout. Inspect differences in changed path/method, request parameters, required fields, types/enums, response/status codes, auth/security and errors. Map changed operations to this PR's REQ/UI/TC and consuming FE/BE code. **A different SHA is not automatically a breaking change.**
4. Classify `NON_BREAKING`, `BREAKING_CANDIDATE`, or `NEEDS_REVIEW`. For candidate breaking or uncertain consumer impact, stop without pin update and request AA/affected owner review and an approved migration strategy. A machine schema diff alone never proves runtime compatibility.
5. If the latest contract is unchanged or impact is verified `NON_BREAKING`, update **only this repository's `contract.sha`** to the resolved Contract/dev SHA as part of this explicit pre-PR task. The developer should inspect `git diff -- contract.sha` before committing. Do not change another repo's pin.
6. `bash scripts/sync-contract.sh`와 `./gradlew test`를 실행한다. 별도로 기존 Backend contract CI 기준을 참조한다. Respect existing dependency/tool availability; do not install packages without permission. Capture exact command, exit status and passed/failed tests. If any required verification fails or cannot run, report `FAIL` or `NOT_RUN` and **do not claim PR readiness**. Generated files are not automatically committed.
7. Resolve Contract/dev HEAD again before declaring readiness. If it moved during verification, mark `STALE` and review the intervening changes; do not claim the new HEAD passed.
8. Report feature branch, old/new Contract SHA, changed API operations, impact on A/B/C, classification, test evidence and a proposed PR-body summary. Leave committing, pushing, PR creation, approval and merge to a separately requested action.

## Review policy

- Contract changes are coordinated by AA. Unaffected developers may continue working; no global immediate pin change is required.
- This skill updates the pin only on the explicitly requested pre-PR path after impact checks. An existing open PR is not retroactively blocked by this newly introduced procedure.
- Current CI drift warnings are **advisory**. Existing pinned Contract validation and integration gates remain independent. Strict cross-repository G0 requires compatible FE + BE + Contract baseline; passing Backend unit checks cannot prove G2/G3/G4 end-to-end compatibility.
- Never log credentials or run state-changing calls against production services.
