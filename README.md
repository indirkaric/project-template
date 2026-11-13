# project-template

Starting point for a new nSight/Genie Spring Boot 3.2 / Java 17 microservice:
a verified code skeleton, the architecture rules, and the Claude agents and skills that build on
them. `identity` and `artifact` are the reference services. They were distilled into `skeleton/`
once (provenance: [REFERENCE.md](REFERENCE.md)), so nothing here reads them again.

## New service — let Claude do it

Open Claude Code in this directory and say, e.g.:

> napravi mi dva mikroservisa, `invoice` (vlastita baza, zove identity) i `report` (proxy nad
> artifactom), i vodi se identity i artifact servisom

or `/new-microservice invoice:persistence:identity report:proxy:artifact`. The skill confirms
the spec once and then:

1. runs `scripts/new_service.py` per service. That creates `../<name>/` from `skeleton/`, fills
   CLAUDE.md + `.claude/`, wires the downstream base URIs and client ids, registers the port in
   `platform.md`, and writes `PLATFORM_CHANGES.md`;
2. builds the first module (`service-creator` / `proxy-service-creator`);
3. writes the tests (`test-writer`) and runs `mvn -o verify`;
4. reviews with `code-reviewer` + `integration-flow-auditor`;
5. reports, and offers to apply `PLATFORM_CHANGES.md` (DB, compose, hosts, callee allowlists,
   workspace docs). That file edits other repos, so it is applied only on your yes.

No git, no `docker`, no edits to existing repos without asking.

## New service — by hand

```bash
python3 scripts/new_service.py --name invoice --role persistence --domain Invoice --calls identity
```

```bash
cd ../invoice && mvn -o verify
```

`--help` lists every option: `--port`, `--root-package`, `--db`, `--api-prefix`, `--coverage`,
`--target`, `--no-register`.

## Layout

| Path | What | Copied into a new service |
|---|---|---|
| `CLAUDE.md` | architecture rules, always loaded | yes, filled |
| `.claude/skills/*` | one skill per layer + module checklists | yes |
| `.claude/skills/inter-service-propagation/platform.md` | registry of every service: host, port, base URI, caller policy | yes |
| `.claude/agents/*` | creators, test writer, reviewers | yes |
| `.claude/skills/new-microservice` | orchestration | no, template-only |
| `skeleton/` | infrastructure code + tests + build/CI files, with placeholders | yes, as the code |
| `scripts/new_service.py` | instantiation | no |
| `REFERENCE.md` | what came from identity/artifact and why it changed | no |

## Changing the skeleton

Edit `skeleton/`, then prove it still builds for both roles:

```bash
python3 scripts/new_service.py --name sandbox --role persistence --domain Demo --calls identity --no-register --target /tmp/sandbox && (cd /tmp/sandbox && mvn -o verify)
```

Repeat with `--role proxy`, and record the change in `REFERENCE.md`.
