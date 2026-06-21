import { screen, waitFor } from "@testing-library/react";
import userEvent from "@testing-library/user-event";
import { beforeEach, vi } from "vitest";
import { renderWithTheme } from "../../test/render";
import { SchedulePage } from "./SchedulePage";

const listAppointmentsByDate = vi.fn();
const updateAppointment = vi.fn();
const removeAppointment = vi.fn();

vi.mock("../../services/appointmentService", () => ({
  listAppointmentsByDate: (...args: unknown[]) => listAppointmentsByDate(...args),
  createAppointment: vi.fn(),
  updateAppointment: (...args: unknown[]) => updateAppointment(...args),
  removeAppointment: (...args: unknown[]) => removeAppointment(...args),
}));
vi.mock("../../services/patientService", () => ({ searchPatients: vi.fn().mockResolvedValue([]) }));
vi.mock("../../services/therapistService", () => ({ listTherapists: vi.fn().mockResolvedValue([{ id: 1, name: "Dr. Smith" }]) }));

const row = {
  id: 8,
  scheduledAt: "2026-06-20T09:30",
  type: "EVALUATION" as const,
  status: "SCHEDULED" as const,
  patientId: 4,
  patientName: "Kim Vu",
  therapistId: 1,
  therapistName: "Dr. Smith",
};

describe("SchedulePage", () => {
  beforeEach(() => {
    vi.clearAllMocks();
    listAppointmentsByDate.mockResolvedValue([row]);
    updateAppointment.mockResolvedValue({ ...row, status: "WAITING" });
  });

  it("renders desktop and compact schedule presentations and preserves status actions", async () => {
    const user = userEvent.setup();
    renderWithTheme(<SchedulePage />);

    expect(await screen.findAllByText("Kim Vu")).toHaveLength(2);
    expect(screen.getByRole("table")).toBeInTheDocument();
    await user.click(screen.getAllByRole("button", { name: "Arrived" })[0]);

    await waitFor(() => expect(updateAppointment).toHaveBeenCalledWith(8, { status: "WAITING" }));
  });

  it("opens the edit and cancellation dialogs through accessible controls", async () => {
    const user = userEvent.setup();
    renderWithTheme(<SchedulePage />);

    await screen.findAllByText("Kim Vu");
    await user.click(screen.getAllByRole("button", { name: "Edit Kim Vu's appointment" })[0]);
    expect(screen.getByRole("dialog", { name: "Edit Appointment" })).toBeInTheDocument();
    await user.click(screen.getByRole("button", { name: "Cancel" }));
    await user.click(screen.getAllByRole("button", { name: "Cancel Kim Vu's appointment" })[0]);
    expect(screen.getByRole("dialog", { name: "Cancel appointment" })).toBeInTheDocument();
  });

  it("shows a clear empty state", async () => {
    listAppointmentsByDate.mockResolvedValue([]);
    renderWithTheme(<SchedulePage />);
    expect(await screen.findByText("The day is clear")).toBeInTheDocument();
  });

  it("surfaces schedule loading failures", async () => {
    listAppointmentsByDate.mockRejectedValue(new Error("Schedule unavailable"));
    renderWithTheme(<SchedulePage />);
    expect(await screen.findByRole("alert")).toHaveTextContent("Schedule unavailable");
  });
});
