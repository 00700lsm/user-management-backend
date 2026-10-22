# Auth/User/Permission 병렬 개발용 공통 인증 주체 (P1-04)

Contract DEC-001/005/009/010 및 [인증 설계](https://github.com/00700lsm/user-management-contract/blob/dev/docs/aa/auth-design.md)를 준수합니다. **이 문서는 실제 JWT 구현 완료 선언이 아닙니다.**

## 공통 운영 인터페이스 (src/main)

- `shared/VerifiedActor.java`: 서버 측에서 확인한 `userId`와 **현재 DB의 Role** 집합을 담는 불변 인증 주체.
- `shared/CurrentActor.java`: A/Auth가 SecurityContext에 적재한 `VerifiedActor`만 반환. 익명 요청, 타입이 다른 Principal, 임의 문자열 Username 등은 거절.
- B/User는 `CurrentActor.required().userId()`를 사용하여 본인 정보를 조회하고, C/Permission은 서버 측 Role을 확인합니다. 파라미터/헤더/JWT Payload의 Role을 신뢰하지 않습니다.
- 실제 Auth 필터 구현 전까지 운영 요청에는 유효한 인증 주체가 자동 생성되지 않습니다. 기존 `SecurityConfig`의 fail-closed 기본값은 그대로입니다.

## 테스트 Fixture (src/test에만 존재)

`src/test/java/dev/usermanagement/shared/TestActors.java`:
- `activeUser()`: 사용자 1001, Role `USER` — FIX-USER
- `activeAdmin()`: 사용자 1002, Role `USER`, `ADMIN` — FIX-ADMIN
- `actor(id, databaseRoles)`: 서비스 단위 테스트에 사용할 명시적인 계정/Role Fixture

이 Fixture는 `UsernamePasswordAuthenticationToken`을 테스트 SecurityContext에 직접 적재하는 도구입니다. **서명된 JWT도 아니고 실제 사용자 상태 DB 검증도 아닙니다.** 절대로 `src/main`, 운영 Bean/Filter, dev 우회 로그인 엔드포인트에 복사하지 않습니다.

## A / B / C 책임과 단계

| 담당 | 구현 / 테스트 연결 |
|---|---|
| A Auth | JWT 서명·만료·JTI 폐기 확인 → User 상태 ACTIVE 재조회 → Permission 최신 Role 조회 → `VerifiedActor` 생성 및 SecurityContext 적재. 각 보호 요청마다 수행 |
| B User | `CurrentActor`로 본인 ID를 얻어 User 전용 데이터에 접근; Guest·타인 접근/계정 변경은 실제 API 테스트로 검증 |
| C Permission | 테스트 Fixture로 USER/ADMIN 권한 분기를 검증하되, 실제 권한 변경 후 다음 API 호출 시 최신 DB Role 반영은 G3에서 실증 |

## 검증·완료 판정

- `./gradlew --no-daemon test`가 통과하면 기본 Context/Entity 검증과 `CurrentActorTests`의 순수 테스트 Fixture 계약만 확인됩니다.
- `FIX-EXPIRED`, `FIX-INACTIVE`, `FIX-WITHDRAWN`은 **추후 Auth A의 실제 JWT/DB User 상태 검사에서** 확인해야 하며 지금 인증 우회 Fixture로 꾸미지 않습니다.
- G2 실제 Controller/계약 테스트, G3 실 JWT, G4 FE/BE E2E는 현재 `BLOCKED/NOT_RUN`. Mock USER/ADMIN 검증은 프로덕션 인가의 증거가 아닙니다.
- 아직 승인되지 않은 내부 서비스 인터페이스·서버 인증 우회 엔드포인트나 새 외부 OpenAPI 필드를 추가하지 않습니다.
