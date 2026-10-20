# Backend 기술 스택 / H2 로컬 개발 가이드 (PoC Accepted)

**상태: Accepted PoC 기준 / 실행 골격 추가, 실제 빌드 미검증.** 기술 스택 공통 정본: sibling Contract `../user-management-contract/docs/aa/tech-stack-proposal.md` (Workspace 위치: `repos/user-management-contract/docs/aa/tech-stack-proposal.md`). 승인 내용은 Contract `docs/aa/decisions.md`의 DEC-003/011을 확인합니다.

## 권장 구성

- Java 21 LTS + Spring Boot 4.1.x + Spring Web MVC, Spring Security
- Spring Data JPA + Hibernate ORM: Java Entity↔테이블 매핑. **Hibernate는 DB가 아닙니다.**
- H2 Embedded: 로컬은 File Mode, Unit/Integration Test는 Memory Mode. **별도 DB 서버 설치 필요 없음.**
- Flyway: 스키마 버전 이력. `spring.jpa.hibernate.ddl-auto=validate`와 병행 (자동 `create/update`는 마이그레이션 기준선을 해치므로 지양).
- Gradle Wrapper + JUnit + MockMvc, 인증 통합 E2E는 FE/BE 전체 연결 시 수행
- **한 개 Spring Boot 프로세스 내 Auth/User/Permission 모듈** (단일 Git Backend Repository, 물리 MSA 아님). AA DEC-003 승인된 기준.

## 프로젝트 구성 제안

```text
user-management-backend/
├── build.gradle.kts
├── settings.gradle.kts
├── gradlew / gradle/wrapper/
├── src/main/java/.../
│   ├── app/           # 공통 진입점/설정
│   ├── auth/          # 인증/자격증명 Owner A
│   ├── user/          # 계정/프로필 Owner B
│   ├── permission/    # 권한/Role Owner C
│   └── shared/        # 검토된 보안 경계/공통 유틸만
├── src/main/resources/
│   ├── application.yml
│   ├── application-local.yml
│   └── db/migration/V1__initial_schema.sql
├── src/test/          # test Profile + fixture
└── .local-data/        # gitignore 로컬 DB 파일 (커밋 금지)
```

기존 `auth/`, `user/`, `permission/`은 문서 placeholder이며 실제 Java 모듈 Entity 골격은 `src/main/java/dev/usermanagement/`에 생성했습니다. Gradle·H2/Flyway·Security 프로젝트 실행 골격을 추가했으며 **실제 빌드 검증은 아직 수행하지 않았습니다.**

## H2 DB Profile 예시 (적용된 기본 Profile (실제 실행 전 검증 필요))

```yaml
# application-local.yml 예시 — Secret/실제 운영 환경 아님
spring:
  datasource:
    url: jdbc:h2:file:./.local-data/user-management;DB_CLOSE_ON_EXIT=FALSE
    driver-class-name: org.h2.Driver
    username: sa
    password: ""
  jpa:
    hibernate:
      ddl-auto: validate
  flyway:
    enabled: true
```

```yaml
# application-test.yml 예시
spring:
  datasource:
    url: jdbc:h2:mem:user-management-test;DB_CLOSE_DELAY=-1
    driver-class-name: org.h2.Driver
    username: sa
    password: ""
  jpa:
    hibernate:
      ddl-auto: validate
  flyway:
    enabled: true
```

- `spring.profiles.active=local`을 **개발에서만** 사용; 운영에 H2·H2 Console·테스트 Auth 우회 노출 금지.
- `.local-data/`, `*.mv.db`, `*.trace.db`, 비밀값/임시 환경파일은 `.gitignore` 대상.
- 파일형 H2는 각자의 PC에서 **한 Backend 프로세스**가 열도록 운용합니다. 세 개발자가 단일 DB 파일을 공유하지 않습니다.
- H2 특정 SQL 차이가 있을 수 있으므로 추후 PostgreSQL 전환 시 별도 호환 테스트 필요.
- Flyway SQL 파일 이름은 `V1__initial_schema.sql` 등의 유일한 버전 순서를 사용. A/B/C의 마이그레이션 번호 충돌은 PR 합의/통합으로 해결.
- 논리 Entity `USER`의 물리 테이블 이름은 SQL 예약어 충돌을 피하도록 `app_user` 등으로 매핑하는 것을 추천합니다. **논리 Owner/필드/Key 자체는 Contract 정본 유지**.
- 인증용 Test Secret, 운영 DB 암호, 개발자 실제 계정 등은 Git에 포함하지 않습니다.

## 독립 개발·연결 단계

1. AA: DEC-003/011과 DEC-001/002/009 승인 완료. Backend 기능 구현 시 각 결정 준수
2. 공통 BE Skeleton + H2 File + Flyway + Test Profile + 공통 Security Principal과 Test Fixture 계약 정의
3. A/B/C 각각 Auth/User/Permission **기능별 FE+BE 수직 개발**, Backend는 한 애플리케이션에 모듈로 합류
4. Schema 확인→Mock/Fixture 단위 테스트→실제 Auth Token→G2~G4 통합·E2E
5. FE/BE의 동일 `contract.sha` + Git Commit 조합 기록

## 필수 확인
- [ ] 저장소 `contract.sha`, Contract AA 기술 스택·미결정 기록 확인
- [ ] H2 파일이 Git 추적되지 않음, 마이그레이션만 커밋
- [ ] Fixture 생성·초기화 재현 가능, SECRET 환경 격리
- [ ] 401/403/오류·Role 확인 규칙 일치
- [ ] 빌드·Unit·Runtime Contract Test 실행 증거 확인 (실행 전 완료 아님)
