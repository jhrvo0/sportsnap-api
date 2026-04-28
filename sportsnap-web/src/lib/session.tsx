"use client";

import { createContext, useContext, useState, ReactNode } from "react";

type UserType = "ATLETA" | "FOTOGRAFO" | null;

interface Session {
  userType: UserType;
  userId: number | null;
  userName: string;
  setSession: (type: UserType, id: number, name: string) => void;
  clearSession: () => void;
}

const SessionContext = createContext<Session>({
  userType: null,
  userId: null,
  userName: "",
  setSession: () => {},
  clearSession: () => {},
});

export function SessionProvider({ children }: { children: ReactNode }) {
  const [userType, setUserType] = useState<UserType>(null);
  const [userId, setUserId] = useState<number | null>(null);
  const [userName, setUserName] = useState("");

  const setSession = (type: UserType, id: number, name: string) => {
    setUserType(type);
    setUserId(id);
    setUserName(name);
  };

  const clearSession = () => {
    setUserType(null);
    setUserId(null);
    setUserName("");
  };

  return (
    <SessionContext.Provider value={{ userType, userId, userName, setSession, clearSession }}>
      {children}
    </SessionContext.Provider>
  );
}

export const useSession = () => useContext(SessionContext);
