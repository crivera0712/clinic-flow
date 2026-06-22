export interface Patient {
  id: number;
  firstName: string;
  lastName: string;
}

export interface PatientCreateRequest {
  firstName: string;
  lastName: string;
}

export interface PatientListParams {
  page: number;
  size: number;
}

export function displayName(patient: Pick<Patient, "firstName" | "lastName">): string {
  return `${patient.firstName} ${patient.lastName}`.trim();
}
