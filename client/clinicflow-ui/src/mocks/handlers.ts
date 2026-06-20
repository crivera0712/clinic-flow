import { http, HttpResponse } from "msw";
import type { AppointmentCreateRequest, AppointmentUpdateRequest } from "../types/appointment";
import type { PatientCreateRequest } from "../types/patient";
import type { TherapistCreateRequest } from "../types/therapist";
import { db } from "./db";

const api = (path: string) => `/api${path}`;

// A throwaway JWT (header.payload.sig) with a far-future exp so AuthContext's
// token parsing/proactive-refresh behaves naturally during the prototype.
function fakeToken(): string {
  const enc = (value: unknown) =>
    btoa(JSON.stringify(value)).replace(/=/g, "").replace(/\+/g, "-").replace(/\//g, "_");
  const header = enc({ alg: "none", typ: "JWT" });
  const payload = enc({ sub: "demo_admin", exp: Math.floor(Date.now() / 1000) + 60 * 60 * 8 });
  return `${header}.${payload}.sig`;
}

const demoAdmin = { id: 1, username: "demo_admin", roleName: "ADMIN", isDemo: false };

export const handlers = [
  // --- Auth: boot the app straight into an authenticated ADMIN session ---
  http.post(api("/auth/login"), () => HttpResponse.json({ token: fakeToken() })),
  http.post(api("/auth/refresh"), () => HttpResponse.json({ token: fakeToken() })),
  http.post(api("/auth/logout"), () => new HttpResponse(null, { status: 204 })),
  http.get(api("/auth/me"), () => HttpResponse.json(demoAdmin)),

  // --- Appointments ---
  http.get(api("/appointments/date"), ({ request }) => {
    const date = new URL(request.url).searchParams.get("date") ?? "";
    return HttpResponse.json(db.listAppointmentsByDate(date));
  }),
  http.post(api("/appointments"), async ({ request }) => {
    const payload = (await request.json()) as AppointmentCreateRequest;
    return HttpResponse.json(db.createAppointment(payload), { status: 201 });
  }),
  http.patch(api("/appointments/:id"), async ({ request, params }) => {
    const patch = (await request.json()) as AppointmentUpdateRequest;
    const row = db.updateAppointment(Number(params.id), patch);
    if (!row) return new HttpResponse(null, { status: 404 });
    return HttpResponse.json(row);
  }),
  http.delete(api("/appointments/:id"), ({ params }) => {
    db.removeAppointment(Number(params.id));
    return new HttpResponse(null, { status: 204 });
  }),

  // --- Patients ---
  http.get(api("/patients/search"), ({ request }) => {
    const q = new URL(request.url).searchParams.get("q") ?? "";
    return HttpResponse.json(db.searchPatients(q));
  }),
  http.get(api("/patients"), ({ request }) => {
    const url = new URL(request.url);
    const page = Number(url.searchParams.get("page") ?? 0);
    const size = Number(url.searchParams.get("size") ?? 20);
    return HttpResponse.json(db.listPatients(page, size));
  }),
  http.post(api("/patients"), async ({ request }) => {
    const payload = (await request.json()) as PatientCreateRequest;
    return HttpResponse.json(db.createPatient(payload.firstName, payload.lastName), { status: 201 });
  }),

  // --- Therapists ---
  http.get(api("/therapists"), () => HttpResponse.json(db.listTherapists())),
  http.post(api("/therapists"), async ({ request }) => {
    const payload = (await request.json()) as TherapistCreateRequest;
    return HttpResponse.json(db.createTherapist(payload.name), { status: 201 });
  }),
  http.delete(api("/therapists/:id"), ({ params }) => {
    db.removeTherapist(Number(params.id));
    return new HttpResponse(null, { status: 204 });
  }),
];
