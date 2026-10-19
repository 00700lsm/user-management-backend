# Backend 경계

- Auth (A): 로그인/로그아웃, 토큰, 비밀번호. 사용자 상태/계정 정보는 User 영역을 통해 확인.
- User (B): 회원가입, 프로필, 관리자 사용자 CRUD, ACTIVE/INACTIVE/WITHDRAWN 상태.
- Permission (C): USER/ADMIN Role과 사용자 권한. 사용자 프로필은 User가 소유.

세 도메인 모두 **동일 담당자가 프론트와 백을 수직 개발**합니다.

공통 정본: [AA 아키텍처](https://github.com/00700lsm/user-management-contract/blob/main/docs/architecture.md), [논리 DB 설계](https://github.com/00700lsm/user-management-contract/blob/main/docs/db-design.md).

## 확정 전 사항
- Spring Boot 등 프레임워크/빌드 도구
- 모놀리스 모듈화 vs 물리 MSA, DB 인스턴스
- 회원가입 시 도메인 간 호출/보상/트랜잭션
- 토큰 갱신/로그아웃 무효화 정책

위 사항은 개발 착수 전 공동 설계 결정 대상입니다.
