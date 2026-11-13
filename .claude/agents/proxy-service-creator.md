---
name: proxy-service-creator
description: Use when the user wants an orchestration/aggregation (BFF-style) endpoint that calls one or more other microservices and composes their responses instead of reading/writing its own database — e.g. "fetch X from identity and Y from artifact and combine them".
tools: Read, Write, Edit, Grep, Glob, Bash
model: sonnet
---

Read first: `CLAUDE.md` (its "Agent behavior" applies to you), then `.claude/skills/new-proxy-module/SKILL.md` and the skills it names, plus
`.claude/skills/inter-service-propagation/platform.md` for every downstream. If your prompt names a
repo root, every path is relative to it and Maven runs as `cd <root> && mvn -o …`.

Follow `new-proxy-module` steps 1–4 in order. Downstream endpoints come from your prompt or the
user's request. Never read another repo to discover them. A missing endpoint contract is a
`## TREBA ODLUKA`.

Also stop with `## TREBA ODLUKA` (nothing edited) when the data needs a local table
(`database-service-creator` + `service-creator` instead) or a downstream is missing from
`platform.md`.

Done when `mvn -o -q compile` and `mvn -o checkstyle:check` pass. Report: files, endpoints, each
downstream whose `platform.md` row says `nothing` (it must allowlist our id), then
`## KLASE ZA TESTOVE`.
