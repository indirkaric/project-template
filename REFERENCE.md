# Reference provenance

`skeleton/` is the starting point of every new service. It was distilled once from the two
reference services, so neither Claude nor the script ever reads them again. This file records
what came from where and what was deliberately changed. Read it only when changing the skeleton.

| Area | From | Changed on purpose |
|---|---|---|
| `pom.xml` | identity | Dropped batch, zxing, aws-rds, json-patch, snakeyaml, jpamodelgen. JPA/Liquibase sit between `@persistence` markers. JaCoCo excludes only wiring and data holders. |
| checkstyle | identity (stricter than artifact: 60-line methods, `InnerAssignment`, `Designed for extension.`) | — |
| `Dockerfile` | identity (slim alpine, non-root) | artifact's installs Chrome and mount-s3, so it was not used |
| `.gitlab-ci.yml` | identity | No e2e job, since a new service has no suite yet |
| security filters | artifact | Not `@Component` (artifact registers them both as beans and in the chain, so Boot runs each twice). `SqaUserIdAuthFilter` gets `IScopeContext` injected instead of a static lookup. A malformed header is ignored, not a 500. Unknown client → no authorities (identity grants defaults). The client-id allowlist is a `Set`, not an if-chain. |
| `ScopeContext` | artifact | Plain `ThreadLocal` for the user id (artifact's `InheritableThreadLocal` leaks into pooled threads), plus `clearSqaUserId()` |
| `WebClientConfig` | artifact (timeouts, retry, buffer) | `Sqa-request-id`/`Sqa-user-id` added per request. Artifact uses `.defaultHeaders`, which freezes the value at startup. |
| `BaseClient` | artifact | Binds base URI + `Sqa-Client-Id` once per downstream. Error swallowing is opt-in (artifact swallows 404/500/503 by default). |
| `GlobalExceptionHandler` | artifact | Adds `MethodArgumentNotValidException` → 400, and Spring MVC's `ErrorResponse` keeps its status (artifact turns an unknown path into 500). |
| datasource routing | artifact | The interceptor also matches class-level `@Transactional`. Artifact matches only the method level, so class-annotated write services read the read-only pool. |
| `entity/common`, `BaseEntityListener`, `AuditContext` | artifact | Without the LLM-specific update rule. Listener typed to `AuditEntity`. |
| `platform.md` | ports/URIs from all repos' `application*.yml`; caller policy from each `SqaClientIdHeaderAuthenticationFilter` (snapshot 2026-11-12) | — |

Verified: both roles build with `mvn -o verify` (tests, JaCoCo 0.80, checkstyle). The proxy role
boots, with health/swagger 200, 401 without auth and 404 for an unknown path. The persistence
role has not been booted against Postgres.
