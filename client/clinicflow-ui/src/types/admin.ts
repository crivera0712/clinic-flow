export interface EntityListResult<T> {
  rows: T[];
  rowCount: number;
}

export interface PageResponse<T> {
  content: T[];
  page: number;
  size: number;
  totalElements: number;
  totalPages: number;
  last: boolean;
}

export interface BodyRegion {
  id: number;
  code: string;
  displayName: string;
  isActive: boolean;
}

export interface Patient {
  id: number;
  firstName: string;
  lastName: string;
  displayName: string;
}

export interface Therapist {
  id: number;
  therapistName: string;
  type: string;
}

export interface CaseSummary {
  id: number;
  patientId: number;
  bodyRegionId: number;
  createdAt: string;
}

export type AppointmentStatusValue = "CHECKED_IN" | "IN_SESSION" | "FINISHED" | "SCHEDULED";
export type AppointmentTypeValue = "EVALUATION" | "REASSESSMENT" | "FOLLOW_UP";

export interface AppointmentRecord {
  id: number;
  scheduledAt: string;
  modifiedAt: string | null;
  caseId: number;
  therapistId: number;
  status: AppointmentStatusValue;
  type: AppointmentTypeValue;
}

export interface BodyRegionCreateRequest {
  code: string;
  displayName: string;
}

export interface BodyRegionUpdateRequest {
  code?: string;
  isActive?: boolean;
}

export interface PatientListParams {
  page: number;
  size: number;
  search: string;
}

export interface PatientCreateRequest {
  firstName: string;
  lastName: string;
}

export interface PatientUpdateRequest {
  firstName?: string;
  lastName?: string;
}

export interface CaseCreateRequest {
  patientId: number;
  bodyRegionId: number;
}

export interface CaseUpdateRequest {
  bodyRegionId: number;
}

export interface AppointmentListParams {
  date?: string;
  caseId?: number;
  page?: number;
  size?: number;
}

export interface AppointmentCreateRequest {
  scheduledAt: string;
  caseId: number;
  therapistId: number;
  status: AppointmentStatusValue;
  type: AppointmentTypeValue;
}

export interface AppointmentUpdateRequest {
  scheduledAt?: string;
  caseId?: number;
  therapistId?: number;
  status?: AppointmentStatusValue;
  type?: AppointmentTypeValue;
}

export type TherapistTypeValue =
  | "PHYSICAL_THERAPIST"
  | "OCCUPATIONAL_THERAPIST"
  | "PHYSICAL_THERAPY_ASSISTANT";

export interface TherapistCreateRequest {
  therapistName: string;
  type: TherapistTypeValue;
}

export interface TherapistUpdateRequest {
  therapistName?: string;
  therapistType?: TherapistTypeValue;
}

export const therapistTypeOptions: Array<{ value: TherapistTypeValue; label: string }> = [
  { value: "PHYSICAL_THERAPIST", label: "Physical Therapist" },
  { value: "OCCUPATIONAL_THERAPIST", label: "Occupational Therapist" },
  { value: "PHYSICAL_THERAPY_ASSISTANT", label: "Physical Therapy Assistant" },
];

export const appointmentStatusOptions: Array<{ value: AppointmentStatusValue; label: string }> = [
  { value: "SCHEDULED", label: "Scheduled" },
  { value: "CHECKED_IN", label: "Checked In" },
  { value: "IN_SESSION", label: "In Session" },
  { value: "FINISHED", label: "Finished" },
];

export const appointmentTypeOptions: Array<{ value: AppointmentTypeValue; label: string }> = [
  { value: "EVALUATION", label: "Evaluation" },
  { value: "REASSESSMENT", label: "Reassessment" },
  { value: "FOLLOW_UP", label: "Follow Up" },
];
