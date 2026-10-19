# User (개발자 B)

- 기능: 회원가입, 내 정보 조회/수정, 관리자 사용자 목록/상세/등록/수정/상태
- API: `/users/signup`, `/users/me`, `/admin/users`, `/admin/users/{id}`, `/admin/users/{id}/status`
- Data: USER (논리 소유)
- 화면: [Frontend User](https://github.com/00700lsm/user-management-frontend/tree/main/src/features/user)
- 공통 API: [OpenAPI](https://github.com/00700lsm/user-management-contract/blob/main/openapi.yaml)

검색 OR, 기본 페이지 20, 로그인 ID/이메일 중복, Soft Delete를 테스트합니다.
