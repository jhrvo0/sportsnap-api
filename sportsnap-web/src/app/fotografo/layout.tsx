"use client";

import { Sidebar } from "@/components/sidebar";

export default function FotografoLayout({ children }: { children: React.ReactNode }) {
  return (
    <div className="min-h-screen bg-dark-900">
      <Sidebar />
      <main className="ml-64 p-8">{children}</main>
    </div>
  );
}
