---
name: controller-conventions
description: Use when creating or modifying a REST Controller in this repo — endpoint annotations, status codes, and the final-method CGLIB gotcha.
---

Full detail for the `<Domain>Controller` layer referenced by `CLAUDE.md`'s
package layout. Read `CLAUDE.md` first for the placeholders and the
overall map.

## Controllers

- `@RestController`, `@RequestMapping(path = "/api/v1/<plural>", produces =
  MediaType.APPLICATION_JSON_VALUE)`, `@Tag(name = "...", description =
  "...")`.
- Every endpoint: full Javadoc, `@Operation` + `@ApiResponses` (+
  `@Parameters` for query params), `@PreAuthorize(Constants.Authority.X_Y_AUTHORITY)`,
  `@Valid @RequestBody` for bodies, `Check.*` on every parameter, delegate
  to exactly one `*ResponseCreatorService` (or a bare operation service for
  delete), wrap the result in `ResponseEntity` with the right status
  (`201` create, `200` read/update, `204` delete or empty list, `404`/
  `422` documented in `@ApiResponses` even though they're thrown, not
  returned, from this method).
- Class members are `final` wherever the language allows it — but
  controller **methods are never `final`**: Spring's method-security proxy
  (CGLIB) must be able to override them to route through `@PreAuthorize`
  interception; a `final` method breaks that and produces an NPE on every
  injected field the moment the endpoint is actually called (not caught by
  `mvn test` with mocks, only on live traffic, since method-security
  proxies are only built for the real Spring context). This has been the
  root cause of real incidents across multiple services built on this
  exact convention.
- The Controller imports nothing from `entity`, `repository`, or `mapper`
  packages — only `model`/`dto` and `*ResponseCreatorService` (or, for a
  pure proxy module, the operation Service it calls directly).
