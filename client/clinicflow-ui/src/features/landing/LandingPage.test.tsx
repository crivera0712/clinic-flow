import { MemoryRouter } from "react-router-dom";
import { screen, waitFor } from "@testing-library/react";
import userEvent from "@testing-library/user-event";
import { vi } from "vitest";
import { ApiError } from "../../api/client";
import { renderWithTheme } from "../../test/render";
import LandingPage from "./LandingPage";

// `calls` records which role was requested; `result` decides how the sign-in ends. The rejection
// comes from a plain function rather than the vi.fn, whose promise tracking reports a handled
// rejection as an unhandled test error.
const demo = vi.hoisted(() => ({
  calls: vi.fn(),
  result: (): Promise<void> => Promise.resolve(),
}));
vi.mock("../../auth/AuthContext", () => ({
  useAuth: () => ({
    loginAsDemo: (role: string) => {
      demo.calls(role);
      return demo.result();
    },
  }),
}));

function renderLanding() {
  renderWithTheme(
    <MemoryRouter>
      <LandingPage />
    </MemoryRouter>,
  );
}

describe("LandingPage", () => {
  beforeEach(() => {
    demo.calls.mockReset();
    demo.result = () => Promise.resolve();
  });

  it("leads with the pitch and the two one-click demo buttons", () => {
    renderLanding();

    expect(
      screen.getByRole("heading", {
        level: 1,
        name: "Run the front desk and the gym from one schedule.",
      }),
    ).toBeInTheDocument();
    expect(screen.getByRole("button", { name: "Try as Front Desk" })).toBeInTheDocument();
    expect(screen.getByRole("button", { name: "Try as Display" })).toBeInTheDocument();
    for (const name of [
      "From the front desk to the gym",
      "Built for a busy front desk",
      "Made for a clinic in Corte Madera",
      "How it's built",
    ]) {
      expect(screen.getByRole("region", { name })).toBeInTheDocument();
    }
  });

  it.each([
    ["Try as Front Desk", "ADMIN"],
    ["Try as Display", "DISPLAY"],
  ])("%s signs into the demo as %s", async (label, role) => {
    const user = userEvent.setup();
    renderLanding();

    await user.click(screen.getByRole("button", { name: label }));

    expect(demo.calls).toHaveBeenCalledWith(role);
    // Both buttons wait while signing in; the app then switches to the console or board.
    expect(screen.getByRole("button", { name: "Try as Front Desk" })).toBeDisabled();
  });

  it("explains when the demo can't be opened and lets the visitor retry", async () => {
    const user = userEvent.setup();
    demo.result = () => Promise.reject(new ApiError("Demo user not available", 404, null));
    renderLanding();

    await user.click(screen.getByRole("button", { name: "Try as Front Desk" }));

    expect(await screen.findByText("Demo user not available")).toBeInTheDocument();
    await waitFor(() =>
      expect(screen.getByRole("button", { name: "Try as Front Desk" })).toBeEnabled(),
    );
  });

  it("links to sign-in and the source code", () => {
    renderLanding();

    expect(screen.getAllByRole("link", { name: "Sign in" })[0]).toHaveAttribute("href", "/login");
    // Header icon and footer link.
    for (const link of screen.getAllByRole("link", { name: "Source on GitHub" })) {
      expect(link).toHaveAttribute("href", "https://github.com/crivera0712/clinic-flow");
    }
    expect(screen.getByRole("img", { name: /ClinicFlow architecture/ })).toBeInTheDocument();
  });
});
