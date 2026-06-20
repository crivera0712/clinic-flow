import { apiRequest } from "../api/client";
import type {
  AppointmentCreateRequest,
  AppointmentUpdateRequest,
  BoardRow,
} from "../types/appointment";

export function listAppointmentsByDate(date: string): Promise<BoardRow[]> {
  const params = new URLSearchParams({ date });
  return apiRequest<BoardRow[]>(`/appointments/date?${params.toString()}`);
}

export function createAppointment(payload: AppointmentCreateRequest): Promise<BoardRow> {
  return apiRequest<BoardRow>("/appointments", { method: "POST", body: payload });
}

export function updateAppointment(id: number, patch: AppointmentUpdateRequest): Promise<BoardRow> {
  return apiRequest<BoardRow>(`/appointments/${id}`, { method: "PATCH", body: patch });
}

export function removeAppointment(id: number): Promise<void> {
  return apiRequest<void>(`/appointments/${id}`, { method: "DELETE" });
}
