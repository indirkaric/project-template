#!/usr/bin/env python3
"""Instantiate a new microservice repo from ../skeleton and this template's Claude setup.

Deterministic on purpose: the infrastructure is copied, never re-generated, so every
service starts from the same verified code. Usage:

    python3 scripts/new_service.py --name invoice --role persistence --domain Invoice \
        [--calls identity,artifact] [--port 50036] [--root-package com.sqa.nsight.genie.invoice] \
        [--db invoice] [--api-prefix invoice] [--coverage 0.80] [--target ../invoice]

Other services are known only through .claude/skills/inter-service-propagation/platform.md;
the script never reads a sibling repo. Changes needed outside the new repo are written to
<target>/PLATFORM_CHANGES.md, not applied.
"""
import argparse
import re
import shutil
import sys
from pathlib import Path

TEMPLATE = Path(__file__).resolve().parent.parent
SKELETON = TEMPLATE / "skeleton"
REGISTRY = TEMPLATE / ".claude/skills/inter-service-propagation/platform.md"
FIRST_PORT = 50036

# Paths (relative to the instantiated repo) that only a persistence service keeps.
PERSISTENCE_ONLY = [
    "src/main/java/ROOT/db",
    "src/main/java/ROOT/entity",
    "src/main/java/ROOT/service/audit",
    "src/test/java/ROOT/service/audit",
    "src/main/resources/liquibase",
]
# Template-only Claude files that must not land in a generated service.
TEMPLATE_ONLY = [
    ".claude/skills/new-microservice",
    ".claude/settings.local.json",
]
BLOCK_PATTERNS = [
    re.compile(r"[ \t]*<!-- @persistence:start -->.*?<!-- @persistence:end -->[ \t]*\n", re.S),
    re.compile(r"# @persistence:start\n.*?# @persistence:end\n", re.S),
]
MARKERS = re.compile(r"[ \t]*(<!-- @persistence:(start|end) -->|# @persistence:(start|end))[ \t]*\n")
NAME = re.compile(r"^[a-z][a-z0-9]*(-[a-z0-9]+)*$")


def parse_args():
    parser = argparse.ArgumentParser(description=__doc__, formatter_class=argparse.RawDescriptionHelpFormatter)
    parser.add_argument("--name", required=True, help="kebab-case service name, e.g. invoice")
    parser.add_argument("--port", type=int, help="default: lowest free port from the registry")
    parser.add_argument("--calls", default="", help="comma-separated downstream services from the registry")
    parser.add_argument("--role", required=True, choices=["persistence", "proxy"])
    parser.add_argument("--domain", required=True, help="PascalCase first domain, e.g. Invoice")
    parser.add_argument("--root-package")
    parser.add_argument("--db", help="database and schema name, default: name with underscores")
    parser.add_argument("--api-prefix")
    parser.add_argument("--coverage", default="0.80")
    parser.add_argument("--target")
    parser.add_argument("--no-register", action="store_true", help="don't add the service to the registry (dry runs)")
    args = parser.parse_args()

    if not NAME.match(args.name):
        sys.exit(f"--name must be kebab-case: {args.name}")
    if not re.match(r"^[A-Z][A-Za-z0-9]*$", args.domain):
        sys.exit(f"--domain must be PascalCase: {args.domain}")
    if not re.match(r"^0(\.\d+)?$|^1(\.0+)?$", args.coverage):
        sys.exit(f"--coverage must be between 0 and 1: {args.coverage}")
    snake = args.name.replace("-", "_")
    args.root_package = args.root_package or f"com.sqa.nsight.genie.{snake}"
    if not re.match(r"^[a-z][a-z0-9_]*(\.[a-z][a-z0-9_]*)+$", args.root_package):
        sys.exit(f"--root-package is not a valid package: {args.root_package}")
    args.db = args.db or snake
    args.api_prefix = args.api_prefix or args.name
    args.target = Path(args.target or TEMPLATE.parent / args.name).resolve()
    args.calls = [c.strip() for c in args.calls.split(",") if c.strip()]
    return args


def read_registry():
    """Rows of the platform table plus the extra taken alias names."""
    text = REGISTRY.read_text(encoding="utf-8")
    rows = {}
    for line in text.splitlines():
        cells = [c.strip() for c in line.strip().strip("|").split("|")]
        if len(cells) == 6 and cells[2].isdigit():
            rows[cells[0]] = {"alias": cells[1], "port": int(cells[2]), "base": cells[3], "unknown": cells[4]}
    taken = re.search(r"the name is taken\): (.*?)\.\n", text, re.S)
    names = set(rows) | {n.strip() for n in taken.group(1).replace("\n", " ").split(",")} if taken else set(rows)
    return rows, names


