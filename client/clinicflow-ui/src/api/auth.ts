import { apiRequest } from "./client";
import type { CurrentUser, JwtResponse, LoginRequest } from "../types/auth";

const clinicSlug = import.meta.env.VITE_CLINIC_SLUG || "demo";

export function login(request: LoginRequest) {
  return apiRequest<JwtResponse>(`/auth/${clinicSlug}/login`, {
    method: "POST",
    body: request,
    skipAuth: true,
    retryOn401: false,
  });
}

export function refresh() {
  return apiRequest<JwtResponse>("/auth/refresh", {
    method: "POST",
    skipAuth: true,
    retryOn401: false,
  });
}

export function logout() {
  return apiRequest<void>("/auth/logout", {
    method: "POST",
  });
}

export function getCurrentUser() {
  return apiRequest<CurrentUser>("/auth/me");
}
