/* eslint-disable react-refresh/only-export-components */
import { createContext, useContext, useEffect, useRef, useState, type ReactNode } from "react";
import { configureApiClient, resetApiClient } from "../api/client";
import {
  getCurrentUser,
  login as loginRequest,
  logout as logoutRequest,
  refresh as refreshRequest,
} from "../api/auth";
import type { CurrentUser, LoginRequest } from "../types/auth";

type AuthStatus = "bootstrapping" | "authenticated" | "anonymous";

const ACCESS_TOKEN_STORAGE_KEY = "clinicflow.accessToken";

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
  const didBootstrapRef = useRef(false);

  function readStoredAccessToken() {
    return sessionStorage.getItem(ACCESS_TOKEN_STORAGE_KEY);
  }

  function writeStoredAccessToken(token: string) {
    sessionStorage.setItem(ACCESS_TOKEN_STORAGE_KEY, token);
  }

  function clearStoredAccessToken() {
    sessionStorage.removeItem(ACCESS_TOKEN_STORAGE_KEY);
  }

  function parseJwtPayload(token: string) {
    try {
      const [, payload] = token.split(".");
      if (!payload) {
        return null;
      }

      const normalized = payload.replace(/-/g, "+").replace(/_/g, "/");
      const padded = normalized.padEnd(Math.ceil(normalized.length / 4) * 4, "=");
      return JSON.parse(window.atob(padded)) as { exp?: number };
    } catch {
      return null;
    }
  }

  function isTokenExpired(token: string) {
    const payload = parseJwtPayload(token);
    if (!payload?.exp) {
      return true;
    }

    return payload.exp * 1000 <= Date.now();
  }

  function setAccessTokenState(token: string | null) {
    accessTokenRef.current = token;
    setAccessToken(token);
    if (token) {
      writeStoredAccessToken(token);
    } else {
      clearStoredAccessToken();
    }
  }

  function clearAuthState() {
    setAccessTokenState(null);
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
        setAccessTokenState(response.token);
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
    setAccessTokenState(response.token);

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
    if (status !== "authenticated" || !accessToken) return;
    const payload = parseJwtPayload(accessToken);
    if (!payload?.exp) return;
    const msUntilRefresh = payload.exp * 1000 - Date.now() - 2 * 60 * 1000;
    if (msUntilRefresh <= 0) return;
    const id = setTimeout(() => {
      refresh();
    }, msUntilRefresh);
    return () => clearTimeout(id);
  }, [accessToken, status]);

  useEffect(() => {
    configureApiClient({
      getAccessToken: () => accessTokenRef.current,
      refreshAccessToken: refresh,
      onAuthFailure: clearAuthState,
    });

    return () => {
      resetApiClient();
    };
  }, []);

  useEffect(() => {
    if (didBootstrapRef.current) {
      return;
    }
    didBootstrapRef.current = true;

    let cancelled = false;

    async function bootstrap() {
      const storedToken = readStoredAccessToken();

      if (storedToken && !isTokenExpired(storedToken)) {
        setAccessTokenState(storedToken);

        try {
          await loadCurrentUser();
        } catch {
          if (!cancelled) {
            clearAuthState();
          }
        }
        return;
      }

      if (storedToken) {
        clearStoredAccessToken();
      }

      const token = await refresh();
      if (!token || cancelled) {
        if (!cancelled) {
          clearAuthState();
        }
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
