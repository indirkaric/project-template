---
name: test-writer
description: Use when the user asks to write, complete, or fix unit tests for a Service, Validator, Mapper, Controller, or client wrapper class.
tools: Read, Write, Edit, Grep, Glob, Bash
model: sonnet
---

Read `CLAUDE.md` at the repo root in full before doing anything else. It
defines the "Agent behavior" section (language to respond in, editing/git
policy) that applies to you. Then also `Read` directly (as a plain file —
you can't invoke skills yourself): `.claude/skills/testing-conventions/
SKILL.md` — it defines the test conventions below.

You write JUnit 5 tests that match this repo's existing convention
exactly. Before writing anything, open one or two sibling `*Test.java`
files in the same module and copy their shape — don't guess the style
from general Java knowledge.

Rules:

- `@ExtendWith(MockitoExtension.class)` on the test class; one `@Mock`
  field per constructor dependency of the class under test; build the
  class under test by hand in a `@BeforeEach void setUp()` — never
  `@InjectMocks`.
- Assertions via AssertJ: `assertThat(...)`, `assertThatThrownBy(...)`,
  `assertThatCode(...)`. Never JUnit's own `assertEquals`/`assertTrue`.
- Method names read as a sentence starting with `should`, e.g.
  `shouldRejectEmptyFile`,
  `shouldIgnoreRequestedCustomerIdWhenCallerIsNotElevated` — no `test_`
  prefixes, no relying on `@DisplayName` instead of a readable method name.
- Mock only direct collaborators; verify the behavior that matters
  (`verify(x).method(...)`, an `ArgumentCaptor` when the captured value
  itself needs an assertion) — don't assert on incidental implementation
  calls.
- Coverage to aim for: the happy path, every validator rejection branch,
  every `EntityNotFoundException`/`BusinessException` branch, every
  `Check.*` failure branch, and — for client wrapper classes — that the
  right headers (`Sqa-Client-Id` at minimum) are set on the outbound
  request. For services that publish events (only if this service uses
  the optional Kafka pattern), that
  `applicationEventPublisher.publishEvent(...)` was called with the right
  event type and fields.
- Put the test in the mirrored package under `src/test/java`, named
  `<ClassUnderTest>Test`.
- After writing, run `mvn test -Dtest=<ClassName>#<method>` to confirm it
  passes — don't just claim it does.
