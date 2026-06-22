import type { BoardRow } from "../types/appointment";
import type { PageResponse } from "../types/common";
import type { Patient } from "../types/patient";
import type { Therapist } from "../types/therapist";
import type { CurrentUser, JwtResponse } from "../types/auth";

export const patientFixture: Patient = { id: 1, firstName: "Jane", lastName: "Doe" };

export const therapistFixture: Therapist = { id: 7, name: "Dr. Smith" };

export const boardRowFixture: BoardRow = {
  id: 100,
  scheduledAt: "2026-06-20T14:30",
  type: "EVALUATION",
  status: "SCHEDULED",
  patientId: 1,
  patientName: "Jane Doe",
  therapistId: 7,
  therapistName: "Dr. Smith",
};

export const jwtFixture: JwtResponse = { token: "test-access-token" };

export const currentUserFixture: CurrentUser = {
  id: 1,
  username: "demo_admin",
  roleName: "ADMIN",
  isDemo: true,
};

// Wrap an array of rows in the backend's `PageResponse` envelope.
export function pageOf<T>(content: T[], totalElements = content.length): PageResponse<T> {
  const size = content.length || 1;
  return {
    content,
    page: 0,
    size,
    totalElements,
    totalPages: Math.max(1, Math.ceil(totalElements / size)),
    last: true,
  };
}
