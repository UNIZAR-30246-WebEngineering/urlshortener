# Feature: Link stats

## Decision

**Owner module**: `analytics` | **Event-coupled**: Y (`ShortUrlCreated`, `ClickLogged`)

Maintain per-hash `LinkStats` (not the raw click log): zeroed on `ShortUrlCreated`, incremented on `ClickLogged`. Expose totals via `GET /api/stats/{hash}`. Unknown hashes return **404**.

**Traps**: `analytics` must never import `links` or `clicks` internals — it only consumes events from their base packages; computing totals on every request is O(N), materialise a read model instead.

**Technology hints**: `@ApplicationModuleListener` on both events; JPA read model (`LinkStats` entity); `GET /api/stats/{hash}` → 200 or 404.

---

- **Weight:** 0 (seed — not in agreement)
- **Event-coupled:** Y

---

## Later

- **ADR:** [`docs/adr/0001-modulith-and-hexagon.md`](../adr/0001-modulith-and-hexagon.md)

### Acceptance criteria

- [x] `ShortUrlCreated` → zeroed stats — create then eventually readable stats in flow
- [x] `ClickLogged` → increment — `LinkFlowTests` asserts `"totalClicks\":1` after redirect
- [ ] Unknown hash → **404** on `GET /api/stats/{hash}` — handler returns 404; **no automated test yet**

### Scale evidence

- [x] Level 3 — in-process events from `links` and `clicks`
- [ ] Stats under load/failover not measured

### Qualities (self-assessed)

| Quality | Assessed | How to test |
| --- | --- | --- |
| **Correctness** | **0** | Happy path stats=1 automated; claimed **404** untested → **0**. Re-test: `GET /api/stats/missing` → 404; then **5**. |
| **Scalability** | **0** | No scale run data. Confirm only via Compose scale evidence when claimed. |
| **Engineering** | **1.5** | ADR `0001` + `ModularityTests` green; `./gradlew check` green; AI disclosure present. |

**Indicative total:** 0 + 0 + 1.5 = **1.5 / 10**.

### AI disclosure

- **No AI assistance** was used to implement this seed feature in the provided baseline.
- Card text may be maintained with instructor tooling; any later student edits must update this section.
