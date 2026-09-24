# Feature: Create short URL

## Decision

**Owner module**: `links` | **Event-coupled**: Y (`ShortUrlCreated`)

Accept a target URL, validate it, hash it, persist a `ShortUrl`, and publish `ShortUrlCreated` so other modules can react.

**Traps**: invalid URLs must be rejected before hashing; hash collisions need a retry strategy; duplicate URLs should return the existing hash, not a new one.

**Technology hints**: `CommonsUrlValidator`; `MurmurHashGenerator`; Spring Modulith application event.

---

- **Weight:** 0 (seed — not in agreement)
- **Event-coupled:** Y

---

## Later

- **ADR:** [`docs/adr/0001-modulith-and-hexagon.md`](../adr/0001-modulith-and-hexagon.md)

### Acceptance criteria

- [x] `POST /api/link` with form `url=` returns **201** and JSON `hash` + location — covered by `LinkFlowTests`
- [ ] Invalid URL → **400**
- [x] Publishes `ShortUrlCreated` — implied by stats bootstrap in `LinkFlowTests` + `LinkStatsListener`

### Scale evidence

- [x] Level 3 — in-process events via `ShortUrlCreated`
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
- **Human-reviewed:** The instructor reviewed the code and partially verified the correctness of the adaptation.
