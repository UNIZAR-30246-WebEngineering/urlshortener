# Feature: Create short URL

## Decision

**Owner module**: `links` | **Event-coupled**: Y (`ShortUrlCreatedEvent`)

Accept a target URL, validate it, hash it, persist a `ShortUrl`, and publish `ShortUrlCreatedEvent` so other modules can react.

**Traps**: invalid URLs must be rejected before hashing; hash collisions need a retry strategy; duplicate URLs should return the existing hash, not a new one.

**Technology hints**: `CommonsUrlValidator`; `MurmurHashGenerator`; atomic HQL insert-if-absent on `short_url` (see ADR 0002); Spring Modulith application event.

---

- **Weight:** 0 (seed — not in agreement)
- **Event-coupled:** Y

---

## Later

- **ADR:** [`docs/adr/0001-modulith-and-hexagon.md`](../adr/0001-modulith-and-hexagon.md), [`docs/adr/0002-atomic-upserts.md`](../adr/0002-atomic-upserts.md)

### Acceptance criteria

- [x] `POST /api/link` with form `url=` returns **201** and JSON `hash` + location — covered by `LinkFlowTests`
- [ ] Invalid URL → **400**
- [x] Publishes `ShortUrlCreatedEvent` — implied by stats bootstrap in `LinkFlowTests` + `LinkStatsListener`
- [x] Same URL (also concurrently) returns the existing hash — `CreateShortUrlConcurrencyTests`
- [x] Hash collision → retried with salted rehash (`hash("$url#n")`, 3 attempts), stable on repeat; all taken → **409**, target never overwritten — `CreateShortUrlConcurrencyTests`

### Scale evidence

- [x] Level 3 — in-process events via `ShortUrlCreatedEvent`
- [ ] Run data of failure scenarios
- [ ] Run data of load scenarios

### Qualities (self-assessed)

| Quality | Assessed | How to test |
| --- | --- | --- |
| **Correctness** | **0** | No test for invalid URL. |
| **Scalability** | **0** | No run data available. |
| **Engineering** | **1.5** | ADR + `ModularityTests` green; `./gradlew check` green; AI disclosure present. |

**Indicative total:** 0 + 0 + 1.5 = **1.5 / 10**.

### AI disclosure

- **Tools / skills:** Model selected by Cursor Auto.
- **Used for:** Adapting the seed feature from 25-26 course code to the 26-27 course code.
- **Cursor agent (Claude):** atomic insert-if-absent, salted-rehash collision retry with 409 on exhaustion, `CreateShortUrlConcurrencyTests` (shown to fail against the previous `save`).
- **Human-reviewed:** The instructor reviewed the code and verified the correctness of the adaptation.