def check_against_registry(args, rows, names):
    if args.name in names:
        sys.exit(f"Name already taken in {REGISTRY.name}: {args.name}")
    used = {row["port"] for row in rows.values()}
    if args.port is None:
        args.port = next(p for p in range(FIRST_PORT, 65536) if p not in used)
    if not 1024 < args.port < 65536 or args.port in used:
        sys.exit(f"Port not available: {args.port}")
    for callee in args.calls:
        if callee not in rows or not rows[callee]["base"].startswith("http"):
            sys.exit(f"--calls: {callee} has no base URI in {REGISTRY.name}")


def label(name):
    return "-".join(part.capitalize() for part in name.split("-"))


def wire_calls(args, values, rows):
    """Base URIs in application.yaml and outbound client ids in Constants."""
    if not args.calls:
        return
    yaml = args.target / "src/main/resources/application.yaml"
    entries = "".join(f'  {c}:\n    base-uri: "{rows[c]["base"]}"\n' for c in args.calls)
    text = yaml.read_text(encoding="utf-8")
    yaml.write_text(re.sub(r"# One entry per downstream.*?client: \{\}\n", "client:\n" + entries, text, flags=re.S),
                    encoding="utf-8")
    constants = args.target / "src/main/java" / values["{{rootPath}}"] / "constants/Constants.java"
    ids = "".join(f'    public static final String {c.upper().replace("-", "_")}_CLIENT = '
                  f'"{values["{{ServiceName}}"]}.{label(c)}";\n\n' for c in args.calls)
    text = constants.read_text(encoding="utf-8")
    anchor = "    /** Inbound caller ids"
    constants.write_text(text.replace(anchor, "    /** Outbound caller ids, one per downstream. */\n" + ids + anchor, 1),
                         encoding="utf-8")


def register(args, values):
    base = f"http://{args.name}.nsight.local:{args.port}/{args.api_prefix}/"
    row = f"| {args.name} | {args.name}.nsight.local | {args.port} | {base} | nothing | created by new_service.py |"
    text = REGISTRY.read_text(encoding="utf-8")
    last = [line for line in text.splitlines() if line.startswith("| ") and "|---" not in line][-1]
    REGISTRY.write_text(text.replace(last, last + "\n" + row, 1), encoding="utf-8")


def platform_changes(args, values, rows):
    name, port, svc = args.name, args.port, values["{{ServiceName}}"]
    out = [f"# Changes outside `{name}` — not applied\n",
           "Apply only with the user's yes; each block names its file.\n"]
    if args.role == "persistence":
        out.append(f"""## development/nSight/docker-mounts/postgres/init-scripts/01_create_databases.sql

```sql
CREATE DATABASE "{args.db}";
CREATE ROLE {args.db}_app WITH LOGIN REPLICATION PASSWORD '{args.db}_app' CREATEDB; GRANT postgres to {args.db}_app;
```
""")
    depends = "      db:\n        condition: service_healthy\n" if args.role == "persistence" else ""
    out.append(f"""## development/nSight/docker-compose.yml

```yaml
  {name}-api:
    image: ${{{name.upper().replace("-", "_")}_API_IMAGE:-211125356125.dkr.ecr.eu-west-2.amazonaws.com/genie-{name}:latest}}
    restart: always
    depends_on:
{depends}      auth:
        condition: service_healthy
    networks:
      nsight:
        aliases:
          - {name}.nsight.local
```

## development/nSight/docker-compose.override.yml

```yaml
  {name}-api:
    ports:
      - "{port}:{port}"
    healthcheck:
      test: [ "CMD-SHELL", "wget -q --spider http://localhost:{port}/actuator/health || exit 1" ]
      interval: 30s
      timeout: 10s
      retries: 5
    deploy:
      resources:
        limits:
          cpus: '0.5'
          memory: 1024M
```

## development/nSight/gitlab/setup-hosts.sh — alias list

```
{name}.nsight.local
```
""")
    for callee in args.calls:
        if rows[callee]["unknown"] == "nothing":
            const = f"{svc.upper()}_{callee.upper().replace('-', '_')}_CLIENT"
            out.append(f"""## {callee} — allowlist `{svc}.{label(callee)}`, otherwise every call is a 403

`constants/Constants.java`:
```java
public static final String {const} = "{svc}.{label(callee)}";
```
`security/SqaClientIdHeaderAuthenticationFilter.java`, in `getAuthoritiesBasedOnClient`, grant
`{const}` the authorities the called endpoints require (read the endpoints' `@PreAuthorize`).
""")
    for caller in ("genie-ui-service",):
        out.append(f"""## {caller} — only if it will call `{name}`

`client.{name}.base-uri: "http://{name}.nsight.local:{port}/{args.api_prefix}/"` and a client id
`UI.{svc}`, which `{name}` already trusts (`Constants.TRUSTED_CLIENTS`).
""")
    out.append(f"""## Workspace `CLAUDE.md` — repo map row

```
| `{name}` | 3.2.4 | {port} | `{args.root_package}` | <role in one line> |
```

## Workspace `.claude/rules/java-conventions.md` and `testing.md` — `paths:`

```
  - "{name}/src/main/java/**/*.java"
```
(`testing.md`: `"{name}/src/test/**"`)

## sqa-gitlab/gitlab-templates

`Service/{name}/all-environments.yml` — `.gitlab-ci.yml` includes it and it does not exist yet.

## SonarQube

`sonar-project.properties` has the placeholder key `{name}`; replace it with the key SonarQube issues.
""")
    (args.target / "PLATFORM_CHANGES.md").write_text("\n".join(out), encoding="utf-8")


