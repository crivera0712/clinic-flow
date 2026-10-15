import { screen } from "@testing-library/react";
import userEvent from "@testing-library/user-event";
import { vi } from "vitest";
import { renderWithTheme } from "../../test/render";
import { PatientsDirectory } from "./PatientsDirectory";
import { SchedulePage } from "./SchedulePage";
import { TherapistsManage } from "./TherapistsManage";

// Signed into the demo clinic: only check-in is allowed.
vi.mock("../../auth/AuthContext", () => ({ useIsDemo: () => true }));

vi.mock("../../services/appointmentService", () => ({
  listAppointmentsByDate: vi.fn().mockResolvedValue([
    {
      id: 8,
      scheduledAt: "2026-06-20T09:30",
      type: "EVALUATION",
      status: "SCHEDULED",
      patientId: 4,
      patientName: "Kim Vu",
      therapistId: 1,
      therapistName: "Dr. Smith",
    },
  ]),
  createAppointment: vi.fn(),
  updateAppointment: vi.fn(),
  removeAppointment: vi.fn(),
}));
vi.mock("../../services/patientService", () => ({
  listPatients: vi.fn().mockResolvedValue({
    rows: [{ id: 4, firstName: "Kim", lastName: "Vu" }],
    rowCount: 1,
  }),
  searchPatients: vi.fn().mockResolvedValue([]),
  createPatient: vi.fn(),
  updatePatient: vi.fn(),
}));
vi.mock("../../services/therapistService", () => ({
  listTherapists: vi.fn().mockResolvedValue([{ id: 1, name: "Dr. Smith" }]),
  createTherapist: vi.fn(),
  removeTherapist: vi.fn(),
}));

describe("demo clinic", () => {
  it("keeps check-in on the schedule but turns off adding, editing and cancelling", async () => {
    const user = userEvent.setup();
    renderWithTheme(<SchedulePage />);
    await screen.findByText("Kim Vu");

    expect(screen.getByRole("button", { name: "Arrived" })).toBeEnabled();
    expect(screen.getByRole("button", { name: "Add visit" })).toBeDisabled();

    await user.click(screen.getByRole("button", { name: "More actions for Kim Vu" }));
    expect(screen.getByRole("menuitem", { name: /Edit appointment/ })).toHaveAttribute(
      "aria-disabled",
      "true",
    );
    expect(screen.getByRole("menuitem", { name: /Cancel appointment/ })).toHaveAttribute(
      "aria-disabled",
      "true",
    );
  });

  it("turns off adding and editing patients", async () => {
    renderWithTheme(<PatientsDirectory />);
    await screen.findByText("1 patient");

    expect(screen.getByRole("button", { name: "Add patient" })).toBeDisabled();
    expect(screen.getByRole("button", { name: "Edit Kim Vu" })).toBeDisabled();
  });

  it("turns off adding and removing therapists", async () => {
    const user = userEvent.setup();
    renderWithTheme(<TherapistsManage />);
    await screen.findByRole("article", { name: "Dr. Smith" });

    expect(screen.getByRole("button", { name: "Add therapist" })).toBeDisabled();
    await user.click(screen.getByRole("button", { name: "More actions for Dr. Smith" }));
    expect(screen.getByRole("menuitem", { name: /Remove therapist/ })).toHaveAttribute(
      "aria-disabled",
      "true",
    );
  });
});
