import { MemoryRouter } from "react-router-dom";
import { screen, within } from "@testing-library/react";
import userEvent from "@testing-library/user-event";
import { vi } from "vitest";
import ScheduleDisplayPage from "./ScheduleDisplayPage";
import { buildTherapistAccentMap } from "./components/ui/therapistAccents";
import { renderWithTheme } from "./test/render";
import { clinicTheme } from "./theme";
import { boardName } from "./types/patient";

const authMock = vi.hoisted(() => ({
  currentUser: { username: "demo_admin", roleName: "ADMIN" },
  logout: vi.fn(),
}));

vi.mock("./auth/AuthContext", () => ({
  useAuth: () => authMock,
}));

vi.mock("./services/therapistService", () => ({
  listTherapists: vi.fn().mockResolvedValue([
    { id: 1, name: "Dr. Smith" },
    { id: 2, name: "Dr. Lee" },
    { id: 3, name: "Dr. Idle" },
  ]),
}));

// vi.mock factories are hoisted above imports, so their data must be hoisted too.
const board = vi.hoisted(() => ({
  rows: [
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
      therapistId: 2,
      therapistName: "Dr. Lee",
    },
  ],
  list: vi.fn(),
}));

vi.mock("./services/appointmentService", () => ({
  listAppointmentsByDate: (...args: unknown[]) => board.list(...args),
}));

function renderBoard() {
  renderWithTheme(
    <MemoryRouter>
      <ScheduleDisplayPage />
    </MemoryRouter>,
  );
}

describe("ScheduleDisplayPage", () => {
  beforeEach(() => {
    authMock.currentUser = { username: "demo_admin", roleName: "ADMIN" };
    authMock.logout.mockReset();
    board.list.mockReset();
    board.list.mockResolvedValue(board.rows);
  });

  it("assigns one unique accent to each therapist regardless of database id gaps", () => {
    const accents = buildTherapistAccentMap([1, 9, 1], clinicTheme.vars.palette.therapist);

    expect(accents.get(1)).toBeDefined();
    expect(accents.get(9)).toBeDefined();
    expect(accents.get(1)).not.toBe(accents.get(9));
    expect(new Set(accents.values()).size).toBe(accents.size);
  });

  it("shortens patient names to first name and last initial for the public screen", () => {
    expect(boardName("Maya Lopez")).toBe("Maya L.");
    expect(boardName("Mary Jo Smith")).toBe("Mary S.");
    expect(boardName("Cher")).toBe("Cher");
    expect(boardName("  ana  ruiz ")).toBe("ana R.");
  });

  it("separates waiting patients from upcoming appointments, without full names", async () => {
    renderBoard();

    expect(screen.getByRole("heading", { name: "Currently waiting" })).toBeInTheDocument();
    expect(screen.getByRole("heading", { name: "Up next" })).toBeInTheDocument();
    expect(
      screen.getByRole("heading", { name: "Mill Valley Physical Therapy" }),
    ).toBeInTheDocument();
    expect(await screen.findByText("Mary R.")).toBeInTheDocument();
    expect(screen.getByText("Pat S.")).toBeInTheDocument();
    expect(screen.queryByText("Mary Roe")).not.toBeInTheDocument();
    expect(screen.queryByText("Pat Sims")).not.toBeInTheDocument();
    expect(screen.getByRole("button", { name: /Console/ })).toBeInTheDocument();
    expect(screen.queryByRole("button", { name: /Log out/ })).not.toBeInTheDocument();
  });

  it("lists only the therapists on today's board in the legend", async () => {
    renderBoard();
    await screen.findByText("Mary R.");

    const legend = screen.getByRole("list", { name: "Therapists" });
    expect(
      within(legend)
        .getAllByRole("listitem")
        .map((item) => item.textContent),
    ).toEqual(["Dr. Smith", "Dr. Lee"]);
  });

  it("shows when the board last updated", async () => {
    renderBoard();
    await screen.findByText("Mary R.");

    expect(screen.getByText("Updated just now")).toBeInTheDocument();
  });

  it("keeps the last schedule and turns the indicator amber when a refresh fails", async () => {
    vi.useFakeTimers({ shouldAdvanceTime: true });
    try {
      renderBoard();
      await screen.findByText("Mary R.");

      // The next 30s refresh fails, and nothing succeeds for over 2 minutes.
      board.list.mockRejectedValue(new Error("Network down"));
      await vi.advanceTimersByTimeAsync(150_000);

      expect(screen.getByText(/^Reconnecting… last updated 2 min ago$/)).toBeInTheDocument();
      expect(screen.getByText("Mary R.")).toBeInTheDocument();
      expect(screen.queryByRole("alert")).not.toBeInTheDocument();
    } finally {
      vi.useRealTimers();
    }
  });

  it("shows the logout button only for display users, next to the dark mode button", async () => {
    const user = userEvent.setup();
    authMock.currentUser = { username: "demo_display", roleName: "DISPLAY" };
    renderBoard();

    expect(screen.queryByRole("button", { name: /Console/ })).not.toBeInTheDocument();
    expect(screen.getByRole("button", { name: "Switch to dark mode" })).toBeInTheDocument();
    await user.click(screen.getByRole("button", { name: /Log out/ }));

    expect(authMock.logout).toHaveBeenCalledTimes(1);
  });
});
