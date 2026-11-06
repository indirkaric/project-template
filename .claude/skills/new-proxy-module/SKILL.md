---
name: new-proxy-module
description: Ordered checklist for scaffolding a new orchestration/aggregation (BFF-style) endpoint that calls other microservices instead of reading/writing local persistence, per the proxy-bff-conventions skill. Use when starting a proxy endpoint from zero and you want the checklist injected into the current conversation instead of delegating to a separate subagent.
---

Follow `CLAUDE.md` at the repo root for the package map and its "Agent
behavior" section (language to respond in, editing/git policy). For the
exact rules at each step, pull in `proxy-bff-conventions`,
`inter-service-propagation`, `service-layer-conventions`, and
`controller-conventions` as you reach each step — they'll trigger
automatically, or invoke them directly.

This checklist is the ordering only. If the endpoint actually needs its
own local entity/table, use `new-crud-module` instead — don't force a
proxy shape onto persistence-backed data.

1. For each downstream service this endpoint needs: check whether
   `client/<downstream-service>/<DownstreamService>Client.java` already
   exists. If not, create it extending `client/BaseClient`.
2. Confirm (or add, once) the shared `ExchangeFilterFunction` in
   `config/WebClientConfig.java` that sets `Sqa-Client-Id`,
   `Sqa-Request-Id`, and `Sqa-user-id` on every outbound call from the
   service's one `WebClient` bean. Don't set headers ad hoc per client
   method as the only mechanism — that's how real services end up with
   gaps.
3. Decide the seam: real local persistence anywhere in this module? Use
   the existing `*ResponseCreatorService` for that part. Pure pass-through
   data? Skip ResponseCreatorService/mapper/entity/repository entirely for
   that data — the operation Service calls the client(s) directly.
4. `service/<domain>/I<Verb><Domain>Service` + impl — orchestration logic
   (call order, combining results, partial-failure handling) lives here.
   `Check.isNotNull` on every constructor dependency and public method
   parameter, same as any other service.
5. `<Domain>Controller` — delegates to the operation Service (or
   ResponseCreatorService where persistence exists), full Javadoc +
   `@Operation`/`@ApiResponses`/`@PreAuthorize`, `Check.*` on every
   parameter, methods not `final`.
6. If the orchestrated call kicks off a long-running/async operation in
   another service: default to client-side polling (trigger endpoint
   returns an id, a separate status endpoint polls it) — don't reach for
   WebSocket/SSE unless explicitly asked.
7. Tests for every new client wrapper method and the operation Service,
   then `mvn test` and `mvn checkstyle:check`.

This is the same ordering `proxy-service-creator` follows when delegated
to as a subagent. Use this skill instead of that agent when you want to do
the scaffolding yourself, in the main conversation, with the checklist as
a guide rather than handing the whole task off.
