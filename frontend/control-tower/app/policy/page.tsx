"use client";

import React, { useEffect, useState } from "react";
import { Shell, TopNav, Hero, Card, Button, Pill } from "@/components/ui";
import { getPolicy, updatePolicy, Policy } from "@/lib/api";

const ACTIONS = ["EXPEDITE", "REBALANCE", "REROUTE", "SUBSTITUTE"];

export default function PolicyStudio() {
  const [p, setP] = useState<Policy | null>(null);
  const [saving, setSaving] = useState(false);
  const [msg, setMsg] = useState<string | null>(null);

  useEffect(() => {
    (async () => setP(await getPolicy()))();
  }, []);

  async function save() {
    if (!p) return;
    setSaving(true);
    setMsg(null);
    try {
      await updatePolicy(p);
      setMsg("Saved.");
    } catch (e: any) {
      setMsg(e?.message || "Failed.");
    } finally {
      setSaving(false);
      setTimeout(() => setMsg(null), 1500);
    }
  }

  return (
    <Shell>
      <TopNav />
      <Hero
        title="Policy Studio"
        subtitle="Guardrails define autonomy. Adjust constraints, approvals, and allowed actions — then watch the decision engine adapt."
      />

      {!p ? (
        <div className="mt-8 text-white/60">Loading policy…</div>
      ) : (
        <div className="mt-8 grid grid-cols-1 gap-5 lg:grid-cols-2">
          <Card title="Guardrails" subtitle="Budget and approval thresholds">
            <div className="space-y-5">
              <Slider
                label="Budget cap (USD)"
                value={p.budgetCapUsd}
                min={500}
                max={20000}
                step={100}
                onChange={(v) => setP({ ...p, budgetCapUsd: v })}
              />
              <Slider
                label="Approval required over (USD)"
                value={p.approvalRequiredOverUsd}
                min={500}
                max={20000}
                step={100}
                onChange={(v) => setP({ ...p, approvalRequiredOverUsd: v })}
              />
              <Slider
                label="Max expedites per day"
                value={p.maxExpeditesPerDay}
                min={0}
                max={50}
                step={1}
                onChange={(v) => setP({ ...p, maxExpeditesPerDay: v })}
              />

              <label className="flex items-center justify-between gap-3 rounded-xl border border-white/10 bg-black/30 px-3 py-2">
                <div>
                  <div className="text-sm font-semibold">Force execution failure (demo)</div>
                  <div className="text-xs text-white/60">Sends actions to DLQ so you can replay.</div>
                </div>
                <input
                  type="checkbox"
                  checked={p.forceExecutionFailure}
                  onChange={(e) => setP({ ...p, forceExecutionFailure: e.target.checked })}
                  className="h-5 w-5"
                />
              </label>

              <div className="flex items-center gap-3">
                <Button onClick={save} disabled={saving}>
                  {saving ? "Saving…" : "Save policy"}
                </Button>
                {msg ? <Pill>{msg}</Pill> : null}
              </div>
            </div>
          </Card>

          <Card title="Allowed Actions" subtitle="Governed autonomy: what the system can do">
            <div className="space-y-3">
              {ACTIONS.map((a) => {
                const checked = p.allowedActions.includes(a);
                return (
                  <label key={a} className="flex items-center justify-between gap-3 rounded-xl border border-white/10 bg-black/30 px-3 py-2">
                    <div className="text-sm font-semibold">{a}</div>
                    <input
                      type="checkbox"
                      checked={checked}
                      onChange={(e) => {
                        const next = e.target.checked
                          ? [...p.allowedActions, a]
                          : p.allowedActions.filter((x) => x !== a);
                        setP({ ...p, allowedActions: next });
                      }}
                      className="h-5 w-5"
                    />
                  </label>
                );
              })}

              <div className="mt-4 text-sm text-white/60 leading-relaxed">
                Tip: disable <span className="text-white">EXPEDITE</span> and run a simulation — the engine will pick the next-best option.
              </div>
            </div>
          </Card>
        </div>
      )}
    </Shell>
  );
}

function Slider({
  label,
  value,
  min,
  max,
  step,
  onChange,
}: {
  label: string;
  value: number;
  min: number;
  max: number;
  step: number;
  onChange: (v: number) => void;
}) {
  return (
    <div className="rounded-xl border border-white/10 bg-black/30 px-3 py-3">
      <div className="flex items-center justify-between">
        <div className="text-sm font-semibold">{label}</div>
        <div className="font-mono text-xs text-white/70">{value}</div>
      </div>
      <input
        type="range"
        className="mt-3 w-full"
        value={value}
        min={min}
        max={max}
        step={step}
        onChange={(e) => onChange(Number(e.target.value))}
      />
      <div className="mt-2 flex justify-between text-[11px] text-white/40 font-mono">
        <span>{min}</span>
        <span>{max}</span>
      </div>
    </div>
  );
}
