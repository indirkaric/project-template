---
name: inter-service-propagation
description: Use when touching authentication filters, SecurityConfiguration, IScopeContext, a WebClientConfig, or any client/<service> wrapper class — covers how this service authenticates inbound callers and propagates identity (Sqa-Client-Id, Sqa-user-id, Sqa-Request-Id) on outbound calls.
---

This is how {{ServiceName}} authenticates incoming calls and how it
authenticates itself when calling other microservices. This mechanism is
verified across five real production services — follow it exactly, it is
not optional scaffolding. Read `CLAUDE.md` first for the placeholders and
the overall map.

## Inbound: who's calling this service

Three (or four) servlet filters, each `OncePerRequestFilter`, registered
with `addFilterBefore(..., BasicAuthenticationFilter.class)` in
`SecurityConfiguration`, in this order:

1. **`SqaClientIdHeaderAuthenticationFilter`** — reads header
   `Sqa-Client-Id` (constant `Constants.SQA_CLIENT_ID_HEADER`). If
   present, it's a trusted service-to-service call (trusted at the
   network layer, no signature check) — builds a
   `UsernamePasswordAuthenticationToken` with the header value as
   principal, and grants authorities by matching the value against a
   known-caller allowlist (e.g. `"UI.{{ServiceName}}"`,
   `"Integration.{{ServiceName}}"` — name format is
   `"<Caller>.<Callee>"`).
2. **`SqaRequestIdHeaderFilter`** — reads/generates header
   `Sqa-Request-Id`, stores it as the correlation id on `IScopeContext`
   for the lifetime of the request, clears it in `finally`. Not an auth
   filter — pure correlation-id propagation.
3. **`SqaUserIdAuthFilter`** — reads header `Sqa-user-id`, a
   base64-encoded JSON blob `{"userId": ..., "email": ...}`, decodes it,
   stores the userId on `IScopeContext` via a `ThreadLocal`, clears it in
   `finally`. Only needed on services that are called by other backend
   services on behalf of an end user (i.e. most services below the BFF
   layer).
4. **`PersonalTokenAuthenticationFilter`** — only on services that accept
   calls directly from a browser/external client via
   `Authorization: PersonalToken ...` (typically just the BFF/UI-facing
   service). It does NOT validate the token itself — it calls identity's
   `GET /identity/v1/personal-access-tokens/authenticate` endpoint with
   the raw header, and uses the returned userId/permissions as the
   principal.
5. **`.oauth2ResourceServer(oauth2 -> oauth2.jwt(Customizer.withDefaults()))`**
   — standard Keycloak JWT validation, always last — the fallback for
   real end-user browser sessions that didn't come in via PAT.

If {{ServiceName}} IS the identity-of-record for Personal Access Tokens
(unlikely — normally only one service in the platform is), it instead
exposes that `@PermitAll`, whitelisted `.../authenticate` endpoint itself
rather than running filter #4. State explicitly in your service's own
notes which role applies.

## Reading "who is the current user"

`IScopeContext` / `ScopeContext` (`@Service`, under `service/security/`):

- `getCurrentUser()` / `getCurrentUserId()` — read
  `SecurityContextHolder.getContext().getAuthentication().getPrincipal()`;
  if it's a raw `String`, this is a service-to-service call, return it
  directly; if it's a `Jwt`, read the `email`/`user_id` claim. Fall back
  to a constant app name when there's no authentication at all
  (cron/startup jobs).
- `getBase64EncodedUserInfo()` — re-encodes the current user as base64
  JSON for propagation to downstream calls (see below).
- `getCorrelationId()` / `setCorrelationId()` — the `Sqa-Request-Id`
  value, backed by a `ThreadLocal`.
- Entity listeners (not Spring beans) fetch this bean via a static
  `BeanUtility.getBean("scopeContext")` lookup rather than constructor
  injection.

## Outbound: calling another microservice

Every `WebClient` call {{ServiceName}} makes to another microservice MUST
set all three headers:

- `Sqa-Client-Id` → `"{{ServiceName}}.<TargetService>"`
- `Sqa-Request-Id` → the current correlation id from `IScopeContext`
- `Sqa-user-id` → `scopeContext.getBase64EncodedUserInfo()`

**Exception:** calls to identity's PAT-authenticate endpoint skip
`Sqa-user-id` — there is no per-request user context yet at that point.

Set all three via **one shared `ExchangeFilterFunction`** registered on
the service's single `WebClient` bean (in `config/WebClientConfig.java`),
not per client-method. Setting headers per-method is how real services
ended up with gaps (a client class that quietly forgot one header on
every call) — centralizing on one filter function is the fix, and it's
what every new `client/<downstream>/` wrapper class gets for free.

## Every application has users

A new service bootstraps with the full inbound filter chain (section
above) and `IScopeContext` from day one — this is not something to add
later once there's "a real auth requirement." Every write path needs a
`createdBy`/`updatedBy` actor, and every outbound call needs a
`Sqa-Client-Id` identity, from the very first endpoint.
