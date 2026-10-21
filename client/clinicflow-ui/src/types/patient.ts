export interface Patient {
  id: number;
  firstName: string;
  lastName: string;
}

export interface PatientCreateRequest {
  firstName: string;
  lastName: string;
}

export type PatientUpdateRequest = Partial<PatientCreateRequest>;

export interface PatientListParams {
  page: number;
  size: number;
}

export function displayName(patient: Pick<Patient, "firstName" | "lastName">): string {
  return `${patient.firstName} ${patient.lastName}`.trim();
}

// For the public display board in the gym: first name and last initial
// ("Maya Lopez" -> "Maya L."), so full names aren't shown to everyone. The console keeps full names.
export function boardName(fullName: string): string {
  const parts = fullName.trim().split(/\s+/);
  if (parts.length < 2) return parts[0] ?? "";
  return `${parts[0]} ${parts[parts.length - 1].charAt(0).toUpperCase()}.`;
}
