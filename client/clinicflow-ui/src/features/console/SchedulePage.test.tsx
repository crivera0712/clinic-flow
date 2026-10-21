import { fireEvent, screen, waitFor, within } from "@testing-library/react";
import userEvent from "@testing-library/user-event";
import { beforeEach, vi } from "vitest";
import { renderWithTheme } from "../../test/render";
import type { BoardRow } from "../../types/appointment";
import { SchedulePage } from "./SchedulePage";
import { getTodayDateString, shiftDate } from "./scheduleUtils";

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
vi.mock("../../services/therapistService", () => ({
  listTherapists: vi.fn().mockResolvedValue([{ id: 1, name: "Dr. Smith" }]),
}));

const row: BoardRow = {
  id: 8,
  scheduledAt: "2026-06-20T09:30",
  type: "EVALUATION",
  status: "SCHEDULED",
  patientId: 4,
  patientName: "Kim Vu",
  therapistId: 1,
  therapistName: "Dr. Smith",
};

const mixedDay: BoardRow[] = [
  row,
  { ...row, id: 9, patientName: "Ana Ruiz", status: "WAITING", scheduledAt: "2026-06-20T10:00" },
  { ...row, id: 10, patientName: "Bo Chen", status: "WAITING", scheduledAt: "2026-06-20T10:00" },
  { ...row, id: 11, patientName: "Cy Moss", status: "DONE", scheduledAt: "2026-06-20T08:00" },
];

describe("SchedulePage", () => {
  beforeEach(() => {
    vi.clearAllMocks();
    listAppointmentsByDate.mockResolvedValue([row]);
    updateAppointment.mockResolvedValue({ ...row, status: "WAITING" });
  });

  it("checks a patient in with the row's single primary action", async () => {
    const user = userEvent.setup();
    renderWithTheme(<SchedulePage />);

    expect(await screen.findByText("Kim Vu")).toBeInTheDocument();
    await user.click(screen.getByRole("button", { name: "Arrived" }));

    await waitFor(() => expect(updateAppointment).toHaveBeenCalledWith(8, { status: "WAITING" }));
  });

  it("opens the edit drawer and cancellation dialog from the row menu", async () => {
    const user = userEvent.setup();
    renderWithTheme(<SchedulePage />);
    await screen.findByText("Kim Vu");

    await user.click(screen.getByRole("button", { name: "More actions for Kim Vu" }));
    await user.click(screen.getByRole("menuitem", { name: "Edit appointment" }));
    const drawer = screen.getByRole("dialog", { name: "Edit appointment" });
    expect(drawer).toHaveTextContent("Kim Vu");
    await user.click(within(drawer).getByRole("button", { name: "Cancel" }));

    await user.click(screen.getByRole("button", { name: "More actions for Kim Vu" }));
    await user.click(screen.getByRole("menuitem", { name: "Cancel appointment" }));
    expect(screen.getByRole("dialog", { name: "Cancel appointment" })).toBeInTheDocument();
  });

  it("groups visits by time and filters them from the summary cards", async () => {
    const user = userEvent.setup();
    listAppointmentsByDate.mockResolvedValue(mixedDay);
    renderWithTheme(<SchedulePage />);
    await screen.findByText("Kim Vu");

    // Grouped in time order: 8:00, 9:30, 10:00 (two visits).
    const groups = screen.getAllByRole("region", { name: /AM$/ });
    expect(groups.map((group) => group.getAttribute("aria-label"))).toEqual([
      "8:00 AM",
      "9:30 AM",
      "10:00 AM",
    ]);
    expect(within(groups[2]).getByText("Ana Ruiz")).toBeInTheDocument();
    expect(within(groups[2]).getByText("Bo Chen")).toBeInTheDocument();
    // A finished visit has no primary action.
    expect(within(groups[0]).queryByRole("button", { name: /Arrived|Start visit/ })).toBeNull();

    const waiting = screen.getByRole("button", { name: /Waiting\s*2/ });
    await user.click(waiting);
    expect(waiting).toHaveAttribute("aria-pressed", "true");
    expect(screen.queryByText("Kim Vu")).not.toBeInTheDocument();
    expect(screen.getByText("Ana Ruiz")).toBeInTheDocument();

    // Clicking the active filter again shows everyone.
    await user.click(waiting);
    expect(screen.getByText("Kim Vu")).toBeInTheDocument();
  });

  it("steps between days and back to today", async () => {
    const user = userEvent.setup();
    const today = getTodayDateString();
    renderWithTheme(<SchedulePage />);
    await screen.findByText("Kim Vu");
    expect(screen.getByRole("button", { name: "Today" })).toBeDisabled();

    await user.click(screen.getByRole("button", { name: "Next day" }));
    await waitFor(() => expect(listAppointmentsByDate).toHaveBeenCalledWith(shiftDate(today, 1)));
    expect(screen.getByRole("button", { name: "Today" })).toBeEnabled();

    await user.click(screen.getByRole("button", { name: "Today" }));
    expect(screen.getByRole("button", { name: "Today" })).toBeDisabled();
  });

  it("focuses Quick add when N is pressed outside a field", async () => {
    const user = userEvent.setup();
    renderWithTheme(<SchedulePage />);
    await screen.findByText("Kim Vu");

    await user.keyboard("n");

    expect(screen.getByRole("combobox", { name: "Patient" })).toHaveFocus();
  });

  it("keeps a typed new patient name when moving to the next field", async () => {
    const user = userEvent.setup();
    renderWithTheme(<SchedulePage />);
    await screen.findByText("Kim Vu");

    const patient = screen.getByRole("combobox", { name: "Patient" });
    await user.type(patient, "Iris Bell");
    await user.tab();

    expect(patient).toHaveValue("Iris Bell");
  });

  it("keeps the previous day dimmed with actions disabled while a new date loads", async () => {
    // The new date never answers, so the page stays in its "loading the next day" state.
    listAppointmentsByDate.mockImplementation((date: string) =>
      date === "2000-01-01" ? new Promise(() => undefined) : Promise.resolve([row]),
    );
    renderWithTheme(<SchedulePage />);
    await screen.findByText("Kim Vu");

    fireEvent.change(screen.getByLabelText("Date"), { target: { value: "2000-01-01" } });

    expect(await screen.findByText("Loading Jan 1…")).toBeInTheDocument();
    expect(screen.getByText("Kim Vu")).toBeInTheDocument();
    expect(screen.getByRole("button", { name: "Arrived" })).toBeDisabled();
    expect(screen.getByRole("button", { name: "More actions for Kim Vu" })).toBeDisabled();
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
    expect(screen.queryByText("The day is clear")).not.toBeInTheDocument();
  });
});
