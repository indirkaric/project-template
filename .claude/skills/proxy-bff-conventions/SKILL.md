---
name: proxy-bff-conventions
description: Use when building an orchestration/aggregation endpoint that calls other microservices and composes their responses instead of reading/writing local persistence (a BFF-style module).
---

Full detail for the proxy/BFF variant of this repo's layering, referenced
by `CLAUDE.md`'s package layout. Read `CLAUDE.md` first for the
placeholders and the overall map, and the `inter-service-propagation`
skill for the header mechanics this section depends on.

## Proxy / BFF services

Use this variant instead of the standard layout when {{ServiceName}} (or
one of its modules) is an aggregation/orchestration layer with **no local
persistence** for a given domain — it only calls other services and
composes their responses.

- `client/<downstream-service>/<DownstreamService>Client.java` — one
  hand-written `WebClient` wrapper class per downstream service, all
  extending a shared `client/BaseClient` (shared error-mapping helpers).
  Every method sets `Sqa-Client-Id` explicitly for extra clarity even
  though the shared filter also sets it — this is belt-and-suspenders,
  not a contradiction.
- The `*ResponseCreatorService` seam (see `service-layer-conventions`) is
  used **only** where the module has real local persistence. For a pure
  pass-through module (no local entity): the operation `Service` itself
  calls the client(s) directly, in whatever order the data dependencies
  require, and composes the result — don't invent a fake
  `mapper`/`entity`/`repository` for data that only ever lives in another
  service.
- Still apply the Check/Exception/Test rules identically regardless of
  whether a given module is persistence-backed or pure-proxy — the BFF
  shape changes where data comes from, not the defensive-coding or
  testing bar.
- Long-running downstream operations (e.g. triggering an async pipeline
  in another service) are handled by client-side polling by default
  (trigger endpoint returns an id immediately, a second endpoint polls
  status) — don't reach for WebSocket/SSE unless {{ServiceName}}
  specifically needs push semantics.
