import { apiRequest } from "../api/client";
import type {
  EntityListResult,
  PageResponse,
  Patient,
  PatientCreateRequest,
  PatientListParams,
  PatientUpdateRequest,
} from "../types/admin";

function withDisplayName(patient: Omit<Patient, "displayName"> & { displayName?: string }): Patient {
  return {
    ...patient,
    displayName: `${patient.firstName} ${patient.lastName}`.trim(),
  };
}

export async function listPatients(params: PatientListParams): Promise<EntityListResult<Patient>> {
  if (params.search.trim()) {
    const response = await apiRequest<Array<Omit<Patient, "displayName">>>(`/patients/search?q=${encodeURIComponent(params.search.trim())}`);
    const rows = response.map(withDisplayName);
    return { rows, rowCount: rows.length };
  }

  const response = await apiRequest<PageResponse<Omit<Patient, "displayName">>>(
    `/patients?page=${params.page}&size=${params.size}`,
  );

  return {
    rows: response.content.map(withDisplayName),
    rowCount: response.totalElements,
  };
}

export function createPatient(payload: PatientCreateRequest) {
  return apiRequest<Patient>("/patients", { method: "POST", body: payload });
}

export function updatePatient(id: number, payload: PatientUpdateRequest) {
  return apiRequest<Patient>(`/patients/${id}`, { method: "PATCH", body: payload });
}

export function removePatient(id: number) {
  return apiRequest<void>(`/patients/${id}`, { method: "DELETE" });
}
