# Backend Agent Guide

Work in the Backend Git repository only. Do not commit application code to the Workspace root.

- Workflow Skill: `.agents/skills/backend-workflow/SKILL.md`.
- Sibling Contract: `../user-management-contract`; from Workspace root: `repos/user-management-contract`.
- Read the Contract `docs/aa/README.md`, `docs/aa/decisions.md`, `docs/aa/auth-design.md`, `docs/aa/integration-test-plan.md`, `docs/governance/agent-skills-guide.md` before implementation.
- Use `contract.sha` and pinned OpenAPI, not current dev HEAD, for external schemas.
- A=Auth, B=User, C=Permission, each owning FE and BE. Preserve data ownership and 401/403 policy.
- Propose Contract PR first for API/DB/shared design changes; test-only Auth Fixtures must not be enabled in production.
- Feature PRs target dev, with independent commit history. No automatic pull, switch, commit or push.

- **Before initializing or changing Backend stack/database, read `docs/aa/tech-stack.md` and sibling Contract `docs/aa/tech-stack-proposal.md`; check DEC-003/011.** This is a proposal, not approval.
