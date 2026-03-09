export type TimelineItem = {
  kind: "EVENT" | "DECISION" | "ACTION" | "DLQ";
  correlationId: string;
  title: string;
  subtitle: string;
  at: string;
  payload: any;
};

export type Policy = {
  budgetCapUsd: number;
  approvalRequiredOverUsd: number;
  maxExpeditesPerDay: number;
  allowedActions: string[];
  forceExecutionFailure: boolean;
};

const API_BASE = process.env.NEXT_PUBLIC_API_BASE || "http://localhost:8080";

export async function getTimeline(): Promise<TimelineItem[]> {
  const res = await fetch(`${API_BASE}/api/timeline`, { cache: "no-store" });
  if (!res.ok) throw new Error("Failed timeline");
  return res.json();
}

export async function simulateStockout(): Promise<any> {
  const res = await fetch(`${API_BASE}/api/simulate/stockout`, {
    method: "POST",
    headers: { "Content-Type": "application/json" },
  });
  if (!res.ok) throw new Error("Failed simulate");
  return res.json();
}

export async function getPolicy(): Promise<Policy> {
  const res = await fetch(`${API_BASE}/api/policy`, { cache: "no-store" });
  if (!res.ok) throw new Error("Failed policy");
  return res.json();
}

export async function updatePolicy(p: Policy): Promise<void> {
  const res = await fetch(`${API_BASE}/api/policy`, {
    method: "PUT",
    headers: { "Content-Type": "application/json" },
    body: JSON.stringify(p),
  });
  if (!res.ok) throw new Error("Failed update policy");
}

export async function getDlq(): Promise<any[]> {
  const res = await fetch(`${API_BASE}/api/dlq`, { cache: "no-store" });
  if (!res.ok) throw new Error("Failed dlq");
  return res.json();
}

export async function replayDlq(id: string): Promise<any> {
  const res = await fetch(`${API_BASE}/api/replay/${id}`, { method: "POST" });
  if (!res.ok) throw new Error("Failed replay");
  return res.json();
}
