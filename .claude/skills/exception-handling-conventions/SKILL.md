---
name: exception-handling-conventions
description: Use when creating or reviewing exception types, the GlobalExceptionHandler, or any code that throws/catches a business or validation error in this repo.
---

Full detail for exception handling, referenced by `CLAUDE.md`. Read
`CLAUDE.md` first for the placeholders and the overall map.

## Exceptions

- `EntityNotFoundException` → 404, `BusinessException` (carries a list of
  messages) → 422, both thrown from the service layer, both handled
  centrally in `GlobalExceptionHandler` (`@ControllerAdvice`), never
  caught and swallowed in a Controller or Service.
- `IllegalArgumentException` (what `Check.*` throws) → 400, and
  `MethodArgumentNotValidException` (bean validation) → 400, also handled
  there.
- **Every** handler logs via SLF4J before building the response — all of
  them, including 404/422, no exceptions to this. (Some real reference
  services skip logging on the 404/422 paths; that is a gap, not a
  pattern to repeat here.)
- `AuthenticationException`/`AccessDeniedException` are re-thrown from the
  handler, not swallowed, so Spring Security's own entry point/access-denied
  handler responds instead.
