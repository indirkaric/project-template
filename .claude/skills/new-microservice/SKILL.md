---
name: new-microservice
description: Create one or more new Spring Boot microservices from zero with identity and artifact as the reference — e.g. "napravi mi dva mikroservisa i vodi se identity i artifact servisom", "create service X like artifact". Template-only; run from project-template.
argument-hint: "<name>:<persistence|proxy>[:<calls>] ... — e.g. invoice:persistence:identity report:proxy:artifact"
---

`identity` and `artifact` are already distilled into `skeleton/`. Their provenance is in
`REFERENCE.md`. `scripts/new_service.py` copies the skeleton deterministically, so never read
those repos and never write infrastructure by hand. You orchestrate and agents write. Talk to the
user in Bosnian.

## 1. Spec

Per service, from the request:

| Field | Default |
|---|---|
| name | kebab-case from the request |
| role | `persistence`, or `proxy` if it only combines other services |
| calls | downstream services the request names (names from `platform.md`). "vodi se X" means copy X's style, not call X |
| domain | PascalCase first domain; the service name if none is given |
| operations | create / get / list / modify / delete |
| port, package, db, prefix, coverage | script defaults: next free port from `platform.md`, `com.sqa.nsight.genie.<name>`, `<name>`, `<name>`, `0.80` |

Show one table row per service and confirm once with `AskUserQuestion`, naming only the inferred
fields. A browser-facing service, which needs PAT auth, is outside the skeleton: ask how to
proceed.

## 2. Create

Run the script per service, one after another, because each run registers its port:

```
python3 scripts/new_service.py --name <n> --role <r> --domain <D> [--calls a,b] [--port …]
```

On a non-zero exit, show the message and stop for that service.

## 3. Module, tests, review

Agents work in different directories, so launch all services' agents of a step in **one message**.
Each prompt begins `Repo root: <target>. Read <target>/CLAUDE.md first; paths are relative to it;
run Maven as cd <target> && mvn -o …`, followed by the domain, the operations and the user's
field/endpoint wishes.

1. `service-creator` for `persistence`, `proxy-service-creator` for `proxy`. Answer each
   `## TREBA ODLUKA` with the user, then relaunch.
2. `test-writer` with that service's `## KLASE ZA TESTOVE`. Then run `mvn -o verify` yourself.
   If it fails, send the output back to the same agent (`SendMessage`), at most twice.
3. `code-reviewer` + `integration-flow-auditor`. Send a `BLOCK` back to the writing agent and
   review once more.

## 4. Report

Per service: path, port, role, endpoints, `mvn -o verify` result, verdicts. Then what is in
`<target>/PLATFORM_CHANGES.md` (DB, compose, hosts, callee allowlists, workspace docs, CI), as a
short list with nothing applied yet, and what wasn't verified: the app was not started against
the local stack, there is no git repo, nothing is committed. Offer to apply `PLATFORM_CHANGES.md`.
It edits other repos, so it needs the user's yes.
