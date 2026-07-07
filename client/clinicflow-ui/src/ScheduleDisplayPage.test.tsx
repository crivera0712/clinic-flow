import { MemoryRouter } from "react-router-dom";
import { screen } from "@testing-library/react";
import { vi } from "vitest";
import ScheduleDisplayPage from "./ScheduleDisplayPage";
import { buildTherapistAccentMap } from "./features/display/therapistAccents";
import { renderWithTheme } from "./test/render";

vi.mock("./auth/AuthContext", () => ({
  useAuth: () => ({ currentUser: { username: "demo_admin", roleName: "ADMIN" } }),
}));

vi.mock("./services/appointmentService", () => ({
  listAppointmentsByDate: vi.fn().mockResolvedValue([
    {
      id: 1,
      scheduledAt: "2026-06-20T09:30",
      type: "EVALUATION",
      status: "WAITING",
      patientId: 1,
      patientName: "Mary Roe",
      therapistId: 1,
      therapistName: "Dr. Smith",
    },
    {
      id: 2,
      scheduledAt: "2026-06-20T10:00",
      type: "FOLLOW_UP",
      status: "SCHEDULED",
      patientId: 2,
      patientName: "Pat Sims",
      therapistId: 2,
      therapistName: "Dr. Lee",
    },
  ]),
}));

describe("ScheduleDisplayPage", () => {
  it("assigns one unique accent to each therapist regardless of database id gaps", () => {
    const appointments = [
      {
        id: 1,
        scheduledAt: "2026-06-20T09:30",
        type: "EVALUATION" as const,
        status: "WAITING" as const,
        patientId: 1,
        patientName: "Mary Roe",
        therapistId: 1,
        therapistName: "Dr. Smith",
      },
      {
        id: 2,
        scheduledAt: "2026-06-20T10:00",
        type: "FOLLOW_UP" as const,
        status: "SCHEDULED" as const,
        patientId: 2,
        patientName: "Pat Sims",
        therapistId: 9,
        therapistName: "Dr. Lee",
      },
      {
        id: 3,
        scheduledAt: "2026-06-20T10:30",
        type: "FOLLOW_UP" as const,
        status: "SCHEDULED" as const,
        patientId: 3,
        patientName: "Jane Doe",
        therapistId: 1,
        therapistName: "Dr. Smith",
      },
    ];

    const accents = buildTherapistAccentMap(appointments);

    expect(accents.get(1)).toBeDefined();
    expect(accents.get(9)).toBeDefined();
    expect(accents.get(1)).not.toBe(accents.get(9));
    expect(new Set(accents.values()).size).toBe(accents.size);
  });

  it("separates waiting patients from upcoming appointments", async () => {
    renderWithTheme(
      <MemoryRouter>
        <ScheduleDisplayPage />
      </MemoryRouter>,
    );

    expect(screen.getByRole("heading", { name: "Currently waiting" })).toBeInTheDocument();
    expect(screen.getByRole("heading", { name: "Up next" })).toBeInTheDocument();
    expect(
      screen.getByRole("heading", { name: "Mill Valley Physical Therapy" }),
    ).toBeInTheDocument();
    expect(screen.queryByText("Today’s patient flow")).not.toBeInTheDocument();
    expect(await screen.findByText("Mary Roe")).toBeInTheDocument();
    expect(await screen.findByText("Pat Sims")).toBeInTheDocument();
    expect(screen.getByRole("button", { name: /Console/ })).toBeInTheDocument();
  });
});
