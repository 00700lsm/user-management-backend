# Permission (개발자 C)

- 기능: 역할 목록, 사용자 역할 조회/변경
- API: `GET /admin/roles`, `GET/PUT /admin/users/{id}/roles`
- Data: ROLE, USER_ROLE (논리 소유)
- 화면: [Frontend Permission](https://github.com/00700lsm/user-management-frontend/tree/main/src/features/permission)
- 공통 API: [OpenAPI](https://github.com/00700lsm/user-management-contract/blob/main/openapi.yaml)

일반 사용자 접근 제한, 역할 변경 권한, 중복 역할 입력을 테스트합니다.
