import { createContext, useContext, useEffect, useRef, useState, type ReactNode } from "react";
import { configureApiClient, resetApiClient } from "../api/client";
import { getCurrentUser, login as loginRequest, logout as logoutRequest, refresh as refreshRequest } from "../api/auth";
import type { CurrentUser, LoginRequest } from "../types/auth";

type AuthStatus = "bootstrapping" | "authenticated" | "anonymous";

type AuthContextValue = {
  accessToken: string | null;
  currentUser: CurrentUser | null;
  status: AuthStatus;
  login: (request: LoginRequest) => Promise<void>;
  logout: () => Promise<void>;
  refresh: () => Promise<string | null>;
};

const AuthContext = createContext<AuthContextValue | null>(null);

function useProvideAuth(): AuthContextValue {
  const [accessToken, setAccessToken] = useState<string | null>(null);
  const [currentUser, setCurrentUser] = useState<CurrentUser | null>(null);
  const [status, setStatus] = useState<AuthStatus>("bootstrapping");
  const accessTokenRef = useRef<string | null>(null);
  const refreshPromiseRef = useRef<Promise<string | null> | null>(null);

  useEffect(() => {
    accessTokenRef.current = accessToken;
  }, [accessToken]);

  function clearAuthState() {
    setAccessToken(null);
    setCurrentUser(null);
    setStatus("anonymous");
  }

  async function loadCurrentUser() {
    const user = await getCurrentUser();
    setCurrentUser(user);
    setStatus("authenticated");
    return user;
  }

  async function refresh() {
    if (refreshPromiseRef.current) {
      return refreshPromiseRef.current;
    }

    refreshPromiseRef.current = (async () => {
      try {
        const response = await refreshRequest();
        setAccessToken(response.token);
        return response.token;
      } catch {
        clearAuthState();
        return null;
      } finally {
        refreshPromiseRef.current = null;
      }
    })();

    return refreshPromiseRef.current;
  }

  async function login(request: LoginRequest) {
    const response = await loginRequest(request);
    setAccessToken(response.token);

    try {
      await loadCurrentUser();
    } catch (error) {
      clearAuthState();
      throw error;
    }
  }

  async function logout() {
    try {
      await logoutRequest();
    } catch {
      // Local cleanup still needs to happen when the backend session is already gone.
    } finally {
      clearAuthState();
    }
  }

  useEffect(() => {
    configureApiClient({
      getAccessToken: () => accessTokenRef.current,
      refreshAccessToken: refresh,
      onAuthFailure: clearAuthState,
    });

    return () => {
      resetApiClient();
    };
  });

  useEffect(() => {
    let cancelled = false;

    async function bootstrap() {
      const token = await refresh();
      if (!token || cancelled) {
        return;
      }

      try {
        await loadCurrentUser();
      } catch {
        if (!cancelled) {
          clearAuthState();
        }
      }
    }

    bootstrap();

    return () => {
      cancelled = true;
    };
  }, []);

  return { accessToken, currentUser, status, login, logout, refresh };
}

export function AuthProvider({ children }: { children: ReactNode }) {
  const value = useProvideAuth();
  return <AuthContext.Provider value={value}>{children}</AuthContext.Provider>;
}

export function useAuth() {
  const context = useContext(AuthContext);
  if (!context) {
    throw new Error("useAuth must be used within an AuthProvider");
  }

  return context;
}
