import { screen, waitFor, within } from "@testing-library/react";
import userEvent from "@testing-library/user-event";
import { beforeEach, vi } from "vitest";
import { renderWithTheme } from "../../test/render";
import { TherapistsManage } from "./TherapistsManage";

const listTherapists = vi.fn();
const createTherapist = vi.fn();
const removeTherapist = vi.fn();

vi.mock("../../services/therapistService", () => ({
  listTherapists: (...args: unknown[]) => listTherapists(...args),
  createTherapist: (...args: unknown[]) => createTherapist(...args),
  removeTherapist: (...args: unknown[]) => removeTherapist(...args),
}));

// vi.mock factories are hoisted above imports, so their data must be hoisted too.
const todaysVisits = vi.hoisted(() => {
  const visit = {
    scheduledAt: "2026-06-20T09:00",
    type: "FOLLOW_UP" as const,
    patientId: 1,
    patientName: "Maya Lopez",
    therapistId: 9,
    therapistName: "Dana Ortiz",
  };
  return [
    { ...visit, id: 1, status: "WAITING" as const },
    { ...visit, id: 2, status: "SCHEDULED" as const },
  ];
});
vi.mock("../../services/appointmentService", () => ({
  listAppointmentsByDate: vi.fn().mockResolvedValue(todaysVisits),
}));

describe("TherapistsManage", () => {
  beforeEach(() => {
    vi.clearAllMocks();
    listTherapists.mockResolvedValue([
      { id: 9, name: "Dana Ortiz" },
      { id: 10, name: "Sam Patel" },
    ]);
    createTherapist.mockResolvedValue({ id: 11, name: "Lee Kim" });
  });

  it("shows a card per therapist with today's visits", async () => {
    renderWithTheme(<TherapistsManage />);

    const dana = await screen.findByRole("article", { name: "Dana Ortiz" });
    await waitFor(() => expect(dana).toHaveTextContent("2 visits today · 1 waiting"));
    expect(screen.getByRole("article", { name: "Sam Patel" })).toHaveTextContent("No visits today");
    expect(screen.getByText("2 therapists · 2 visits today")).toBeInTheDocument();
  });

  it("adds a therapist from the drawer", async () => {
    const user = userEvent.setup();
    renderWithTheme(<TherapistsManage />);
    await screen.findByRole("article", { name: "Dana Ortiz" });

    await user.click(screen.getByRole("button", { name: "Add therapist" }));
    const drawer = screen.getByRole("dialog", { name: "Add therapist" });
    await user.type(within(drawer).getByRole("textbox", { name: "Therapist name" }), "Lee Kim");
    await user.click(within(drawer).getByRole("button", { name: "Add therapist" }));

    await waitFor(() => expect(createTherapist).toHaveBeenCalledWith({ name: "Lee Kim" }));
  });

  it("warns that removing a therapist also deletes their appointments", async () => {
    const user = userEvent.setup();
    removeTherapist.mockResolvedValue(undefined);
    renderWithTheme(<TherapistsManage />);
    await screen.findByRole("article", { name: "Dana Ortiz" });

    await user.click(screen.getByRole("button", { name: "More actions for Dana Ortiz" }));
    await user.click(screen.getByRole("menuitem", { name: "Remove therapist" }));
    expect(screen.getByRole("dialog", { name: "Remove therapist" })).toHaveTextContent(
      "This also permanently deletes all of their appointments, including past visits.",
    );
    await user.click(screen.getByRole("button", { name: "Remove" }));

    await waitFor(() => expect(removeTherapist).toHaveBeenCalledWith(9));
  });
});
