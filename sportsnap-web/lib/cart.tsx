"use client";

import { createContext, useContext, useEffect, useState, ReactNode } from "react";
import { type FotoDto } from "./marketplace";

type CartContextType = {
  cart: FotoDto[];
  addToCart: (foto: FotoDto) => void;
  removeFromCart: (fotoId: number) => void;
  clearCart: () => void;
};

const CartContext = createContext<CartContextType | undefined>(undefined);

export function CartProvider({ children }: { children: ReactNode }) {
  const [cart, setCart] = useState<FotoDto[]>([]);

  useEffect(() => {
    const saved = localStorage.getItem("sportsnap_cart");
    if (saved) {
      try {
        setCart(JSON.parse(saved));
      } catch (e) {}
    }
  }, []);

  useEffect(() => {
    localStorage.setItem("sportsnap_cart", JSON.stringify(cart));
  }, [cart]);

  const addToCart = (foto: FotoDto) => {
    setCart((prev) => {
      if (prev.some((item) => item.id === foto.id)) return prev;
      return [...prev, foto];
    });
  };

  const removeFromCart = (fotoId: number) => {
    setCart((prev) => prev.filter((item) => item.id !== fotoId));
  };

  const clearCart = () => setCart([]);

  return (
    <CartContext.Provider value={{ cart, addToCart, removeFromCart, clearCart }}>
      {children}
    </CartContext.Provider>
  );
}

export function useCart() {
  const context = useContext(CartContext);
  if (!context) throw new Error("useCart must be used within a CartProvider");
  return context;
}
