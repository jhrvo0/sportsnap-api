import type { Config } from "tailwindcss";

const config: Config = {
  content: ["./app/**/*.{ts,tsx}", "./components/**/*.{ts,tsx}"],
  theme: {
    extend: {
      colors: {
        ink: {
          900: "#111113",
          800: "#1d1d1f",
          700: "#2d2d2f",
          600: "#4b4b4f",
          500: "#6e6e73",
          400: "#86868b",
          300: "#a8a8ad",
          200: "#d2d2d7",
          100: "#e5e5ea",
          50: "#f5f5f7",
        },
        accent: {
          DEFAULT: "#0a84ff",
          50: "#e6f2ff",
          100: "#cce5ff",
          500: "#0a84ff",
          600: "#0071e3",
          700: "#005bb5",
        },
      },
      fontFamily: {
        display: [
          "-apple-system",
          "BlinkMacSystemFont",
          "SF Pro Display",
          "Inter",
          "Helvetica Neue",
          "sans-serif",
        ],
      },
      borderRadius: {
        "2xl": "1.25rem",
        "3xl": "1.75rem",
      },
      boxShadow: {
        soft: "0 1px 2px rgba(0,0,0,0.04), 0 4px 12px rgba(0,0,0,0.04)",
        ring: "0 0 0 4px rgba(10,132,255,0.15)",
      },
    },
  },
  plugins: [],
};

export default config;
