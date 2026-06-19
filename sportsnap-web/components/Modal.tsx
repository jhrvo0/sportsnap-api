"use client";

import { useEffect, useRef, useState } from "react";
import { createPortal } from "react-dom";

interface ModalProps {
  isOpen: boolean;
  onClose: () => void;
  title: string;
  children: React.ReactNode;
  variant?: "light" | "dark";
}

/**
 * Modal via portal. Adiciona aria-label, focus-visible no botão de fechar
 * e raio unificado. API (isOpen/onClose/title/children/variant) preservada.
 */
export function Modal({ isOpen, onClose, title, children, variant = "light" }: ModalProps) {
  const modalRef = useRef<HTMLDivElement>(null);
  const closeBtnRef = useRef<HTMLButtonElement>(null);
  const [mounted, setMounted] = useState(false);

  useEffect(() => {
    setMounted(true);
    return () => setMounted(false);
  }, []);

  useEffect(() => {
    const handleEscape = (e: KeyboardEvent) => {
      if (e.key === "Escape") onClose();
    };

    if (isOpen) {
      document.body.style.overflow = "hidden";
      window.addEventListener("keydown", handleEscape);
      return () => {
        window.removeEventListener("keydown", handleEscape);
      };
    }

    document.body.style.overflow = "unset";
    window.removeEventListener("keydown", handleEscape);
    return;
  }, [isOpen, onClose]);

  useEffect(() => {
    if (isOpen) {
      // Foca o botão de fechar ao abrir para acessibilidade por teclado.
      const t = setTimeout(() => closeBtnRef.current?.focus(), 50);
      return () => {
        clearTimeout(t);
      };
    }
  }, [isOpen]);

  if (!isOpen || !mounted) return null;

  const isDark = variant === "dark";

  return createPortal(
    <div
      className="fixed inset-0 z-[100] flex items-center justify-center bg-black/40 p-4 backdrop-blur-md animate-in fade-in duration-300 sm:p-6"
      onClick={(e) => {
        if (e.target === e.currentTarget) onClose();
      }}
    >
      <div
        ref={modalRef}
        role="dialog"
        aria-modal="true"
        aria-label={title}
        className={`flex max-h-[90vh] w-full max-w-2xl flex-col rounded-3xl border shadow-2xl animate-in fade-in zoom-in duration-500 ${isDark ? "border-white/10 bg-[#1c1c1e] text-white" : "border-ink-100 bg-white text-ink-900"}`}
      >
        {/* Header fixo */}
        <div
          className={`flex shrink-0 items-center justify-between px-6 pb-4 pt-6 sm:px-10 sm:pt-10`}
        >
          <h2 className={`text-2xl font-black tracking-tight sm:text-3xl ${isDark ? "text-white" : "text-ink-900"}`}>
            {title}
          </h2>
          <button
            ref={closeBtnRef}
            onClick={onClose}
            aria-label="Fechar"
            className={`flex h-10 w-10 shrink-0 items-center justify-center rounded-full transition-all duration-200 outline-none focus-visible:ring-4 focus-visible:ring-accent/30 sm:h-12 sm:w-12 ${isDark ? "bg-white/5 text-white/50 hover:bg-white/10 hover:text-white" : "bg-ink-50 text-ink-400 hover:bg-ink-100 hover:text-ink-700"}`}
          >
            <span className="text-xl">✕</span>
          </button>
        </div>

        {/* Corpo rolável */}
        <div className="min-h-0 flex-1 overflow-y-auto p-6 pt-2 sm:p-10 sm:pt-2">{children}</div>
      </div>
    </div>,
    document.body,
  );
}
