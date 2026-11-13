---
name: proxy-bff-conventions
description: Use when building an orchestration/aggregation endpoint that calls other microservices and composes their responses instead of reading/writing local persistence (a BFF-style module).
---

The variant of the layout for a module with **no local persistence** for its data.

- One wrapper per downstream: `client/<downstream>/<Downstream>Client extends BaseClient`, base URI
  from `client.<downstream>.base-uri`, id from `Constants` (`inter-service-propagation`). Only the
  methods the module uses. Each returns a domain type or an empty `Optional`/list, never a raw
  `Mono`.
- No `ResponseCreatorService`, mapper, entity or repository for data that lives elsewhere. The
  operation `Service` calls the clients in dependency order and composes the result. Partial-failure
  behavior is explicit: fail the request, or return what's there with the gap marked. Never let a
  silent empty result stand in for a failure.
- Response records live in `modules/<domain>/model/`, as in any module.
- `Check`, exceptions, tests and controller rules apply unchanged.
- Long-running downstream work: a trigger endpoint returns an id and a status endpoint polls it.
  No WebSocket or SSE unless asked.
