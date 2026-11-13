---
name: service-layer-conventions
description: Use when creating or modifying the business/API layer above the repository — operation services split by verb, validators, the ResponseCreatorService seam, the MapStruct mapper, or deciding dto vs model placement.
---

Full detail for everything between the repository and the controller,
referenced by `CLAUDE.md`'s package layout. Read `CLAUDE.md` first for the
placeholders and the overall map.

## Service layer: split by operation, not by entity

Do not write one `{{Domain}}Service` with `create/update/delete/get`
methods. Split into:

- `ICreate{{Domain}}Service` / `Create{{Domain}}Service` — write, returns
  the new entityId
- `IModify{{Domain}}Service` / `Modify{{Domain}}Service` — update / status
  transitions
- `IDelete{{Domain}}Service` / `Delete{{Domain}}Service` — delete
- `I{{Domain}}Service` / `{{Domain}}Service` — read-only queries,
  `@Transactional(readOnly = true)`

They live in `service/<domain>/` (interfaces) and `service/<domain>/impl/`
(implementations) at the root, not inside `modules/`.

Each is `@Service`, constructor injection only, with `private final`
fields, every injected dependency validated with
`Check.isNotNull(dep, "depName")` inside the constructor, and documented
with a full `/** ... @param ... */` Javadoc block naming every parameter.
Write services are `@Transactional` at the class level; read services are
`@Transactional(readOnly = true)`. The **class itself is never `final`**:
`@Transactional` is applied through a CGLIB proxy that must subclass it,
exactly like the controller-method rule in `controller-conventions`.

## Validators

- Live in `service/validator/`, one interface+impl per write operation
  (`ICreate{{Domain}}ServiceValidator`, `IModify{{Domain}}ServiceValidator`).
- Shared field-level rules go in a `Base{{Domain}}DataValidator` that
  concrete validators extend — don't copy-paste the same rule into two
  validators.
- Injected into the operation service and called as the FIRST line of the
  method body, before any other logic.
- Violations throw `BusinessException` (never `IllegalStateException` or a
  plain `RuntimeException`).

## ResponseCreatorService: the only thing the Controller calls

`modules/<domain>/service/I<Verb><Domain>ResponseCreatorService` (impl in
`modules/<domain>/service/impl/`) wires one
or two core services together with the mapper and returns the final API
response model. Example: `Create{{Domain}}ResponseCreatorService` calls
`ICreate{{Domain}}Service.save(request)` to get an `entityId`, then
`I{{Domain}}ResponseCreatorService.create(entityId)` to build the full
`{{Domain}}Response`. This seam is what keeps the Controller free of
persistence concerns.

This seam applies **only where the module has real local persistence**.
For a pure pass-through/proxy module, see the `proxy-bff-conventions`
skill — the operation Service calls the downstream client(s) directly
instead.

## Mapper (MapStruct)

- Root-level `mapper/I<Domain>Mapper.java`, shared by every module that
  needs it — never a `mapper/` package inside `modules/<domain>/`.
- One interface per domain: `@Mapper(componentModel = "spring",
  unmappedTargetPolicy = ReportingPolicy.IGNORE)`.
- Every method has a short Javadoc (`@param`/`@return`).
- Derived/computed fields use `@Mapping(target = "...", expression =
  "java(...)")`, delegating to a small static helper — never duplicate the
  computation inline in both the service and the mapper.
- The mapper only ever touches `model`/`dto` types on the way out and
  `entity`/`projection` types on the way in — it is the one place allowed
  to know about both.

## DTO vs model

- `modules/<domain>/model/` = records exposed over the API
  (`Create{{Domain}}Request`, `{{Domain}}Response`,
  `{{Domain}}ListResponse`) — validated with `jakarta.validation`
  annotations, bound with `@Valid @RequestBody`.
- Root-level `dto/` = internal objects built off projections
  (`{{Domain}}DTO`), used
  only between the repository/mapper and the ResponseCreatorService. Never
  return a `dto` type from a Controller (the one exception is the shared
  `dto/response/EntityCreatedResponse`), and never return an `entity` from
  anywhere above the repository.
