# project-template

Reusable Claude Code setup (architecture rules + agents + skills) for
starting a new Spring Boot 3.2+ / Java 17 microservice from zero, with a
non-negotiable layered architecture and a verified inter-service identity
propagation mechanism baked in from day one.

This is **not** a buildable project itself — it's a stamp to copy into a
new, empty repo.

## How to use it for a new project

1. Create (or clone into) a new, empty repo for the new service.
2. Copy `CLAUDE.md` and the whole `.claude/` folder from this template into
   that repo's root.
3. Open `CLAUDE.md` and find/replace every placeholder:
   - `{{ServiceName}}` — PascalCase service name, e.g. `PaymentService`
   - `{{service-name}}` — kebab-case, e.g. `payment-service`
   - `{{Domain}}` / `{{domain}}` — your first domain module, e.g.
     `Payment` / `payment`
   - `{{port}}` — the service's port
   - `{{schema}}` — its Postgres schema name
4. Open Claude Code in that new repo and just describe what you want:
   - "Scaffold a `<Domain>` module" → triggers `service-creator` /
     the `new-crud-module` skill.
   - "Add the entity and repository for `<Domain>`, nothing else yet" →
     `database-service-creator`.
   - "Add a proxy endpoint that calls X and Y and combines them" →
     `proxy-service-creator` / the `new-proxy-module` skill.
   - "Review what I just wrote" → `code-reviewer`.
   - "Check the header propagation on the new client" →
     `integration-flow-auditor`.
   - "Write tests for X" → `test-writer`.

`CLAUDE.md` itself is kept short on purpose — the full rule for each
layer (entities, repositories, controllers, exceptions, tests,
inter-service propagation, proxy/BFF, optional Kafka events) lives in its
own skill under `.claude/skills/`, listed in `CLAUDE.md`'s "Conventions
by layer" table. Those load automatically when you're actually working
on that layer instead of being read in full every time.

## Where this came from

`CLAUDE.md` and its skills combine two things:

- The layered Controller → ResponseCreatorService → Service → Validator →
  Repository → Mapper → Entity convention (package layout, `Check`
  defensive checks, exception mapping, test style, Checkstyle) — a
  proven, strict baseline.
- A verified inter-service auth/identity-propagation mechanism
  (`Sqa-Client-Id`, `Sqa-user-id`, `Sqa-Request-Id`, the 3-tier filter
  chain, `IScopeContext`) and the BFF/proxy client pattern, extracted from
  reading five real production microservices end-to-end. Where those real
  services were inconsistent with each other (test style, exception
  logging, one missing header on one client), `CLAUDE.md` picked the
  stricter rule rather than copying the inconsistency forward.

Don't loosen either part without flagging it to whoever's reviewing the
change.
