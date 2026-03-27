import type { AppointmentDisplay } from "../types/appointment";
import { apiRequest } from "./client";

export function fetchAppointmentsByDate(date: string): Promise<AppointmentDisplay[]> {
  const params = new URLSearchParams({ date });
  return apiRequest<AppointmentDisplay[]>(`/appointments/date?${params.toString()}`);
}
