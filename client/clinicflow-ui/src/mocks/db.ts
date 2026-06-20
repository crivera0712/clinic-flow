import type { AppointmentCreateRequest, AppointmentUpdateRequest, BoardRow } from "../types/appointment";
import type { AppointmentStatus, AppointmentType } from "../types/appointment";
import type { PageResponse } from "../types/common";
import type { Patient } from "../types/patient";
import { displayName } from "../types/patient";
import type { Therapist } from "../types/therapist";

interface AppointmentRow {
  id: number;
  patientId: number;
  therapistId: number;
  scheduledAt: string; // "YYYY-MM-DDTHH:mm"
  type: AppointmentType;
  status: AppointmentStatus;
}

// "YYYY-MM-DDT HH:mm" for today at the given local time.
function todayAt(hour: number, minute: number): string {
  const d = new Date();
  const yyyy = d.getFullYear();
  const mm = String(d.getMonth() + 1).padStart(2, "0");
  const dd = String(d.getDate()).padStart(2, "0");
  const hh = String(hour).padStart(2, "0");
  const min = String(minute).padStart(2, "0");
  return `${yyyy}-${mm}-${dd}T${hh}:${min}`;
}

// In-memory seed data. Resets on page reload — fine for the prototype.
const patients: Patient[] = [
  { id: 1, firstName: "Jane", lastName: "Doe" },
  { id: 2, firstName: "Alan", lastName: "Ng" },
  { id: 3, firstName: "Mary", lastName: "Roe" },
  { id: 4, firstName: "Kim", lastName: "Vu" },
  { id: 5, firstName: "Pat", lastName: "Sims" },
  { id: 6, firstName: "Riku", lastName: "Ito" },
];

const therapists: Therapist[] = [
  { id: 1, name: "Dr. Smith" },
  { id: 2, name: "Dr. Lee" },
  { id: 3, name: "Dr. Patel" },
];

const appointments: AppointmentRow[] = [
  { id: 1, patientId: 3, therapistId: 1, scheduledAt: todayAt(9, 30), type: "EVALUATION", status: "WAITING" },
  { id: 2, patientId: 4, therapistId: 2, scheduledAt: todayAt(9, 30), type: "FOLLOW_UP", status: "WAITING" },
  { id: 3, patientId: 5, therapistId: 1, scheduledAt: todayAt(10, 0), type: "FOLLOW_UP", status: "SCHEDULED" },
  { id: 4, patientId: 6, therapistId: 2, scheduledAt: todayAt(10, 30), type: "REASSESSMENT", status: "SCHEDULED" },
  { id: 5, patientId: 1, therapistId: 3, scheduledAt: todayAt(11, 0), type: "EVALUATION", status: "SCHEDULED" },
  { id: 6, patientId: 2, therapistId: 3, scheduledAt: todayAt(8, 30), type: "FOLLOW_UP", status: "DONE" },
];

let nextPatientId = patients.length + 1;
let nextTherapistId = therapists.length + 1;
let nextAppointmentId = appointments.length + 1;

function toBoardRow(appt: AppointmentRow): BoardRow {
  const patient = patients.find((p) => p.id === appt.patientId);
  const therapist = therapists.find((t) => t.id === appt.therapistId);
  return {
    id: appt.id,
    scheduledAt: appt.scheduledAt,
    type: appt.type,
    status: appt.status,
    patientId: appt.patientId,
    patientName: patient ? displayName(patient) : `Patient #${appt.patientId}`,
    therapistId: appt.therapistId,
    therapistName: therapist ? therapist.name : `Therapist #${appt.therapistId}`,
  };
}

export const db = {
  searchPatients(query: string): Patient[] {
    const q = query.trim().toLowerCase();
    if (!q) return patients.slice(0, 10);
    return patients
      .filter((p) => displayName(p).toLowerCase().includes(q))
      .slice(0, 10);
  },

  listPatients(page: number, size: number): PageResponse<Patient> {
    const start = page * size;
    const content = patients.slice(start, start + size);
    return {
      content,
      page,
      size,
      totalElements: patients.length,
      totalPages: Math.max(1, Math.ceil(patients.length / size)),
      last: start + size >= patients.length,
    };
  },

  createPatient(firstName: string, lastName: string): Patient {
    const patient: Patient = { id: nextPatientId++, firstName, lastName };
    patients.push(patient);
    return patient;
  },

  listTherapists(): Therapist[] {
    return [...therapists];
  },

  createTherapist(name: string): Therapist {
    const therapist: Therapist = { id: nextTherapistId++, name };
    therapists.push(therapist);
    return therapist;
  },

  removeTherapist(id: number): void {
    const index = therapists.findIndex((t) => t.id === id);
    if (index >= 0) therapists.splice(index, 1);
  },

  listAppointmentsByDate(date: string): BoardRow[] {
    return appointments
      .filter((a) => a.scheduledAt.slice(0, 10) === date)
      .sort((a, b) => a.scheduledAt.localeCompare(b.scheduledAt))
      .map(toBoardRow);
  },

  createAppointment(payload: AppointmentCreateRequest): BoardRow {
    let patientId = payload.patientId;
    if (patientId === undefined && payload.patient) {
      patientId = db.createPatient(payload.patient.firstName, payload.patient.lastName).id;
    }
    const appt: AppointmentRow = {
      id: nextAppointmentId++,
      patientId: patientId ?? 0,
      therapistId: payload.therapistId,
      scheduledAt: payload.scheduledAt,
      type: payload.type,
      status: "SCHEDULED",
    };
    appointments.push(appt);
    return toBoardRow(appt);
  },

  updateAppointment(id: number, patch: AppointmentUpdateRequest): BoardRow | null {
    const appt = appointments.find((a) => a.id === id);
    if (!appt) return null;
    if (patch.scheduledAt !== undefined) appt.scheduledAt = patch.scheduledAt;
    if (patch.therapistId !== undefined) appt.therapistId = patch.therapistId;
    if (patch.type !== undefined) appt.type = patch.type;
    if (patch.status !== undefined) appt.status = patch.status;
    return toBoardRow(appt);
  },

  removeAppointment(id: number): void {
    const index = appointments.findIndex((a) => a.id === id);
    if (index >= 0) appointments.splice(index, 1);
  },
};
