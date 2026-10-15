import { MemoryRouter, Route, Routes } from "react-router-dom";
import { screen } from "@testing-library/react";
import userEvent from "@testing-library/user-event";
import { vi } from "vitest";
import { renderWithTheme } from "../../test/render";
import { ConsoleLayout } from "./ConsoleLayout";

const authMock = vi.hoisted(() => ({
  currentUser: { username: "demo_admin", roleName: "ADMIN", isDemo: false },
  logout: vi.fn(),
}));

vi.mock("../../auth/AuthContext", () => ({ useAuth: () => authMock }));

function renderLayout() {
  renderWithTheme(
    <MemoryRouter initialEntries={["/"]}>
      <Routes>
        <Route path="/" element={<ConsoleLayout />}>
          <Route index element={<div>Schedule content</div>} />
        </Route>
      </Routes>
    </MemoryRouter>,
  );
}

describe("ConsoleLayout", () => {
  beforeEach(() => {
    authMock.currentUser = { username: "demo_admin", roleName: "ADMIN", isDemo: false };
  });

  it("exposes the primary workspace navigation with an accessible mobile control", async () => {
    const user = userEvent.setup();
    renderLayout();

    expect(screen.getByRole("navigation", { name: "Primary navigation" })).toBeInTheDocument();
    // The display is reached from the sidebar only; the top bar has no duplicate button.
    expect(screen.queryByRole("link", { name: "Open display" })).not.toBeInTheDocument();
    const menuButton = screen.getByRole("button", { name: "Open navigation" });
    await user.click(menuButton);
    expect(screen.getByRole("link", { name: /Schedule/ })).toHaveAttribute("href", "/");
    expect(screen.getByRole("link", { name: /Schedule/ })).toHaveAttribute("aria-current", "page");
    expect(screen.getByRole("link", { name: /Patients/ })).toHaveAttribute("href", "/patients");
    expect(screen.getByRole("link", { name: /Therapists/ })).toHaveAttribute("href", "/therapists");
    expect(screen.getByRole("link", { name: "Clinic Display" })).toHaveAttribute("href", "/board");
    expect(screen.getByRole("button", { name: "Log out" })).toBeInTheDocument();
    expect(screen.getByText("Admin")).toBeInTheDocument();
  });

  it("shows today's date in the top bar", () => {
    renderLayout();

    const today = new Intl.DateTimeFormat("en-US", {
      weekday: "short",
      month: "short",
      day: "numeric",
    }).format(new Date());
    expect(screen.getByText(today)).toBeInTheDocument();
  });

  it("marks the demo clinic only for demo accounts", () => {
    renderLayout();
    expect(screen.queryByText("Demo clinic")).not.toBeInTheDocument();
  });

  it("shows a Demo clinic badge for demo accounts", () => {
    authMock.currentUser = { ...authMock.currentUser, isDemo: true };
    renderLayout();
    expect(screen.getByText("Demo clinic")).toBeInTheDocument();
  });
});
