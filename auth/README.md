# Auth (개발자 A)

- 기능: 로그인/로그아웃, 비밀번호 변경, 인증/토큰
- API: `POST /auth/login`, `POST /auth/logout`, `POST /auth/password/change`
- Data: AUTH_CREDENTIAL (논리 소유)
- 화면: [Frontend Auth](https://github.com/00700lsm/user-management-frontend/tree/main/src/features/auth)
- 공통 API: [OpenAPI](https://github.com/00700lsm/user-management-contract/blob/main/openapi.yaml)

인증 실패, 비활성 사용자, 비밀번호 보안 정책을 테스트합니다.
