export type AppointmentType = "EVALUATION" | "REASSESSMENT" | "FOLLOW_UP";

export type AppointmentStatus = "SCHEDULED" | "WAITING" | "DONE";

// Denormalized row used by both the gym board and the front-desk console.
export interface BoardRow {
  id: number;
  scheduledAt: string;
  type: AppointmentType;
  status: AppointmentStatus;
  patientId: number;
  patientName: string;
  therapistId: number;
  therapistName: string;
}

export interface AppointmentCreateRequest {
  therapistId: number;
  scheduledAt: string;
  type: AppointmentType;
  // Supply exactly one: an existing patient id, or new patient fields (created inline).
  patientId?: number;
  patient?: { firstName: string; lastName: string };
}

export interface AppointmentUpdateRequest {
  scheduledAt?: string;
  therapistId?: number;
  type?: AppointmentType;
  status?: AppointmentStatus;
}

export const appointmentTypeOptions: Array<{ value: AppointmentType; label: string }> = [
  { value: "EVALUATION", label: "Evaluation" },
  { value: "REASSESSMENT", label: "Reassessment" },
  { value: "FOLLOW_UP", label: "Follow Up" },
];

export const appointmentStatusLabels: Record<AppointmentStatus, string> = {
  SCHEDULED: "Scheduled",
  WAITING: "Waiting",
  DONE: "Done",
};
