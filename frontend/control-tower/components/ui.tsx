import React from "react";
import { motion } from "framer-motion";

export function Shell({ children }: { children: React.ReactNode }) {
  return (
    <div className="min-h-screen bg-[#060609] text-white">
      <div className="pointer-events-none fixed inset-0 opacity-60">
        <div className="absolute -top-40 left-1/2 h-[520px] w-[520px] -translate-x-1/2 rounded-full bg-white/6 blur-3xl" />
        <div className="absolute top-40 left-10 h-[420px] w-[420px] rounded-full bg-white/4 blur-3xl" />
        <div className="absolute bottom-10 right-10 h-[420px] w-[420px] rounded-full bg-white/4 blur-3xl" />
      </div>

      <div className="relative mx-auto max-w-6xl px-6 py-8">
        {children}
      </div>
    </div>
  );
}

export function TopNav() {
  return (
    <div className="flex items-center justify-between gap-6 border-b border-white/10 pb-6">
      <div className="flex items-baseline gap-3">
        <div className="text-sm font-semibold tracking-[0.35em] text-white/70">CTK</div>
        <div className="text-xl font-semibold tracking-tight">Coordination Tax Killer</div>
      </div>

      <div className="flex items-center gap-3 text-sm text-white/70">
        <a className="hover:text-white transition" href="/">Mission Control</a>
        <span className="text-white/20">/</span>
        <a className="hover:text-white transition" href="/policy">Policy Studio</a>
        <span className="text-white/20">/</span>
        <a className="hover:text-white transition" href="/recovery">Replay & Recovery</a>
      </div>
    </div>
  );
}

export function Hero({ title, subtitle }: { title: string; subtitle: string }) {
  return (
    <div className="mt-8">
      <div className="text-xs font-semibold tracking-[0.35em] text-white/60">SENSE · DECIDE · EXECUTE</div>
      <h1 className="mt-4 text-4xl sm:text-6xl font-semibold tracking-tight leading-[1.05]">
        {title}
      </h1>
      <p className="mt-4 max-w-3xl text-base sm:text-lg text-white/70">
        {subtitle}
      </p>
      <div className="mt-6 h-px w-full bg-white/10" />
    </div>
  );
}

export function Card({
  title,
  subtitle,
  children,
}: {
  title: string;
  subtitle?: string;
  children: React.ReactNode;
}) {
  return (
    <motion.div
      initial={{ opacity: 0, y: 10 }}
      animate={{ opacity: 1, y: 0 }}
      transition={{ duration: 0.25 }}
      className="rounded-2xl border border-white/10 bg-white/5 p-5 shadow-[0_0_0_1px_rgba(255,255,255,0.04)_inset]"
    >
      <div className="flex items-start justify-between gap-3">
        <div>
          <div className="text-sm font-semibold">{title}</div>
          {subtitle ? <div className="mt-1 text-xs text-white/60">{subtitle}</div> : null}
        </div>
      </div>
      <div className="mt-4">{children}</div>
    </motion.div>
  );
}

export function Pill({ children }: { children: React.ReactNode }) {
  return (
    <span className="inline-flex items-center rounded-full border border-white/10 bg-black/30 px-2.5 py-1 text-[11px] font-medium text-white/70">
      {children}
    </span>
  );
}

export function Button({
  children,
  onClick,
  variant = "primary",
  disabled,
  type = "button",
}: {
  children: React.ReactNode;
  onClick?: () => void;
  variant?: "primary" | "ghost";
  disabled?: boolean;
  type?: "button" | "submit";
}) {
  const cls =
    variant === "primary"
      ? "bg-white text-black hover:bg-white/90"
      : "bg-white/0 text-white border border-white/15 hover:bg-white/10";
  return (
    <button
      type={type}
      disabled={disabled}
      onClick={onClick}
      className={`inline-flex items-center justify-center rounded-xl px-3.5 py-2 text-sm font-semibold transition disabled:opacity-50 disabled:cursor-not-allowed ${cls}`}
    >
      {children}
    </button>
  );
}
