CREATE EXTENSION IF NOT EXISTS "uuid-ossp";

CREATE TABLE IF NOT EXISTS policy (
  id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
  budget_cap_usd INT NOT NULL DEFAULT 5000,
  approval_required_over_usd INT NOT NULL DEFAULT 3000,
  max_expedites_per_day INT NOT NULL DEFAULT 10,
  allowed_actions TEXT NOT NULL DEFAULT '["EXPEDITE","REBALANCE","REROUTE"]',
  force_execution_failure BOOLEAN NOT NULL DEFAULT FALSE,
  updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

INSERT INTO policy (budget_cap_usd, approval_required_over_usd, max_expedites_per_day, allowed_actions, force_execution_failure)
SELECT 5000, 3000, 10, '["EXPEDITE","REBALANCE","REROUTE"]', FALSE
WHERE NOT EXISTS (SELECT 1 FROM policy);

CREATE TABLE IF NOT EXISTS event_records (
  id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
  event_id TEXT NOT NULL UNIQUE,
  source TEXT NOT NULL,
  type TEXT NOT NULL,
  correlation_id TEXT NOT NULL,
  event_time TIMESTAMPTZ NOT NULL,
  payload JSONB NOT NULL,
  received_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE TABLE IF NOT EXISTS inventory_state (
  id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
  sku TEXT NOT NULL,
  location TEXT NOT NULL,
  on_hand INT NOT NULL,
  daily_demand INT NOT NULL DEFAULT 0,
  updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
  UNIQUE (sku, location)
);

CREATE TABLE IF NOT EXISTS shipment_state (
  id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
  shipment_id TEXT NOT NULL UNIQUE,
  status TEXT NOT NULL,
  eta_hours INT NOT NULL,
  origin TEXT NOT NULL,
  destination TEXT NOT NULL,
  updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE TABLE IF NOT EXISTS decision_records (
  id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
  correlation_id TEXT NOT NULL,
  triggered_by TEXT NOT NULL,
  chosen_action TEXT NOT NULL,
  explanation JSONB NOT NULL,
  decided_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE TABLE IF NOT EXISTS action_records (
  id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
  correlation_id TEXT NOT NULL,
  action_type TEXT NOT NULL,
  status TEXT NOT NULL,
  details JSONB NOT NULL,
  created_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE TABLE IF NOT EXISTS dlq_records (
  id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
  correlation_id TEXT NOT NULL,
  reason TEXT NOT NULL,
  payload JSONB NOT NULL,
  status TEXT NOT NULL DEFAULT 'PENDING',
  created_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);
