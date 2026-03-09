"use client";

import React, { useEffect, useMemo, useState } from "react";
import { Shell, TopNav, Hero, Card, Button, Pill } from "@/components/ui";
import { getDlq, replayDlq } from "@/lib/api";
import { RefreshCw } from "lucide-react";

type DlqItem = any;

export default function Recovery() {
  const [items, setItems] = useState<DlqItem[]>([]);
  const [q, setQ] = useState("");
  const [busy, setBusy] = useState<string | null>(null);
  const [msg, setMsg] = useState<string | null>(null);

  async function refresh() {
    const data = await getDlq();
    setItems(data);
  }

  useEffect(() => {
    refresh();
    const id = setInterval(refresh, 3000);
    return () => clearInterval(id);
  }, []);

  const filtered = useMemo(() => {
    const qq = q.trim().toLowerCase();
    if (!qq) return items;
    return items.filter((x) => (x.correlationId || "").toLowerCase().includes(qq));
  }, [items, q]);

  async function replay(id: string) {
    setBusy(id);
    setMsg(null);
    try {
      const res = await replayDlq(id);
      setMsg(`Replayed: ${res.status}`);
      await refresh();
    } catch (e: any) {
      setMsg(e?.message || "Replay failed");
    } finally {
      setBusy(null);
      setTimeout(() => setMsg(null), 1500);
    }
  }

  return (
    <Shell>
      <TopNav />
      <Hero
        title="Replay & Recovery"
        subtitle="Operational rigor: failures go to DLQ. Replay is a first-class button. Correlation IDs make root cause fast."
      />

      <div className="mt-8 grid grid-cols-1 gap-5">
        <Card title="DLQ" subtitle="Dead-letter queue entries (DB-backed for MVP)">
          <div className="flex items-center gap-3">
            <input
              placeholder="Search correlation id…"
              value={q}
              onChange={(e) => setQ(e.target.value)}
              className="w-full rounded-xl border border-white/10 bg-black/30 px-3 py-2 text-sm text-white placeholder:text-white/40"
            />
            <Button variant="ghost" onClick={refresh}>
              <RefreshCw size={16} className="mr-2" /> Refresh
            </Button>
          </div>

          {msg ? <div className="mt-3"><Pill>{msg}</Pill></div> : null}

          <div className="mt-4 space-y-3">
            {filtered.slice(0, 20).map((d: any) => (
              <div key={d.id} className="rounded-xl border border-white/10 bg-black/30 p-3">
                <div className="flex items-start justify-between gap-3">
                  <div className="min-w-0">
                    <div className="truncate text-sm font-semibold">{d.reason}</div>
                    <div className="mt-1 break-all font-mono text-[11px] text-white/50">
                      {d.correlationId}
                    </div>
                    <div className="mt-1 text-[11px] text-white/40">{new Date(d.createdAt).toLocaleString()}</div>
                    <div className="mt-2 text-[11px] text-white/35 line-clamp-2">{JSON.stringify(d.payload)}</div>
                  </div>
                  <div className="flex flex-col items-end gap-2">
                    <Pill>{d.status}</Pill>
                    <Button onClick={() => replay(d.id)} disabled={busy === d.id || String(d.status).toUpperCase() !== "PENDING"}>
                      {busy === d.id ? "Replaying…" : "Replay"}
                    </Button>
                    <a className="text-xs text-white/60 hover:text-white underline" href={`/explain/${encodeURIComponent(d.correlationId)}`}>
                      Open →
                    </a>
                  </div>
                </div>
              </div>
            ))}
            {!filtered.length ? <div className="text-sm text-white/50">No DLQ entries.</div> : null}
          </div>
        </Card>
      </div>
    </Shell>
  );
}