def placeholders(args):
    pascal = "".join(part.capitalize() for part in args.name.split("-"))
    return {
        "{{rootPackage}}": args.root_package,
        "{{rootPath}}": args.root_package.replace(".", "/"),
        "{{service-name}}": args.name,
        "{{ServiceName}}": pascal,
        "{{db}}": args.db,
        "{{schema}}": args.db,
        "{{port}}": str(args.port),
        "{{api-prefix}}": args.api_prefix,
        "{{coverage}}": args.coverage,
        "{{Domain}}": args.domain,
        "{{domain}}": args.domain[0].lower() + args.domain[1:],
    }


def fill(text, values):
    for key, value in values.items():
        text = text.replace(key, value)
    return text


def service_facts(args, values):
    return f"""
## Service facts

| | |
|---|---|
| Port | `{args.port}` |
| API prefix | `/{args.api_prefix}/v1` |
| Host alias | `{args.name}.nsight.local` |
| Root package | `{args.root_package}` |
| Role | `{args.role}` |
| Database / schema | {"`" + args.db + "` (role `" + args.db + "_app`)" if args.role == "persistence" else "none (proxy)"} |
| Inbound filters | ClientId → RequestId → SqaUserId → Keycloak JWT |
| Inbound callers | `{values["{{ServiceName}}"]}` accepts `Constants.TRUSTED_CLIENTS` only (`UI.{values["{{ServiceName}}"]}`) |
| Outbound clients | {', '.join('`' + c + '`' for c in args.calls) or 'none'} — base URIs in `application.yaml`, ids in `Constants`; each wrapper extends `client/BaseClient` |
| Coverage gate | `{args.coverage}` instruction coverage |

Infrastructure comes from `project-template/skeleton`, itself distilled from `identity`
(build, checkstyle, Dockerfile, CI) and `artifact` (security, scope context, datasource
routing, exceptions, clients). It is already adapted to the rules in this file; don't
re-copy from those repos.
"""


def main():
    args = parse_args()
    if args.target.exists() and any(args.target.iterdir()):
        sys.exit(f"Target is not empty, refusing to overwrite: {args.target}")
    rows, names = read_registry()
    check_against_registry(args, rows, names)
    values = placeholders(args)

    shutil.copytree(SKELETON, args.target, dirs_exist_ok=True)
    shutil.copy2(TEMPLATE / "CLAUDE.md", args.target / "CLAUDE.md")
    shutil.copytree(TEMPLATE / ".claude", args.target / ".claude", dirs_exist_ok=True)

    for rel in TEMPLATE_ONLY + (PERSISTENCE_ONLY if args.role == "proxy" else []):
        path = args.target / rel
        if path.is_dir():
            shutil.rmtree(path)
        elif path.exists():
            path.unlink()

    for path in [p for p in args.target.rglob("*") if p.is_file()]:
        text = path.read_text(encoding="utf-8")
        if args.role == "proxy":
            for pattern in BLOCK_PATTERNS:
                text = pattern.sub("", text)
        text = MARKERS.sub("", fill(text, values))
        if path.name == "CLAUDE.md" and path.parent == args.target:
            text = re.sub(r"\n> \*\*Fill these in.*?\n\n", "\n", text, count=1, flags=re.S)
            text = text.rstrip() + "\n" + service_facts(args, values)
        path.write_text(text, encoding="utf-8")

    for root in ("src/main/java", "src/test/java"):
        src = args.target / root / "ROOT"
        if src.exists():
            dest = args.target / root / values["{{rootPath}}"]
            dest.parent.mkdir(parents=True, exist_ok=True)
            src.rename(dest)
    for path in sorted(args.target.rglob("*{{*"), key=lambda p: len(p.parts), reverse=True):
        path.rename(path.with_name(fill(path.name, values)))

    wire_calls(args, values, rows)

    left = [str(p.relative_to(args.target)) for p in args.target.rglob("*")
            if "{{" in p.name or (p.is_file() and "{{" in p.read_text(encoding="utf-8"))]
    if left:
        sys.exit("Unfilled placeholders in: " + ", ".join(left))

    platform_changes(args, values, rows)
    if not args.no_register:
        register(args, values)
    print(f"Created {args.target} ({args.role}, port {args.port}, package {args.root_package})")
    print(f"Registry: {'skipped' if args.no_register else REGISTRY.relative_to(TEMPLATE)}; outside changes: {args.target / 'PLATFORM_CHANGES.md'}")
    print(f"Next: cd {args.target} && mvn -o verify && mvn -o checkstyle:check")


if __name__ == "__main__":
    main()
