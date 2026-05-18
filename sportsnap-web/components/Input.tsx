import { forwardRef, type InputHTMLAttributes, type SelectHTMLAttributes, type TextareaHTMLAttributes } from "react";

type FieldProps = {
  label?: string;
  hint?: string;
  error?: string;
};

const baseInput =
  "h-11 w-full rounded-xl border border-ink-200 bg-white px-4 text-[15px] text-ink-900 placeholder:text-ink-400 transition focus:border-accent focus:outline-none focus:ring-4 focus:ring-accent/20";

export const Input = forwardRef<
  HTMLInputElement,
  InputHTMLAttributes<HTMLInputElement> & FieldProps
>(function Input({ label, hint, error, className = "", ...rest }, ref) {
  return (
    <label className="block">
      {label && <span className="mb-1.5 block text-[13px] font-medium text-ink-700">{label}</span>}
      <input ref={ref} className={`${baseInput} ${className}`} {...rest} />
      {error && <span className="mt-1 block text-[12px] text-rose-600">{error}</span>}
      {!error && hint && <span className="mt-1 block text-[12px] text-ink-400">{hint}</span>}
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
      <select ref={ref} className={`${baseInput} appearance-none ${className}`} {...rest}>
        {children}
      </select>
      {error && <span className="mt-1 block text-[12px] text-rose-600">{error}</span>}
      {!error && hint && <span className="mt-1 block text-[12px] text-ink-400">{hint}</span>}
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
        className={`min-h-[80px] w-full rounded-xl border border-ink-200 bg-white px-4 py-3 text-[15px] text-ink-900 placeholder:text-ink-400 transition focus:border-accent focus:outline-none focus:ring-4 focus:ring-accent/20 ${className}`}
        {...rest}
      />
      {error && <span className="mt-1 block text-[12px] text-rose-600">{error}</span>}
      {!error && hint && <span className="mt-1 block text-[12px] text-ink-400">{hint}</span>}
    </label>
  );
});
