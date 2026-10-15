import { apiRequest } from "./client";
import type {
  CurrentUser,
  JwtResponse,
  LoginRequest,
  RegisterRequest,
  RoleName,
} from "../types/auth";

export function login(request: LoginRequest) {
  return apiRequest<JwtResponse>("/auth/login", {
    method: "POST",
    body: request,
    skipAuth: true,
    retryOn401: false,
    statusMessages: { 401: "Username or password is incorrect." },
  });
}

// One-click sign-in to the shared demo clinic as its front desk (ADMIN) or display (DISPLAY)
// account. No password: the backend only allows this for the demo clinic.
export function loginAsDemo(role: RoleName) {
  return apiRequest<JwtResponse>(`/auth/demo/${role}`, {
    method: "POST",
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
