# CLAUDE.md — {{ServiceName}}

> **Fill these in before using this template**, then delete this note:
> `{{ServiceName}}` (e.g. `OrderService`), `{{service-name}}` (kebab-case,
> e.g. `order-service`), `{{Domain}}`/`{{domain}}` (e.g. `Order`/`order`,
> your first domain module), `{{port}}`, `{{schema}}` (Postgres schema
> name). Find/replace all four across this file and the files under
> `.claude/`.

## What this is

Spring Boot 3.2+ / Java 17 microservice, one service per repo. This file
defines a **non-negotiable** architecture. The detailed rule for each
layer lives in its own skill under `.claude/skills/` (see table below) —
loaded only when that layer is actually being touched — so this file
stays a short index, not the full rulebook.

This template encodes two sources, reconciled on purpose: a proven
layered-domain-service convention (package layout, `Check` defensive
checks, exception mapping, test style), and a verified inter-service
identity-propagation mechanism (`Sqa-Client-Id` / `Sqa-user-id` /
`Sqa-Request-Id`, the 3-tier auth filter chain, `IScopeContext`)
extracted from five real production microservices. Where those real
services disagreed with each other or with the baseline, the skills pick
the **stricter** rule, not a copy-paste of every real-world
inconsistency. Don't deviate from either without calling it out first.

## Package layout (per domain module, e.g. "{{domain}}", "product", "user")

```
entity/<Entity>.java                          JPA entity

modules/<domain>/
  <Domain>Controller.java                     REST layer
  model/                                       API request/response records
  dto/                                         internal DTOs built off projections
  mapper/I<Domain>Mapper.java                  MapStruct
  repository/I<Domain>Repository.java          domain-facing repository contract
  repository/impl/<Domain>Repository.java      Spring Data JPA backed impl
  repository/projection/<Domain>Projection.java read-path projections
  service/I<Verb><Domain>ResponseCreatorService.java + impl/
                                                composes core service(s) + mapper
                                                into the final API response

service/<domain>/I<Verb><Domain>Service.java + impl/
                                                ONE interface PER OPERATION —
                                                never one fat "<Domain>Service"
                                                god class for everything

service/validator/I<Verb><Domain>ServiceValidator.java + impl/
                                                one validator per write
                                                operation, sharing rules via a
                                                Base<Domain>DataValidator

event/<Domain>ChangeEvent + <Domain>EventListener               OPTIONAL —
                                                see domain-events-conventions
                                                skill.
```

Rule of thumb: the Controller depends ONLY on `*ResponseCreatorService`
interfaces (plus a bare operation service for operations with no response
body, like delete). It never imports a `Repository`, an `Entity`, or a
MapStruct mapper directly. A pure proxy/BFF module (no local persistence)
follows a different variant — see the `proxy-bff-conventions` skill.

## Defensive checks — non-negotiable

Call the `Check` utility (`Check.isNotNull`, `Check.isNotEmpty`) at the
start of every constructor (once per injected dependency) and the start
of every public method (once per parameter) — in Controllers, Services,
Validators, ResponseCreatorServices, client wrapper classes, and filters
alike. A missing `Check` call on a new public method/constructor is a
review blocker, not a style nit.

## Comments

Minimal. A comment is only for a non-obvious business rule a reader could
not infer from the code. Never a comment that restates what the next
line does.

## Checkstyle

`src/main/resources/checkstyle/checkstyle.xml` — run `mvn checkstyle:check`
before considering a change done.

## Conventions by layer — see these skills for full detail

| Skill | Covers |
|---|---|
| `entity-and-liquibase-conventions` | JPA entities, audit fields, Liquibase changesets |
| `repository-conventions` | `I<Domain>Repository` + impl + projections |
| `service-layer-conventions` | Operation services, validators, ResponseCreatorService, mapper, dto vs model |
| `controller-conventions` | REST controller rules, status codes, the `final`-method gotcha |
| `exception-handling-conventions` | Exception types, `GlobalExceptionHandler`, status mapping |
| `testing-conventions` | JUnit/Mockito/AssertJ style, coverage expectations |
| `inter-service-propagation` | Inbound auth filters, `IScopeContext`, outbound `Sqa-*` headers |
| `proxy-bff-conventions` | Orchestration/BFF modules with no local persistence |
| `domain-events-conventions` | Optional Kafka local-event + AFTER_COMMIT pattern |

These auto-trigger by description when you're working on that layer; you
can also invoke one directly (`/skill-name`) or just ask for it.

## Agent behavior

This section applies to every agent/subagent/skill working in this repo.
Subagents don't automatically inherit this file the way the main session
does — each agent's own prompt must tell it to read `CLAUDE.md` in full
before doing anything else, plus the specific skill file(s) for the
layer(s) it touches (subagents can't invoke skills themselves — they
`Read` the `.claude/skills/<name>/SKILL.md` file directly instead). If
you're writing a new agent, include that instruction explicitly rather
than assuming it.

- **Language**: respond to the user in Bosnian — explanations, status
  updates, questions, review reports, final summaries. Code, Javadoc,
  comments, and commit messages stay in English, as elsewhere in this
  file.
- **Editing**: agents here may edit files directly (`Write`/`Edit`) —
  print-the-diff-and-ask-permission policies from other, stricter repos
  do not apply here unless this project later adopts one explicitly.
- **Git/infra**: no agent runs `git commit`, `git push`, or
  Docker/infra-destroying commands without being explicitly asked.
