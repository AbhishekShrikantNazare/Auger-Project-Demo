import "./globals.css";
import type { Metadata } from "next";

export const metadata: Metadata = {
  title: "Coordination Tax Killer",
  description: "Mini Sense→Decide→Execute orchestration layer",
};

export default function RootLayout({ children }: { children: React.ReactNode }) {
  return (
    <html lang="en">
      <body>{children}</body>
    </html>
  );
}
