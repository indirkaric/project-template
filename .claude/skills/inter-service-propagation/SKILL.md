---
name: inter-service-propagation
description: Use when touching authentication filters, SecurityConfiguration, IScopeContext, WebClientConfig, BaseClient or any client/<service> wrapper, or when calling another service — inbound auth, outbound Sqa-Client-Id / Sqa-request-id / Sqa-user-id, and the platform.md registry of other services.
---

Implemented in `security/`, `service/security/`, `config/WebClientConfig` and `client/BaseClient`.
Extend those classes; don't re-implement them. Other services' addresses and caller policies:
`platform.md` next to this file. Never read a sibling repo.

## Inbound

`SecurityConfiguration` adds the filters with `addFilterBefore(..., BasicAuthenticationFilter.class)`
in this order. It creates them with `new`, never as `@Component`, because Boot would also register
a bean filter as a servlet filter and run it twice.

1. `SqaClientIdHeaderAuthenticationFilter` — `Sqa-Client-Id`, format `<Caller>.<Callee>`, trusted at
   the network layer. Ids in `Constants.TRUSTED_CLIENTS` get `Authority.fullAuthorities()`; unknown
   ids get none. A new caller = a new constant in that set.
2. `SqaRequestIdHeaderFilter` — reads or generates the correlation id into `IScopeContext`; clears it
   in `finally`.
3. `SqaUserIdAuthFilter` — decodes base64 JSON `{"userId"}` into `IScopeContext`; clears it in
   `finally`; ignores a malformed header.
4. `PersonalTokenAuthenticationFilter` — browser-facing services only; not in the skeleton. It calls
   identity's PAT endpoint (see `platform.md`). Adding it is a `## TREBA ODLUKA`, never a default.
5. `.oauth2ResourceServer(jwt)` — Keycloak, always last.

## Current user

`IScopeContext`: `getCurrentUser()`/`getCurrentUserId()` (JWT `email`/`user_id`, the raw caller id for
a service call, `Constants.APP_NAME` without authentication), the correlation id, the forwarded user
id, and `getBase64EncodedUserInfo()`. `AuditContext.resolveActor()` prefers the forwarded user over
the direct caller. Entity listeners get beans through `BeanUtility.getBean(Class)`.

## Outbound

Every call to another service carries:

- `Sqa-Client-Id` = `<ThisService>.<Downstream>`. A wrapper passes it once:
  `super(webClient, baseUri, Constants.<DOWNSTREAM>_CLIENT)`, and `BaseClient` binds it. Never set it
  per method.
- `Sqa-request-id` and `Sqa-user-id`, added per request by `WebClientConfig.propagateSqaHeaders()`.
  `Sqa-user-id` is sent only when a forwarded user exists, so identity's PAT call carries none, as
  it must.

Rules:

- One `WebClient` bean.
- Read header values when the request is sent. Never put ThreadLocal values into
  `.defaultHeaders(...)`: they would be frozen at startup.
- Call `.block()` on the request thread, because the values are ThreadLocals. No `subscribeOn` or
  `publishOn` before the exchange.
- Swallowing errors is opt-in per call:
  `.onErrorResume(WebClientResponseException.class, suppressErrors(HttpStatus.NOT_FOUND))`.
- A downstream whose `platform.md` row says `nothing` returns 403 until it allowlists our id.
  That is an edit in its repo, so it needs the user's yes.
