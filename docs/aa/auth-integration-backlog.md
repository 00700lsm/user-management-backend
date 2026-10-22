# P1-04 Auth 통합 인계 및 독립 테스트 기준

이 문서는 Contract DEC-001/002/005/009/010, `contract.sha`에 고정된 OpenAPI, Backend `AGENTS.md`를 준수합니다. **아직 실제 JWT, User/Permission 업무 Controller는 구현되지 않았습니다.**

## 공통 인터페이스

- `shared/VerifiedActor`: Backend Auth 경계에서 **서명·유효기간·JTI 검증, User ACTIVE 및 현재 DB Role을 확인한 뒤** 생성할 `userId`/Role 집합.
- `shared/CurrentActor.required()`: 보호 서비스가 검증된 주체만 꺼내는 공통 접근점.
- `src/test/.../TestActors`: MockMvc 및 서비스 테스트에서 `UsernamePasswordAuthenticationToken`으로 User/Admin **테스트용 SecurityContext만** 주입합니다. 운영용 구현/Bean/Auth Filter가 아닙니다.
- 테스트 전용 `AuthBoundaryMockMvcTests.FixtureController`는 `src/test`에만 존재하며 실제 OpenAPI 외부 엔드포인트가 아닙니다. 테스트 성공이 실 API/실 JWT 검증의 증거가 아님.

## Auth 담당 A가 추가 구현해야 할 것

1. Contract의 `POST /auth/login`, `POST /auth/logout`, `POST /auth/password/change`를 OpenAPI 기준으로 구현.
2. JWT 발급: 15분 Access Token, Refresh 없음; `sub` = User ID 문자열, `roles` = Role 코드(UI 표시 참고), `jti` = 고유 식별자.
3. **모든 보호 요청마다** JWT 서명·만료·JTI 폐기 목록 확인, DB의 ACTIVE 상태 재조회, DB 최신 USER/ADMIN Role 재조회. 이 검증을 통과한 `VerifiedActor`만 SecurityContext에 저장.
4. 401/403 `ApiError {code,message}` 보장, 로그아웃 JTI 만료 전 폐기, INACTIVE/WITHDRAWN 차단, Role 변경 다음 요청부터 반영.
5. Mock 테스트 키/계정이 Production에 포함되지 않도록 보장. 서버 고정 Secret·우회 엔드포인트 금지.
6. 실제 Auth Token으로 B/User `/users/me` 및 C/Permission 관리자 API의 **실제 Controller**를 검증하고 G3를 별도 증빙.

## B/C 담당자의 독립 테스트 방식

1. `TestActors.activeUser()`, `TestActors.activeAdmin()`은 각각 FIX-USER(1001), FIX-ADMIN(1002)의 **검증 완료 상태를 테스트에서만 가정한 주체**입니다.
2. `AuthBoundaryMockMvcTests`는 test-only Controller를 이용해 Spring Security에서 Guest 401, USER/Admin 403과 허용 동작, `CurrentActor`의 ID 읽기를 테스트합니다. 실제 비즈니스 API 컨트롤러의 요청/응답은 아직 검증하지 않습니다.
3. 새 User/Permission Controller의 테스트를 작성할 때만 test `Authentication`을 주입하고, **실제 Controller 구현이 완료된 다음** pinned OpenAPI Response·Status를 G2에서 검증합니다.
4. JWT 변조/만료/로그아웃 JTI, 실제 DB Role/Status 갱신, Signup 원자성은 A의 실 Auth 및 B/C 기능 구현 후 별도 G3로 검증해야 합니다. 테스트 환경의 무서명 인증 객체로 실 Auth 성공이라 주장하지 않습니다.

## 테스트 명령 및 완료 조건

```bash
./gradlew --no-daemon test bootJar
```

CI는 BootJar 내부에 TestActors 및 시험용 Controller가 포함되지 않았는지 검사합니다.
- 현재 준비 단계: `CurrentActorTests`, `AuthBoundaryMockMvcTests`, Gradle context/JPA/Flyway, BootJar 분리 검증.
- `G2: BLOCKED` 실제 User/Permission Controller 미완료.
- `G3: BLOCKED` 실제 JWT 서명/DB 최신 상태 확인 미완료.
- `G4: BLOCKED` Frontend MSW를 끈 실제 API 통합 미완료.

본 작업은 인증 기능의 완성이 아니라 **A/B/C 병렬 구현이 가능하도록 테스트·인가 경계를 선행 마련**한 것입니다.
