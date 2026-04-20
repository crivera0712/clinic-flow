import { apiRequest } from "../api/client";
import type {
  AppointmentCreateRequest,
  AppointmentListParams,
  AppointmentRecord,
  AppointmentUpdateRequest,
  EntityListResult,
  PageResponse,
} from "../types/admin";

function toRecord(row: AppointmentRecord): AppointmentRecord {
  return row;
}

export async function listAppointments(params: AppointmentListParams): Promise<EntityListResult<AppointmentRecord>> {
  const searchParams = new URLSearchParams({
    page: String(params.page ?? 0),
    size: String(params.size ?? 1000),
  });
  if (params.date) searchParams.set("date", params.date);
  if (params.caseId !== undefined) searchParams.set("caseId", String(params.caseId));
  const response = await apiRequest<PageResponse<AppointmentRecord>>(`/appointments?${searchParams.toString()}`);
  const rows = response.content.map(toRecord);
  return { rows, rowCount: response.totalElements };
}

export function createAppointment(payload: AppointmentCreateRequest) {
  return apiRequest<AppointmentRecord>("/appointments", { method: "POST", body: payload });
}

export function updateAppointment(id: number, payload: AppointmentUpdateRequest) {
  return apiRequest<AppointmentRecord>(`/appointments/${id}`, { method: "PATCH", body: payload });
}

export function removeAppointment(id: number) {
  return apiRequest<void>(`/appointments/${id}`, { method: "DELETE" });
}

export async function listAppointmentsByTherapist(
  therapistId: number,
  params: { date?: string; page?: number; size?: number } = {},
): Promise<EntityListResult<AppointmentRecord>> {
  const searchParams = new URLSearchParams({
    page: String(params.page ?? 0),
    size: String(params.size ?? 1000),
  });
  if (params.date) searchParams.set("date", params.date);
  const response = await apiRequest<PageResponse<AppointmentRecord>>(
    `/appointments/therapist/${therapistId}?${searchParams.toString()}`,
  );
  return { rows: response.content, rowCount: response.totalElements };
}
