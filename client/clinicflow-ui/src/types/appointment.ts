// appointments.ts
export type Status =
    |"CHECKED_IN"
    |"IN_SESSION"
    |"FINISHED"
    |"SCHEDULED";

export interface appointmentDisplay {
    aptId: long,
    scheduledAt: string,
    caseId: long,
    firstName: string
    lastName: string
    therapistId: string
    therapistName: string
    therapistType: string
    bodyRegionDisplayName: string
    status: Status
}