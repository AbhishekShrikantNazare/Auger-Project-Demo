"use client";

import React, { useEffect, useMemo, useState } from "react";
import { Shell, TopNav, Hero, Card, Pill } from "@/components/ui";
import { getTimeline, TimelineItem } from "@/lib/api";

export default function ExplainPage({ params }: { params: { correlationId: string } }) {
  const correlationId = decodeURIComponent(params.correlationId);
  const [timeline, setTimeline] = useState<TimelineItem[]>([]);

  useEffect(() => {
    (async () => setTimeline(await getTimeline()))();
    const id = setInterval(async () => setTimeline(await getTimeline()), 2500);
    return () => clearInterval(id);
  }, []);

  const items = useMemo(
    () => timeline.filter((t) => t.correlationId === correlationId),
    [timeline, correlationId]
  );

  const decision = items.find((t) => t.kind === "DECISION");
  const action = items.find((t) => t.kind === "ACTION");
  const dlq = items.find((t) => t.kind === "DLQ");

  return (
    <Shell>
      <TopNav />
      <Hero
        title="Explainability"
        subtitle="Glass-box autonomy: show constraints, options, and the chosen action — with an audit trail you can replay."
      />

      <div className="mt-6 flex flex-wrap items-center gap-2">
        <Pill>Correlation ID</Pill>
        <span className="font-mono text-xs text-white/60 break-all">{correlationId}</span>
      </div>

      <div className="mt-8 grid grid-cols-1 gap-5 lg:grid-cols-2">
        <Card title="Decision" subtitle="Options evaluated under guardrails">
          {decision ? (
            <pre className="overflow-auto rounded-xl bg-black/40 p-3 text-xs text-white/70 border border-white/10">
              {JSON.stringify(decision.payload, null, 2)}
            </pre>
          ) : (
            <div className="text-sm text-white/50">No decision yet.</div>
          )}
        </Card>

        <Card title="Execution" subtitle="Action result or DLQ entry">
          {action ? (
            <>
              <div className="text-sm font-semibold">Action payload</div>
              <pre className="mt-2 overflow-auto rounded-xl bg-black/40 p-3 text-xs text-white/70 border border-white/10">
                {JSON.stringify(action.payload, null, 2)}
              </pre>
            </>
          ) : dlq ? (
            <>
              <div className="text-sm font-semibold">DLQ payload</div>
              <pre className="mt-2 overflow-auto rounded-xl bg-black/40 p-3 text-xs text-white/70 border border-white/10">
                {JSON.stringify(dlq.payload, null, 2)}
              </pre>
              <div className="mt-3 text-sm text-white/60">
                Replay from <a className="underline hover:text-white" href="/recovery">Replay & Recovery</a>.
              </div>
            </>
          ) : (
            <div className="text-sm text-white/50">No execution yet.</div>
          )}
        </Card>
      </div>

      <div className="mt-8">
        <Card title="Audit Tape" subtitle="Immutable event → decision → action trail">
          {items.length ? (
            <div className="space-y-3">
              {items.map((t, i) => (
                <div key={i} className="rounded-xl border border-white/10 bg-black/30 p-3">
                  <div className="text-sm font-semibold">{t.kind}: {t.title}</div>
                  <div className="mt-1 text-xs text-white/60">{t.subtitle} • {new Date(t.at).toLocaleString()}</div>
                </div>
              ))}
            </div>
          ) : (
            <div className="text-sm text-white/50">No events for this correlation id yet.</div>
          )}
        </Card>
      </div>
    </Shell>
  );
}
