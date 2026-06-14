import { forwardRef, type ButtonHTMLAttributes } from "react";

type Variant = "primary" | "secondary" | "ghost" | "danger" | "accent";
type Size = "sm" | "md" | "lg" | "icon";

type Props = ButtonHTMLAttributes<HTMLButtonElement> & {
  variant?: Variant;
  size?: Size;
};

const variantClasses: Record<Variant, string> = {
  primary: "bg-ink-900 text-white hover:bg-ink-800 active:scale-[0.98] shadow-sm",
  secondary: "bg-white border border-ink-100 text-ink-900 hover:bg-ink-50 active:scale-[0.98] shadow-sm",
  ghost: "text-ink-600 hover:bg-ink-100/50 hover:text-ink-900 active:scale-[0.98]",
  danger: "bg-rose-500 text-white hover:bg-rose-600 active:scale-[0.98] shadow-sm",
  accent: "bg-accent text-white hover:bg-accent-600 active:scale-[0.98] shadow-lg shadow-accent/20",
};

const sizeClasses: Record<Size, string> = {
  sm: "h-9 px-4 text-[12px] font-bold tracking-tight",
  md: "h-11 px-6 text-[14px] font-bold tracking-tight",
  lg: "h-14 px-8 text-[16px] font-black tracking-tight",
  icon: "h-10 w-10 p-0 text-[14px] font-bold",
};

export const Button = forwardRef<HTMLButtonElement, Props>(function Button(
  { variant = "primary", size = "md", className = "", ...rest },
  ref,
) {
  return (
    <button
      ref={ref}
      className={`inline-flex items-center justify-center gap-2 rounded-full transition-all duration-200 disabled:cursor-not-allowed disabled:opacity-50 ${variantClasses[variant]} ${sizeClasses[size]} ${className}`}
      {...rest}
    />
  );
});
