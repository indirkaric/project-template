---
name: new-proxy-module
description: Ordered checklist for a new orchestration/aggregation (BFF-style) endpoint that calls other microservices instead of local persistence. Use when starting one from zero in the main conversation; proxy-service-creator follows the same steps as a subagent.
---

Rules per step: `proxy-bff-conventions`, `inter-service-propagation`, `service-layer-conventions`,
`controller-conventions`. If the data needs a local table, use `new-crud-module` instead.

1. Per downstream: the `platform.md` row exists → `client.<name>.base-uri` in `application.yaml` →
   a `<NAME>_CLIENT` constant (`<ThisService>.<Downstream>`) → the
   `client/<name>/<Name>Client extends BaseClient` wrapper, with only the methods this module needs.
   If the row says `nothing`, note the needed allowlist in the report.
2. `modules/<domain>/model/` response records.
3. `service/<domain>/I<Verb><Domain>Service` + impl: call order, composition, partial-failure
   handling.
4. `modules/<domain>/<Domain>Controller`, delegating to that service.
5. `mvn -o -q compile` and `mvn -o checkstyle:check`, then tests for steps 1 and 3 and `mvn -o verify`.
