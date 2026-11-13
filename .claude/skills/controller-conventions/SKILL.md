---
name: controller-conventions
description: Use when creating or modifying a REST Controller in this repo — endpoint annotations, status codes, and the final-method CGLIB gotcha.
---

Full detail for the `<Domain>Controller` layer referenced by `CLAUDE.md`'s
package layout. Read `CLAUDE.md` first for the placeholders and the
overall map.

## Controllers

- `modules/<domain>/<Domain>Controller.java`, `@RestController`,
  `@RequestMapping(path = {"/{{api-prefix}}/v1/<plural>"}, produces =
  MediaType.APPLICATION_JSON_VALUE)`, `@Tag(name = "...", description =
  "...")`. The service prefix lives in the mapping — there is no
  `server.servlet.context-path`.
- Every endpoint: full Javadoc, `@Operation(..., security =
  {@SecurityRequirement(name = Constants.BEARER_KEY_VALUE)})` +
  `@ApiResponses` (+ `@Parameters` for query params),
  `@PreAuthorize(Constants.Authority.X_Y_AUTHORITY)`,
  `@Valid @RequestBody` for bodies, `Check.*` on every parameter, delegate
  to exactly one `*ResponseCreatorService` (or a bare operation service for
  delete), wrap the result in `ResponseEntity` with the right status
  (`201` create, `200` read/update, `204` delete or empty list, `404`/
  `422` documented in `@ApiResponses` even though they're thrown, not
  returned, from this method).
- **No generic response envelope.** Create returns
  `ResponseEntity<EntityCreatedResponse>` (`dto/response/EntityCreatedResponse(String id)`);
  a list returns a per-module `<Domain>ListResponse` record with exactly
  `List<<Domain>ItemResponse> data` + `long count`. Pagination is Spring
  Data `Pageable` with `@PageableDefault(size = Constants.LIMIT_PAGE_SIZE)`;
  list filters go in a `<Domain>ListRequest` body, not in a dozen query
  params.
- Class members are `final` wherever the language allows it — but
  controller **methods are never `final`**: Spring's method-security proxy
  (CGLIB) must be able to override them to route through `@PreAuthorize`
  interception; a `final` method breaks that and produces an NPE on every
  injected field the moment the endpoint is actually called (not caught by
  `mvn test` with mocks, only on live traffic, since method-security
  proxies are only built for the real Spring context). This has been the
  root cause of real incidents across multiple services built on this
  exact convention. Same reason a `@Transactional` class is never `final`.
- The Controller imports nothing from `entity`, `repository`, `mapper` or
  internal `dto` packages — only its module's `model/`,
  `dto/response/EntityCreatedResponse`, and `*ResponseCreatorService` (or,
  for a pure proxy module, the operation Service it calls directly).
