import { MemoryRouter } from "react-router-dom";
import { screen } from "@testing-library/react";
import { vi } from "vitest";
import ScheduleDisplayPage from "./ScheduleDisplayPage";
import { renderWithTheme } from "./test/render";

vi.mock("./auth/AuthContext", () => ({
  useAuth: () => ({ currentUser: { username: "demo_admin", roleName: "ADMIN" } }),
}));

vi.mock("./services/appointmentService", () => ({
  listAppointmentsByDate: vi.fn().mockResolvedValue([
    { id: 1, scheduledAt: "2026-06-20T09:30", type: "EVALUATION", status: "WAITING", patientId: 1, patientName: "Mary Roe", therapistId: 1, therapistName: "Dr. Smith" },
    { id: 2, scheduledAt: "2026-06-20T10:00", type: "FOLLOW_UP", status: "SCHEDULED", patientId: 2, patientName: "Pat Sims", therapistId: 2, therapistName: "Dr. Lee" },
  ]),
}));

describe("ScheduleDisplayPage", () => {
  it("separates waiting patients from upcoming appointments", async () => {
    renderWithTheme(<MemoryRouter><ScheduleDisplayPage /></MemoryRouter>);

    expect(screen.getByRole("heading", { name: "Currently waiting" })).toBeInTheDocument();
    expect(screen.getByRole("heading", { name: "Up next" })).toBeInTheDocument();
    expect(screen.getByRole("heading", { name: "Mill Valley Physical Therapy" })).toBeInTheDocument();
    expect(screen.queryByText("Today’s patient flow")).not.toBeInTheDocument();
    expect(await screen.findByText("Mary Roe")).toBeInTheDocument();
    expect(await screen.findByText("Pat Sims")).toBeInTheDocument();
    expect(screen.getByRole("button", { name: /Console/ })).toBeInTheDocument();
  });
});
