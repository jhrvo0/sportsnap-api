import "./globals.css";
import { Navbar } from "@/components/Navbar";
import { AuthProvider } from "@/lib/auth";

export const metadata = {
  title: "SportSnap",
  description: "Performance esportiva real à fotografia profissional",
};

export const viewport = {
  themeColor: "#fafafc",
  colorScheme: "light",
};

export default function RootLayout({ children }: { children: React.ReactNode }) {
  return (
    <html lang="pt-BR" data-theme="light" style={{ colorScheme: "light" }}>
      <head>
        <meta name="color-scheme" content="light only" />
        <meta name="supported-color-schemes" content="light" />
      </head>
      <body className="bg-ink-50 text-ink-900">
        <AuthProvider>
          <Navbar />
          <main className="mx-auto max-w-6xl px-6 py-12">{children}</main>
          <footer className="border-t border-ink-100 py-10">
            <div className="mx-auto max-w-6xl px-6 text-[12px] text-ink-400">
              SportSnap · Ecossistema de performance esportiva e fotografia profissional
            </div>
          </footer>
        </AuthProvider>
      </body>
    </html>
  );
}
