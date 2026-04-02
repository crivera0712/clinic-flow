import { apiRequest } from "../api/client";
import type { CaseCreateRequest, CaseSummary, CaseUpdateRequest, EntityListResult } from "../types/admin";

export async function listCases(): Promise<EntityListResult<CaseSummary>> {
  const response = await apiRequest<CaseSummary[]>("/cases");
  return { rows: response, rowCount: response.length };
}

export function listCasesByPatient(patientId: number) {
  return apiRequest<CaseSummary[]>(`/cases/patient/${patientId}`);
}

export function getCaseById(id: number) {
  return apiRequest<CaseSummary>(`/cases/${id}`);
}

export function createCase(payload: CaseCreateRequest) {
  return apiRequest<CaseSummary>("/cases", { method: "POST", body: payload });
}

export function updateCase(id: number, payload: CaseUpdateRequest) {
  return apiRequest<CaseSummary>(`/cases/${id}`, { method: "PATCH", body: payload });
}

export function removeCase(id: number) {
  return apiRequest<void>(`/cases/${id}`, { method: "DELETE" });
}
