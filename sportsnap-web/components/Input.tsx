import { forwardRef, type InputHTMLAttributes, type SelectHTMLAttributes, type TextareaHTMLAttributes } from "react";

type FieldProps = {
  label?: string;
  hint?: string;
  error?: string;
};

const baseInput =
  "h-11 w-full rounded-xl border bg-white px-4 text-[15px] text-ink-900 placeholder:text-ink-400 transition-colors duration-200 focus:outline-none focus:ring-4 disabled:cursor-not-allowed disabled:opacity-60";

const toneBorder = (hasError?: boolean) =>
  hasError
    ? "border-rose-200 focus:border-rose-400 focus:ring-rose-100"
    : "border-ink-200 focus:border-accent focus:ring-accent/20";

export const Input = forwardRef<
  HTMLInputElement,
  InputHTMLAttributes<HTMLInputElement> & FieldProps
>(function Input({ label, hint, error, className = "", ...rest }, ref) {
  return (
    <label className="block">
      {label && <span className="mb-1.5 block text-[13px] font-medium text-ink-700">{label}</span>}
      <input ref={ref} className={`${baseInput} ${toneBorder(!!error)} ${className}`} {...rest} />
      {error && <span className="mt-1 block text-[12px] font-medium text-rose-600">{error}</span>}
      {!error && hint && <span className="mt-1 block text-[12px] font-medium text-ink-400">{hint}</span>}
    </label>
  );
});

export const Select = forwardRef<
  HTMLSelectElement,
  SelectHTMLAttributes<HTMLSelectElement> & FieldProps
>(function Select({ label, hint, error, className = "", children, ...rest }, ref) {
  return (
    <label className="block">
      {label && <span className="mb-1.5 block text-[13px] font-medium text-ink-700">{label}</span>}
      <div className="relative">
        <select
          ref={ref}
          className={`${baseInput} ${toneBorder(!!error)} cursor-pointer appearance-none pr-10 ${className}`}
          {...rest}
        >
          {children}
        </select>
        <span
          aria-hidden
          className="pointer-events-none absolute right-4 top-1/2 -translate-y-1/2 text-[10px] text-ink-400"
        >
          ▼
        </span>
      </div>
      {error && <span className="mt-1 block text-[12px] font-medium text-rose-600">{error}</span>}
      {!error && hint && <span className="mt-1 block text-[12px] font-medium text-ink-400">{hint}</span>}
    </label>
  );
});

export const Textarea = forwardRef<
  HTMLTextAreaElement,
  TextareaHTMLAttributes<HTMLTextAreaElement> & FieldProps
>(function Textarea({ label, hint, error, className = "", ...rest }, ref) {
  return (
    <label className="block">
      {label && <span className="mb-1.5 block text-[13px] font-medium text-ink-700">{label}</span>}
      <textarea
        ref={ref}
        className={`min-h-[80px] w-full rounded-xl border px-4 py-3 text-[15px] text-ink-900 placeholder:text-ink-400 transition-colors duration-200 focus:outline-none focus:ring-4 disabled:cursor-not-allowed disabled:opacity-60 ${toneBorder(!!error)} ${className}`}
        {...rest}
      />
      {error && <span className="mt-1 block text-[12px] font-medium text-rose-600">{error}</span>}
      {!error && hint && <span className="mt-1 block text-[12px] font-medium text-ink-400">{hint}</span>}
    </label>
  );
});
