# Backend 착수·검증 체크리스트 (미완료)

## 착수
- [ ] [AA 기능 정의](https://github.com/00700lsm/user-management-contract/blob/dev/docs/aa/functional-spec.md), [서비스 경계](./service-boundaries.md), [DB 인계](./data-handoff.md) 확인
- [ ] 구현을 막는 [미결정 사항](https://github.com/00700lsm/user-management-contract/blob/dev/docs/aa/decisions.md) 해결/보류범위 합의
- [ ] `contract.sha`의 40자리 SHA 확인 및 Frontend와 기준 일치
- [ ] `bash scripts/sync-contract.sh`로 고정 OpenAPI 다운로드
- [ ] DBMS/구현 프레임워크/물리 서비스 구성 확정 후 구현 시작

## 테스트 계획
- [ ] TC-AUTH-001~003: 정상/실패 로그인, 비활성 사용자, 로그아웃, 비밀번호 변경
- [ ] TC-USER-001~005: 회원가입 중복/정합성, 내 정보, 관리자 목록 검색·페이징·접근제어, 수정/상태
- [ ] TC-PERM-001~002: Role 목록, 역할 할당·중복/권한 접근제어
- [ ] 실제 서버 응답의 HTTP 상태·필드·오류가 승인 OpenAPI와 동일함을 계약 테스트로 검증
- [ ] 가입 시 여러 도메인 협업 실패에 대한 통합 테스트
- [ ] Backend PR에 REQ/TC ID·Contract SHA·Frontend 연관 PR 링크/테스트 증거 첨부

**주의:** 현 CI는 OpenAPI 구조 검증용이며, 실제 서버 계약 테스트는 아직 구현되지 않았습니다. 이 체크리스트를 완료로 간주하지 마세요.
