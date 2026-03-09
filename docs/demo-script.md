# 5-minute demo script

## 1) Start the stack (30s)

Run:
```bash
docker compose up --build
```

Confirm:
- UI: `http://localhost:3000`
- API health: `http://localhost:8080/actuator/health`

## 2) Baseline walk-through (30s)

Open the UI and show:
- Mission Control
- Policy Studio
- Replay & Recovery

Mention that timeline entries are correlated across the same incident via `correlationId`.

## 3) Trigger a scenario (30s)

In Mission Control, click **Simulate stockout scenario**.

What happens:
- three input events are ingested
- one decision is generated
- one action is executed

## 4) Inspect the decision (60s)

Open the corresponding item in Explainability:
- inspect scored options
- inspect chosen action
- inspect explanation payload

Call out that decision inputs include policy + current state snapshot.

## 5) Show policy effect (60s)

In Policy Studio:
- disable one action (for example `EXPEDITE`) or reduce budget cap
- save policy
- simulate again

Show that the chosen option changes under new guardrails.

## 6) Show failure and recovery (60s)

In Policy Studio:
- enable **Force execution failure**
- simulate again

In Replay & Recovery:
- open new DLQ item
- click **Replay**
- confirm status transition from `PENDING` to `REPLAYED`

## 7) Close with technical summary (30s)

Summarize:
- idempotent ingest (Redis)
- persistent audit/state/history (Postgres)
- policy-governed decisioning (FastAPI)
- deterministic recovery path (DLQ + replay)
