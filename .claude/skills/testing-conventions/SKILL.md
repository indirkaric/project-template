---
name: testing-conventions
description: Use when writing, completing, or fixing unit tests for a Service, Validator, Mapper, Controller, or client wrapper class in this repo.
---

Full detail for test style, referenced by `CLAUDE.md`. Read `CLAUDE.md`
first for the placeholders and the overall map.

## Tests

- JUnit 5 + Mockito (`@ExtendWith(MockitoExtension.class)`, `@Mock` fields,
  dependencies wired manually in `@BeforeEach void setUp()` — no
  `@InjectMocks`) + AssertJ (`assertThat`, `assertThatThrownBy`,
  `assertThatCode`).
- Test class = `<ClassUnderTest>Test`, same package as the class under
  test, under `src/test/java`.
- Method names read as a sentence: `should<ExpectedBehavior>()`, e.g.
  `shouldIgnoreRequestedCustomerIdWhenCallerIsNotElevated`.
- A new `Service`/`Validator`/`Controller`/client wrapper ships with its
  test in the same change — not "tests later."
- Fixed rule, by design: this is stricter than some real reference
  services (which use `@InjectMocks` + plain JUnit assertions) — don't
  follow their example here.
- Coverage to aim for: the happy path, every validator rejection branch,
  every `EntityNotFoundException`/`BusinessException` branch, every
  `Check.*` failure branch, and — for client wrapper classes — that the
  right headers (`Sqa-Client-Id` at minimum) are set on the outbound
  request.
- After writing, run `mvn test -Dtest=<ClassName>#<method>` to confirm it
  passes — don't just claim it does.
