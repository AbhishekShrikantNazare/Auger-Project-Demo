# Coordination Tax Killer

Coordination Tax Killer is a runnable mini-platform that shows how operational events can move from signal to automated action with governance and auditability.

Core loop:
1. **Sense**: ingest operational events (ERP/WMS/TMS-style)
2. **Decide**: evaluate options against policy constraints
3. **Execute**: record an action result, or route to recovery if execution fails

The project is intentionally small, but built with production-style concerns: idempotency, audit trail, policy control, dead-letter queue (DLQ), and replay.

## What this project does

When you trigger the stockout simulation, the orchestrator creates a correlated incident from three events:
- `ShipmentDelayed` (TMS-like)
- `InventoryLow` (WMS-like)
- `DemandSpike` (ERP-like)

For those events, the orchestrator:
1. Deduplicates events using Redis idempotency keys
2. Persists raw events and operational state in Postgres
3. Calls the decision service with:
   - current policy
   - inventory/shipment state snapshot
   - trigger context
4. Receives scored options (e.g. `EXPEDITE`, `REBALANCE`, `REROUTE`) and chosen action
5. Persists decision and simulated execution result
6. If execution fails, writes a DLQ entry that can be replayed from the UI

### Project Screenshots

![Mission Control](<images/Screenshot 2026-03-09 at 4.55.12 PM.png>)
![Policy Studio](<images/Screenshot 2026-03-09 at 4.55.38 PM.png>)
![Replay & Recovery](<images/Screenshot 2026-03-09 at 4.55.53 PM.png>)
![Explainability](<images/Screenshot 2026-03-09 at 4.56.31 PM.png>)
![Timeline Detail](<images/Screenshot 2026-03-09 at 4.56.53 PM.png>)

## Architecture

- **UI**: Next.js control tower
  - Mission Control timeline
  - Policy Studio
  - Explainability view by correlation ID
  - Replay & Recovery for DLQ entries
- **Orchestrator**: Spring Boot service
  - event ingest, state updates, decision orchestration, execution record, replay
- **Decision Service**: FastAPI service
  - simple policy-aware scoring engine and explanation payload
- **Data stores**
  - Postgres: audit/state/decisions/actions/DLQ
  - Redis: event idempotency cache

For details, see:
- [Architecture](docs/architecture.md)
- [Demo script](docs/demo-script.md)

## Quick start

### Prerequisites
- Docker Desktop with Compose

### Run the full stack
```bash
docker compose up --build
```

### Endpoints
- UI: http://localhost:3000
- Orchestrator API: http://localhost:8080
- Decision service: http://localhost:8000
- Postgres: `localhost:5432` (`ctk` / `ctk`, db `ctk`)
- Redis: `localhost:6379`

## UI walkthrough

1. Open **Mission Control** (`/`) and click **Simulate stockout scenario**
2. Watch timeline entries appear in order: events, decision, execution
3. Click an item to open **Explainability** for that correlation ID
4. Open **Policy Studio** (`/policy`) and change guardrails/allowed actions
5. Re-run simulation and observe changed decisions
6. Optional failure/recovery flow:
   - enable `Force execution failure`
   - simulate again
   - open **Replay & Recovery** (`/recovery`) and replay pending DLQ item

## Key API endpoints

- `POST /api/events` ingest one event
- `POST /api/simulate/stockout` trigger demo scenario
- `GET /api/timeline` read recent event/decision/action/DLQ timeline
- `GET /api/policy` read policy
- `PUT /api/policy` update policy
- `GET /api/dlq` list DLQ records
- `POST /api/replay/{dlqId}` replay a pending DLQ item

## Repository structure

- `frontend/control-tower` UI
- `backend/orchestrator` Spring Boot orchestrator
- `backend/decision-service` FastAPI decision engine
- `docs/` project docs

## Notes

- DLQ is DB-backed for simplicity in this mini project.
- Execution is simulated in code; connector calls to external ERP/WMS/TMS systems are the next step.
