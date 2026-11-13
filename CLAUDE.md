# CLAUDE.md — {{ServiceName}}

> **Fill these in** — only when setting this template up by hand; `scripts/new_service.py` fills
> them and removes this note: `{{ServiceName}}`, `{{service-name}}`, `{{Domain}}`/`{{domain}}`,
> `{{port}}`, `{{schema}}`, `{{api-prefix}}`, `{{coverage}}`.

Spring Boot 3.2 / Java 17 microservice, one per repo. The architecture below is
**non-negotiable**; each layer's detail lives in one skill (table at the end), loaded only when
that layer is touched. Where the reference services (`identity`, `artifact`) disagree, the
stricter rule wins — deviate only after calling it out.

## Package layout

Entities, repositories, mappers and DTOs are root-level and shared; a module holds exactly three
things. Every package has a `package-info.java`. Spell `config/` and `annotations/` this way.

```
entity/<Entity>.java  entity/common/  entity/listener/BaseEntityListener
repository/I<Domain>Repository + impl/<Domain>Repository + projection/<Domain>Projection
mapper/I<Domain>Mapper                         MapStruct
dto/<Domain>DTO  dto/response/EntityCreatedResponse
service/<domain>/I<Verb><Domain>Service + impl/     ONE interface per operation
service/validator/I<Verb><Domain>ServiceValidator + impl/   + Base<Domain>DataValidator
service/security/  service/bean/  service/audit/
client/BaseClient  client/<downstream>/<Downstream>Client
config/  security/  exception/  constants/  util/  db/config|model|constants
modules/<domain>/
  <Domain>Controller
  model/                                       API request/response records
  service/I<Verb><Domain>ResponseCreatorService + impl/
event/                                         optional, see domain-events-conventions
```

The Controller depends only on `*ResponseCreatorService` (or a bare operation service for a
body-less operation like delete) — never on a repository, entity, mapper or internal DTO. A
module without local persistence follows `proxy-bff-conventions`.

## Non-negotiable

- **`Check`** (`Check.isNotNull`/`isNotEmpty`) first in every constructor (per dependency) and
  every public method (per parameter) — controllers, services, validators, clients, filters.
  A missing call is a review blocker.
- **Nothing proxied is `final`**: controller methods (`@PreAuthorize`) and `@Transactional`
  classes are CGLIB-proxied; `final` breaks them on live traffic only.
- **Tests ship with the change**; `mvn -o verify` runs them plus the JaCoCo gate
  (`{{coverage}}` instruction coverage) and checkstyle.
- **Checkstyle** (`src/main/resources/checkstyle/`, from identity — the stricter one): Javadoc
  on public members, `Designed for extension.` in the Javadoc of overridable public methods,
  `final` parameters, 60-line methods, 120-char lines, a blank line after a closing `}` unless
  `else`/`catch`/`finally`/`}` follows, no star imports, no magic numbers.
- **Comments** only for a non-obvious reason, never narrating the next line.

## Skills

| Skill | Covers |
|---|---|
| `entity-and-liquibase-conventions` | entities, audit fields, Liquibase, datasource routing |
| `repository-conventions` | repository contract, impl, projections |
| `service-layer-conventions` | operation services, validators, ResponseCreatorService, mapper, dto vs model |
| `controller-conventions` | endpoints, status codes, response shapes |
| `exception-handling-conventions` | exception types and HTTP mapping |
| `testing-conventions` | test style and coverage |
| `inter-service-propagation` | auth filters, `IScopeContext`, outbound headers, `platform.md` registry |
| `proxy-bff-conventions` | modules that only compose other services |
| `domain-events-conventions` | optional Kafka pattern |
| `new-crud-module` / `new-proxy-module` | ordered checklists for a new module |

## Agent behavior

- **Language**: everything said to the user is Bosnian; code, Javadoc, comments, commits English.
- **Subagents** don't inherit this file: every agent prompt starts by reading it plus the skill
  files it needs (`Read .claude/skills/<name>/SKILL.md` — subagents can't invoke skills).
- **Other services** are known only from `.claude/skills/inter-service-propagation/platform.md`.
  Never read a sibling repo; if the registry lacks something, ask.
- **Git/infra**: no commit, push, `docker` or stack changes unless the user asks.
- **Subagents can't ask the user.** They end with one block and the main session acts on it:
  - `## TREBA ODLUKA` — blocked: the question, 2–3 options with consequences; nothing edited.
  - `## KLASE ZA TESTOVE` — every code-writing agent: `src/main/java/…/X.java — what to cover`.
  - `## VERDIKT: APPROVE | WARNING | BLOCK` — last line of every review agent (`BLOCK` = a
    non-negotiable rule broken, `WARNING` = style/Javadoc/missing test, `APPROVE` = clean).
