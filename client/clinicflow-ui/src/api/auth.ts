import { apiRequest } from "./client";
import type { CurrentUser, JwtResponse, LoginRequest, RegisterRequest } from "../types/auth";

export function login(request: LoginRequest) {
  return apiRequest<JwtResponse>("/auth/login", {
    method: "POST",
    body: request,
    skipAuth: true,
    retryOn401: false,
  });
}

export function register(clinicSlug: string, request: RegisterRequest) {
  return apiRequest<void>(`/auth/${clinicSlug}/register`, {
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
