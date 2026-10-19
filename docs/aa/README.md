# Backend AA 설계 인계서

**상태: AA Draft / 팀 합의 전.** 본 문서는 Backend 개발자가 착수 전에 확인할 설계 기준을 연결하는 인계 문서입니다. 요구사항·OpenAPI·논리 DB·시스템 경계의 **정본은 Contract Repository**에만 있습니다.

## 공통 정본 (Contract Repository)
- [AA 인계 전체 목차](https://github.com/00700lsm/user-management-contract/blob/dev/docs/aa/README.md)
- [요구사항](https://github.com/00700lsm/user-management-contract/blob/dev/docs/requirements.md) 및 [기능 정의](https://github.com/00700lsm/user-management-contract/blob/dev/docs/aa/functional-spec.md)
- [서비스 책임/데이터 소유](https://github.com/00700lsm/user-management-contract/blob/dev/docs/architecture.md)
- [논리 ERD/스키마](https://github.com/00700lsm/user-management-contract/blob/dev/docs/db-design.md)
- [공통 OpenAPI](https://github.com/00700lsm/user-management-contract/blob/dev/openapi.yaml)
- [요구사항 추적표](https://github.com/00700lsm/user-management-contract/blob/dev/docs/aa/traceability.md)
- [미결정 설계](https://github.com/00700lsm/user-management-contract/blob/dev/docs/aa/decisions.md)
- [Contract 변경 절차](https://github.com/00700lsm/user-management-contract/blob/dev/docs/contract-governance.md)

**구현 시** 이 Repository의 `contract.sha`가 지정한 승인 계약 커밋을 기준으로 합니다. 문서 링크의 `dev` HEAD가 계약 버전 고정값을 대체하지 않습니다.

## 개발자 분담
| 담당 | Backend 소유 | 본인 Frontend 소유 |
|---|---|---|
| A | Auth 인증/로그인/비밀번호 | Auth 화면 |
| B | User 가입/프로필/관리자 사용자 CRUD | User 화면 |
| C | Permission Role/사용자 권한 | Permission 화면 |

## Backend 인계 산출물
1. [서비스·도메인 경계](./service-boundaries.md) — 담당 기능/핵심 업무 규칙/협업 지점
2. [DB 인계 및 데이터 소유권](./data-handoff.md) — 테이블별 책임·정합성·개발자 확정 사항
3. [개발 착수/검증 체크리스트](./implementation-checklist.md) — Contract SHA/테스트/PR/미결정 사항

## AA/개발자의 책임 경계
- AA/PL: 요구사항·업무 규칙·서비스 경계·데이터 Owner·API 계약의 합의 및 공통 변경 승인
- Backend 담당: 언어/프레임워크, 세부 DDL, 코드 구조, 서비스 내 구현, 테스트 구현
- 물리 MSA 여부·DB 물리 분리·회원가입 다중 모듈 트랜잭션 등은 **아직 확정되지 않았습니다.** 개발자가 임의로 결정하지 않습니다.

현재 이 저장소는 구현 골격/기초 Contract 검증 도구만 있으며 **실행 서버나 실제 응답 Contract Test가 완성된 것은 아닙니다.**
