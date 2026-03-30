export type Status =
  | "CHECKED_IN"
  | "IN_SESSION"
  | "FINISHED"
  | "SCHEDULED";

export type AppointmentType = "EVALUATION" | "REASSESMENT" | "FOLLOW_UP";

export interface AppointmentDisplay {
  aptId: number;
  scheduledAt: string;
  caseId: number;
  firstName: string;
  lastName: string;
  therapistId: number;
  therapistName: string;
  therapistType: string;
  bodyRegionDisplayName: string;
  status: Status;
  type: AppointmentType;
  displayName: string;
}
