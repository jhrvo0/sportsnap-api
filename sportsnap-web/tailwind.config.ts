import type { Config } from "tailwindcss";

const config: Config = {
  content: [
    "./src/pages/**/*.{js,ts,jsx,tsx,mdx}",
    "./src/components/**/*.{js,ts,jsx,tsx,mdx}",
    "./src/app/**/*.{js,ts,jsx,tsx,mdx}",
  ],
  theme: {
    extend: {
      colors: {
        brand: "#00FF7F",
        dark: {
          900: "#0a0a1a",
          800: "#12122a",
          700: "#1a1a2e",
          600: "#2d2d44",
        },
      },
    },
  },
  plugins: [],
};
export default config;
