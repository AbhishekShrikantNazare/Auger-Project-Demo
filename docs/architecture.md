# Architecture

## Objective

Provide a compact, runnable reference for an operational automation loop:
**Sense -> Decide -> Execute -> Recover**

The system demonstrates:
- event ingestion from heterogeneous sources
- policy-governed decisioning
- deterministic audit trail
- failure handling via DLQ + replay

## System components

### 1) Control Tower UI (Next.js)

User-facing features:
- Mission Control timeline (`EVENT`, `DECISION`, `ACTION`, `DLQ`)
- Policy Studio for guardrails
- Explainability page by correlation ID
- Replay & Recovery for failed actions

API usage:
- `GET /api/timeline`
- `GET/PUT /api/policy`
- `GET /api/dlq`
- `POST /api/replay/{dlqId}`
- `POST /api/simulate/stockout`

### 2) Orchestrator (Spring Boot)

Responsibilities:
- ingest external events (`POST /api/events`)
- enforce idempotency with Redis key `event:{eventId}`
- persist:
  - raw events
  - operational state (`inventory_state`, `shipment_state`)
  - decisions (`decision_records`)
  - actions (`action_records`)
  - failures (`dlq_records`)
- call decision service and execute selected action
- replay pending DLQ records

### 3) Decision Service (FastAPI)

Responsibilities:
- accept a decision request (`POST /decide`) containing:
  - correlation ID
  - trigger event type
  - policy constraints
  - inventory and shipment snapshots
- return:
  - scored options
  - chosen option
  - explanation payload for UI/audit

### 4) Data stores

- **Postgres**
  - source of truth for timeline/audit/state/recovery
- **Redis**
  - short-lived idempotency keys to suppress duplicate event processing

## Main sequence

1. Event arrives at orchestrator (`/api/events`) with `eventId` and payload.
2. Orchestrator claims idempotency key in Redis; duplicates are ignored.
3. Event is written to `event_records`.
4. Operational state is updated (inventory/shipment tables).
5. For trigger types (`InventoryLow`, `ShipmentDelayed`, `DemandSpike`), orchestrator calls `/decide`.
6. Decision response is persisted to `decision_records`.
7. Selected action is executed (simulated) and written to `action_records`.
8. If execution fails, record is written to `dlq_records` with `PENDING`.
9. Replay endpoint re-executes pending DLQ item and marks it `REPLAYED`.

## Data model summary

- `policy`: guardrails used by decisioning and execution toggles
- `event_records`: immutable incoming event audit log
- `inventory_state`: latest inventory snapshot by `(sku, location)`
- `shipment_state`: latest shipment snapshot by `shipment_id`
- `decision_records`: chosen actions + explanation payload
- `action_records`: execution outcomes
- `dlq_records`: failed execution payloads for replay

## Failure and recovery model

- Failures during execution do not discard decision context.
- Decision payload is stored with DLQ record to support deterministic replay.
- Replay temporarily bypasses forced-failure demo toggle, then restores prior policy state.

## Current boundaries

- Execution is simulated (no real ERP/WMS/TMS connectors).
- DLQ is DB-backed rather than stream-backed.
- Decision logic is intentionally lightweight and deterministic for demo clarity.
