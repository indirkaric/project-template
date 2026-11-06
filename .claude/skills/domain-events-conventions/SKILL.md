---
name: domain-events-conventions
description: Use only if this service needs async eventing (Kafka) — covers the local-event + AFTER_COMMIT bridge pattern. Most services in this platform don't need this; their inter-service communication is synchronous (see inter-service-propagation skill).
---

Full detail for the optional Kafka domain-event pattern, referenced by
`CLAUDE.md`'s package layout. Read `CLAUDE.md` first for the placeholders
and the overall map.

## Domain events (Kafka) — optional, additive

None of the real services this template is based on use Kafka; their
inter-service communication is synchronous (WebClient + the
`inter-service-propagation` skill's header propagation). Only add this
pattern if {{ServiceName}} specifically needs async eventing:

- A service that changes state publishes a **local** Spring event —
  `applicationEventPublisher.publishEvent(new DomainChangeEvent(entityType,
  action, entityId, performedBy, payload))` — inside the `@Transactional`
  method, never a direct Kafka call.
- Exactly one `@Component` per service (`DomainEventListener` or
  `DomainChangeEventListener` — reuse whichever already exists, never add
  a second one) has the single `@TransactionalEventListener(phase =
  TransactionPhase.AFTER_COMMIT)` method that turns the local event into
  a real Kafka publish via `IDomainEventPublisher.publish(...)`. This is
  the ONLY class allowed to depend on `IDomainEventPublisher`/Kafka.
- That listener swallows and logs `RuntimeException` — a Kafka outage
  must never surface as an error to the original caller, whose
  transaction already committed.
- A consumer-side service checks an idempotency key (e.g.
  `existsByEventId`) before writing, so a redelivery after a
  crash/rebalance is safe.
