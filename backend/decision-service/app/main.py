from fastapi import FastAPI
from pydantic import BaseModel, Field
from typing import Any, Dict, List, Literal, Optional
from datetime import datetime, timezone

app = FastAPI(title="CTK Decision Service", version="0.1.0")

class Policy(BaseModel):
    budget_cap_usd: int = 5000
    approval_required_over_usd: int = 3000
    max_expedites_per_day: int = 10
    allowed_actions: List[str] = Field(default_factory=lambda: ["EXPEDITE", "REBALANCE", "REROUTE"])
    force_execution_failure: bool = False  # demo-only toggle

class InventoryState(BaseModel):
    sku: str
    location: str
    on_hand: int
    daily_demand: int

class ShipmentState(BaseModel):
    shipment_id: str
    status: str
    eta_hours: int
    origin: str
    destination: str

class DecideRequest(BaseModel):
    correlation_id: str
    triggered_by: str
    policy: Policy
    inventory: List[InventoryState] = Field(default_factory=list)
    shipments: List[ShipmentState] = Field(default_factory=list)

class Option(BaseModel):
    action: Literal["EXPEDITE","REBALANCE","REROUTE","SUBSTITUTE"]
    est_cost_usd: int
    est_hours_to_fix: int
    service_risk: str
    score: float
    rationale: str

class DecideResponse(BaseModel):
    correlation_id: str
    chosen: Option
    options: List[Option]
    explanation: Dict[str, Any]
    decided_at: str

def _score(cost: int, hours: int, service_risk: str, policy: Policy) -> float:
    # Lower cost + lower hours = better. Penalize if exceeds budget.
    budget_penalty = 2.0 if cost > policy.budget_cap_usd else 0.0
    risk_penalty = {"LOW":0.0, "MED":1.0, "HIGH":2.0}.get(service_risk, 1.0)
    return (hours * 0.8) + (cost / 1000.0) + budget_penalty + risk_penalty

@app.post("/decide", response_model=DecideResponse)
def decide(req: DecideRequest):
    # Very small "optimizer": generate options based on stockout risk heuristic.
    # If any inventory item is at risk (on_hand < daily_demand), prioritize speed.
    stockout_risk = any(i.on_hand < i.daily_demand for i in req.inventory) if req.inventory else True

    options: List[Option] = []

    # EXPEDITE: fast, expensive
    expedite_cost = 3500 if stockout_risk else 2500
    expedite_hours = 6 if stockout_risk else 12
    options.append(Option(
        action="EXPEDITE",
        est_cost_usd=expedite_cost,
        est_hours_to_fix=expedite_hours,
        service_risk="LOW",
        score=0.0,
        rationale="Pay more to recover service quickly."
    ))

    # REBALANCE: moderate cost/time
    rebalance_cost = 1200
    rebalance_hours = 18 if stockout_risk else 24
    options.append(Option(
        action="REBALANCE",
        est_cost_usd=rebalance_cost,
        est_hours_to_fix=rebalance_hours,
        service_risk="MED",
        score=0.0,
        rationale="Move inventory from another node to avoid stockout."
    ))

    # REROUTE: cheaper but may increase time
    reroute_cost = 800
    reroute_hours = 20 if stockout_risk else 26
    options.append(Option(
        action="REROUTE",
        est_cost_usd=reroute_cost,
        est_hours_to_fix=reroute_hours,
        service_risk="MED",
        score=0.0,
        rationale="Change shipment destination/path to match current constraints."
    ))

    # Compute scores and choose best under policy (lower score is better)
    scored = []
    for o in options:
        o.score = _score(o.est_cost_usd, o.est_hours_to_fix, o.service_risk, req.policy)
        scored.append(o)

    # Filter by allowed actions
    filtered = [o for o in scored if o.action in req.policy.allowed_actions]
    if not filtered:
        filtered = scored

    chosen = sorted(filtered, key=lambda x: x.score)[0]

    explanation = {
        "triggered_by": req.triggered_by,
        "stockout_risk": stockout_risk,
        "policy": req.policy.model_dump(),
        "why_chosen": "Lowest overall score under guardrails (time, cost, service risk).",
        "tradeoffs": [o.model_dump() for o in sorted(scored, key=lambda x: x.score)]
    }

    return DecideResponse(
        correlation_id=req.correlation_id,
        chosen=chosen,
        options=sorted(scored, key=lambda x: x.score),
        explanation=explanation,
        decided_at=datetime.now(timezone.utc).isoformat()
    )
