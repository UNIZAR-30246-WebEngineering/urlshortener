# Feature: \<name\>

## Decision (required by 2 October)

Paste the catalogue entry here (from `guidance.pdf`), or write one in the same shape if the idea is your own:

**Owner module**: links | clicks | analytics | \<new\> | **Event-coupled**: Y | N (\<reason\>)

\<One paragraph — what the user or system gains.\>

**Traps**: \<what will break if done naively\>

**Technology hints**: \<libraries or patterns\>

---

- **Weight:** 5 | 8 | 13 | 21
- **Event-coupled:** Y | N

---

## Later (fill before the feature is graded, not for the agreement)

- **ADR:** none | docs/adr/NNNN-….md *(add when a choice locks a module, the broker, or a library)*

### Acceptance criteria

- [ ] …
- [ ] …

### Scale evidence

| Evidence | Required |
| --- | --- |
| `replica-failover` | Horizontal scale claimed |
| `load-compare` | Weight ≥ 13 or horizontal scale |
| `cross-instance` | Level 4 claimed |

### Qualities (self-assessed)

Fill **Assessed** with the grade you claim; **How to test** must falsify that number if it failed.

| Quality | Assessed | How to test |
| --- | --- | --- |
| **Correctness** | 0 \| 5 | Acceptance tests for this feature |
| **Scalability** | 0–3.5 | Scale evidence above. Not `ModularityTests`. |
| **Engineering** | 0 \| 0.75 \| 1.5 | Hard gates: AI disclosure + `./gradlew check`. Then ADR + `ModularityTests`: both → **1.5**, one → **0.75**. |

**Indicative total:** _ / 10

### AI disclosure

- **Tools / skills:** …
- **Used for:** …
- **Human-reviewed:** …
- Or: **No AI assistance** was used for this feature.
