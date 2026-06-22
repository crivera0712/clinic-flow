import { http, HttpResponse } from "msw";
import { apiUrl } from "../constants";
import {
  boardRowFixture,
  currentUserFixture,
  jwtFixture,
  pageOf,
  patientFixture,
  therapistFixture,
} from "../fixtures";

// Default happy-path handlers. API-layer tests override these per-case with
// `server.use(...)`; they exist as a realistic baseline for future component tests.
export const handlers = [
  // Auth
  http.post(apiUrl("/auth/login"), () => HttpResponse.json(jwtFixture)),
  http.post(apiUrl("/auth/refresh"), () => HttpResponse.json(jwtFixture)),
  http.post(apiUrl("/auth/logout"), () => new HttpResponse(null, { status: 204 })),
  http.get(apiUrl("/auth/me"), () => HttpResponse.json(currentUserFixture)),

  // Entities
  http.get(apiUrl("/appointments/date"), () => HttpResponse.json([boardRowFixture])),
  http.get(apiUrl("/patients/search"), () => HttpResponse.json([patientFixture])),
  http.get(apiUrl("/patients"), () => HttpResponse.json(pageOf([patientFixture]))),
  http.get(apiUrl("/therapists"), () => HttpResponse.json([therapistFixture])),
];
