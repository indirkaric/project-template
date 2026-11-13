---
name: testing-conventions
description: Use when writing, completing, or fixing unit tests for a Service, Validator, Mapper, Controller, or client wrapper class in this repo.
---

- JUnit 5 with `@ExtendWith(MockitoExtension.class)`, `@Mock` per collaborator, and the class under
  test built by hand in `@BeforeEach void setUp()`. Never `@InjectMocks`.
- AssertJ only (`assertThat`, `assertThatThrownBy`, `assertThatCode`), not JUnit assertions.
- `<ClassUnderTest>Test` in the same package under `src/test/java`. Method names are
  `should<Behavior>()`.
- Servlet code: `MockHttpServletRequest`/`Response`/`MockFilterChain`. Client wrappers: a `WebClient`
  built with `.exchangeFunction(...)` that captures the `ClientRequest`, and an assertion on URL,
  body and `Sqa-Client-Id`. `BaseClientTest` and `WebClientConfigTest` show both patterns.
- Cover the happy path, every validator rejection, every `EntityNotFoundException` and
  `BusinessException` branch, and every `Check` failure.
- Mock only direct collaborators and verify what matters. Use an `ArgumentCaptor` when the passed
  value is the point.
- Checkstyle covers tests too, with `final` parameters and the blank line after `}`.
- Run `mvn -o test -Dtest=<Class>`, then `mvn -o verify` for the gate. If coverage is short, add the
  missing branches. Never add exclusions.
