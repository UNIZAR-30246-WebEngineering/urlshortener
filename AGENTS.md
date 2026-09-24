# Agent guidance — urlshortener

## Product

**Product = Boot jar(s) + this Compose file**, not "jar on localhost only."

## Commands

```bash
./gradlew check                # All gates: ktlint + detekt + test + JaCoCo (60%)
./gradlew test                 # Modulith verify + hexagonal ArchUnit + flow test; JaCoCo per module under build/reports/jacoco/<module>/html
./gradlew test --tests '*PostgresTests'  # Concurrency cases on Postgres via Testcontainers (needs Docker; skipped without it)
./gradlew ktlintFormat         # Auto-fix style issues
./gradlew bootJar
docker compose up --build      # Postgres + app1 + app2 + nginx LB on :8080
docker compose --profile load run --rm k6
# or from host:
BASE_URL=http://localhost:8080 k6 run scripts/load.k6.js
./scripts/scale-baseline.sh
```

## Module map (provided)

Each package below is a **Modulith application module**. Own your feature in one owner module; talk to others **only via events** in the base package (never import another module’s `domain` / `application` / `adapters`).

### Ownership

| Module      | Own                                                                                 | Do not                       |
|-------------|-------------------------------------------------------------------------------------|------------------------------|
| `links`     | Create/store/redirect short URLs (`ShortUrl`, `CreateShortUrl`, `RedirectShortUrl`) | Click history or `LinkStats` |
| `clicks`    | Append-only click log (`Click`, event type `ClickLoggedEvent`)                           | Totals / `/api/stats`        |
| `analytics` | Per-hash counters (`LinkStats`, `UpdateLinkStats`, `GetLinkStats`)                  | Raw click rows               |

### HTTP API

| Module      | Endpoints                       |
|-------------|---------------------------------|
| `links`     | `POST /api/link`, `GET /{hash}` |
| `analytics` | `GET /api/stats/{hash}`         |
| `clicks`    | —           |

### Events

| Event | Owner module | When | Payload |
| --- | --- | --- | --- |
| `ShortUrlCreatedEvent` | `links` | After a short URL is persisted | `hash`, `target`, `createdAt` |
| `ClickLoggedEvent` | `clicks` | After a successful redirect lookup | `hash`, `occurredAt`, `eventId` (consumers ignore repeats; [ADR 0002](docs/adr/0002-atomic-upserts.md)) |

| Module | Publishes | Consumes |
| --- | --- | --- |
| `links` | `ShortUrlCreatedEvent`, `ClickLoggedEvent` | — |
| `clicks` | — | `ClickLoggedEvent` consumed by `ClickRecorder` |
| `analytics` | — | `ShortUrlCreatedEvent`, `ClickLoggedEvent` consumed by `LinkStatsListener` |

**Do not confuse:** `clicks` = permanent log (many rows per hash); `analytics` = one `LinkStats` row per hash. Sole exception: `analytics` keeps processed `ClickLoggedEvent` ids (no hash) for deduplication, pruned after `urlshortener.analytics.processed-click-retention` ([ADR 0002](docs/adr/0002-atomic-upserts.md)). Growing a feature? Put it in the module that owns that data, or add a new module + ADR.

Each Modulith module is a **closed** application module with an internal hexagon:

- **Public API** = base package only (integration events such as `ShortUrlCreatedEvent`, `ClickLoggedEvent`)
- **Internal** = `domain` (where present), `application` (use cases + jMolecules ports), `adapters.*` (web, persistence, events, tech)
- **Hexagon** = jMolecules stereotypes; verified via `ensureHexagonal(SEMI_STRICT)` in `ModularityTests`

Do **not** break `ApplicationModules.verify()`. Prefer events over cross-module bean injection. Do not import another module’s `adapters` or `domain` packages.

## Decisions

Grown changes that affect modules, hexagon enforcement, events, or specialised tech need an ADR under [`docs/adr/`](docs/adr/README.md) (template [`0000-TEMPLATE.md`](docs/adr/0000-TEMPLATE.md)). Seed rationale: [`0001-modulith-and-hexagon.md`](docs/adr/0001-modulith-and-hexagon.md).

## Grown (students)

Web UI, specialised tech, Level-4 broker path (`--profile broker` stub), **feature cards** under [`docs/features/`](docs/features/README.md), defence packet (cite ADRs + evidence commands).

## Engineering quality ticks

**Hard gates** (any missing → **Engineering = 0**):

1. **AI disclosure** — required on the feature card (tools used **or** explicit “no AI assistance”)
2. **QA gate** — `./gradlew check` green (ktlint + detekt + test + JaCoCo ≥60%); CI must not be red

**With both gates present**, score the architecture ticks:

| Architecture ticks green | Engineering |
| --- | --- |
| ADR with events **and** `ModularityTests` (verify + hexagon) | **1.5** |
| Exactly one of those two | **0.75** |
| Neither | **0** |

3. **ADR with events** — decision recorded before/with code; events documented
4. **Package + verify green** — `ApplicationModules.verify()` + `ensureHexagonal(SEMI_STRICT)` in `ModularityTests`

## Feature cards

Seed (weight 0): [`0001`](docs/features/0001-create-short-url.md)–[`0003`](docs/features/0003-link-stats.md). Grown: copy [`0000-TEMPLATE.md`](docs/features/0000-TEMPLATE.md) → `0004-…`; update [`docs/features/README.md`](docs/features/README.md) index (budget ≤84, max 5 scored).
