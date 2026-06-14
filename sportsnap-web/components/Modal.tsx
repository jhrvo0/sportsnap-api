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

export function Modal({ isOpen, onClose, title, children, variant = "light" }: ModalProps) {
  const modalRef = useRef<HTMLDivElement>(null);
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
    } else {
      document.body.style.overflow = "unset";
    }

    return () => {
      document.body.style.overflow = "unset";
      window.removeEventListener("keydown", handleEscape);
    };
  }, [isOpen, onClose]);

  if (!isOpen || !mounted) return null;

  const isDark = variant === "dark";

  return createPortal(
    <div className="fixed inset-0 z-[100] flex items-center justify-center p-4 sm:p-6 bg-black/40 backdrop-blur-md animate-in fade-in duration-300">
      <div 
        ref={modalRef}
        className={`${isDark ? "bg-[#1c1c1e] text-white" : "bg-white text-ink-900"} flex flex-col rounded-[3rem] shadow-2xl w-full max-w-2xl max-h-[90vh] animate-in fade-in zoom-in duration-500 border ${isDark ? "border-white/10" : "border-ink-100"}`}
      >
        {/* Fixed Header */}
        <div className="px-8 pt-8 pb-4 sm:px-10 sm:pt-10 flex items-center justify-between shrink-0">
          <h2 className={`text-2xl sm:text-3xl font-black tracking-tight ${isDark ? "text-white" : "text-ink-900"}`}>{title}</h2>
          <button 
            onClick={onClose}
            className={`w-10 h-10 sm:w-12 sm:h-12 flex items-center justify-center rounded-full transition-all duration-200 shrink-0 ${isDark ? "bg-white/5 hover:bg-white/10 text-white/50" : "bg-ink-50 hover:bg-ink-100 text-ink-400"}`}
          >
            <span className="text-xl">✕</span>
          </button>
        </div>
        
        {/* Scrollable Body */}
        <div className="p-8 pt-4 sm:p-10 sm:pt-4 overflow-y-auto flex-1 min-h-0">
          {children}
        </div>
      </div>
    </div>,
    document.body
  );
}
