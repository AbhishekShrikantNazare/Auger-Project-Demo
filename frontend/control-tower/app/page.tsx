"use client";

import React, { useEffect, useMemo, useState } from "react";
import { Shell, TopNav, Hero, Card, Button, Pill } from "@/components/ui";
import { getTimeline, simulateStockout, TimelineItem } from "@/lib/api";
import { Activity, ArrowRight, CheckCircle2, Clock, Flame, ShieldAlert } from "lucide-react";

function fmt(t: string) {
  try {
    const d = new Date(t);
    return d.toLocaleString();
  } catch {
    return t;
  }
}

function kindIcon(kind: TimelineItem["kind"]) {
  if (kind === "EVENT") return <Activity size={16} className="text-white/80" />;
  if (kind === "DECISION") return <Clock size={16} className="text-white/80" />;
  if (kind === "ACTION") return <CheckCircle2 size={16} className="text-white/80" />;
  return <ShieldAlert size={16} className="text-white/80" />;
}

export default function MissionControl() {
  const [timeline, setTimeline] = useState<TimelineItem[]>([]);
  const [loading, setLoading] = useState(false);
  const [pulse, setPulse] = useState(false);

  async function refresh() {
    const data = await getTimeline();
    setTimeline(data);
  }

  useEffect(() => {
    refresh();
    const id = setInterval(refresh, 2000);
    return () => clearInterval(id);
  }, []);

  const grouped = useMemo(() => {
    const sense = timeline.filter((t) => t.kind === "EVENT");
    const decide = timeline.filter((t) => t.kind === "DECISION");
    const exec = timeline.filter((t) => t.kind === "ACTION" || t.kind === "DLQ");
    return { sense, decide, exec };
  }, [timeline]);

  async function onSimulate() {
    setLoading(true);
    setPulse(true);
    try {
      await simulateStockout();
      await refresh();
    } finally {
      setLoading(false);
      setTimeout(() => setPulse(false), 600);
    }
  }

  return (
    <Shell>
      <TopNav />
      <Hero
        title="Replace coordination with execution."
        subtitle="A mini orchestration layer that turns ERP/WMS/TMS signals into governed autonomous actions — with guardrails, explainability, idempotency, and replay."
      />

      <div className="mt-8 flex flex-wrap items-center gap-3">
        <Button onClick={onSimulate} disabled={loading}>
          <span className="mr-2 inline-flex items-center">
            <Flame size={16} />
          </span>
          {loading ? "Simulating..." : "Simulate stockout scenario"}
        </Button>
        <a href="/policy">
          <Button variant="ghost">
            Policy Studio <ArrowRight className="ml-2" size={16} />
          </Button>
        </a>
        <Pill>Polls every 2s</Pill>
        {pulse ? <Pill>Signal → Decision → Execution</Pill> : null}
      </div>

      <div className="mt-8 grid grid-cols-1 gap-5 lg:grid-cols-3">
        <Card title="SENSE" subtitle="Incoming signals from ERP/WMS/TMS">
          <Tape items={grouped.sense} />
        </Card>
        <Card title="DECIDE" subtitle="Trade-offs evaluated under constraints">
          <Tape items={grouped.decide} />
        </Card>
        <Card title="EXECUTE" subtitle="Actions + DLQ + audit-worthy artifacts">
          <Tape items={grouped.exec} />
        </Card>
      </div>

      <div className="mt-8">
        <Card title="ABOUT THIS PROJECT" subtitle="What this system does end-to-end">
          <div className="grid grid-cols-1 gap-4 lg:grid-cols-2">
            <div className="rounded-xl border border-white/10 bg-black/30 p-4">
              <div className="text-sm font-semibold">Overall flow</div>
              <p className="mt-2 text-sm text-white/70 leading-relaxed">
                This project turns operational signals into actions. It ingests events, evaluates response
                options against policy guardrails, executes the selected action, and keeps a complete
                timeline so each decision is explainable and auditable.
              </p>
              <p className="mt-2 text-sm text-white/60 leading-relaxed">
                In the simulation, a shipment delay, low inventory, and demand spike are correlated into one
                incident, then processed through Sense → Decide → Execute.
              </p>
            </div>

            <div className="rounded-xl border border-white/10 bg-black/30 p-4">
              <div className="text-sm font-semibold">What happens in the backend</div>
              <p className="mt-2 text-sm text-white/70 leading-relaxed">
                The orchestrator receives events and prevents duplicates using Redis idempotency keys. It
                stores raw events and state in Postgres, then calls the decision service with policy and
                current inventory/shipment snapshots.
              </p>
              <p className="mt-2 text-sm text-white/60 leading-relaxed">
                The decision service returns scored options and a chosen action. The orchestrator records
                decision + execution results, and on failure routes the action to DLQ so operators can replay
                recovery from the UI.
              </p>
            </div>
          </div>
        </Card>
      </div>
    </Shell>
  );
}

function Tape({ items }: { items: TimelineItem[] }) {
  if (!items.length) {
    return <div className="text-sm text-white/50">No data yet. Run a simulation.</div>;
  }
  return (
    <div className="flex flex-col gap-3">
      {items.slice(0, 12).map((t, idx) => (
        <a
          key={idx}
          href={`/explain/${encodeURIComponent(t.correlationId)}`}
          className="group rounded-xl border border-white/10 bg-black/30 px-3 py-2 transition hover:bg-black/40"
        >
          <div className="flex items-center gap-2">
            <span className="inline-flex h-7 w-7 items-center justify-center rounded-lg bg-white/5 border border-white/10">
              {kindIcon(t.kind)}
            </span>
            <div className="min-w-0 flex-1">
              <div className="truncate text-sm font-semibold">{t.title}</div>
              <div className="truncate text-xs text-white/60">{t.subtitle}</div>
            </div>
          </div>
          <div className="mt-2 text-[11px] text-white/45 font-mono">{fmt(t.at)}</div>
          <div className="mt-2 text-[11px] text-white/35 line-clamp-2">
            {JSON.stringify(t.payload)}
          </div>
          <div className="mt-2 text-[11px] text-white/50 opacity-0 transition group-hover:opacity-100">
            Open explainability →
          </div>
        </a>
      ))}
    </div>
  );
}
