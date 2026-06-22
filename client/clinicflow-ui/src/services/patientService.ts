import { apiRequest } from "../api/client";
import type { EntityListResult, PageResponse } from "../types/common";
import type { Patient, PatientCreateRequest, PatientListParams } from "../types/patient";

export function searchPatients(query: string): Promise<Patient[]> {
  return apiRequest<Patient[]>(`/patients/search?q=${encodeURIComponent(query.trim())}`);
}

export async function listPatients(params: PatientListParams): Promise<EntityListResult<Patient>> {
  const response = await apiRequest<PageResponse<Patient>>(
    `/patients?page=${params.page}&size=${params.size}`,
  );
  return { rows: response.content, rowCount: response.totalElements };
}

export function createPatient(payload: PatientCreateRequest): Promise<Patient> {
  return apiRequest<Patient>("/patients", { method: "POST", body: payload });
}
