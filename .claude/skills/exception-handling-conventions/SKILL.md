---
name: exception-handling-conventions
description: Use when creating or reviewing exception types, the GlobalExceptionHandler, or any code that throws/catches a business or validation error in this repo.
---

`exception/GlobalExceptionHandler` (`@ControllerAdvice`) is the only place an exception becomes a
response. Services throw. Controllers and services never catch and swallow.

| Thrown | Status |
|---|---|
| `EntityNotFoundException` (lookup found nothing) | 404 |
| `BusinessException` (one message or a list; validators throw it) | 422 |
| `IllegalArgumentException` (`Check.*`) | 400 |
| `MethodArgumentNotValidException` (`@Valid`), as `field: message` | 400 |
| Spring MVC's own `ErrorResponse` exceptions (unknown path, wrong method, …) | their own status |
| `AuthenticationException` / `AccessDeniedException` | re-thrown, so Spring Security answers |
| anything else | 500, generic message |

- Every handler logs through SLF4J before it responds. Client errors log at `warn`, a 500 at `error`.
- Never throw `RuntimeException` or `IllegalStateException` for a business rule.
- A new exception type gets its own handler and a test in `GlobalExceptionHandlerTest`.
