import { MemoryRouter, Route, Routes } from "react-router-dom";
import { screen } from "@testing-library/react";
import userEvent from "@testing-library/user-event";
import { vi } from "vitest";
import { renderWithTheme } from "../../test/render";
import { ConsoleLayout } from "./ConsoleLayout";

vi.mock("../../auth/AuthContext", () => ({
  useAuth: () => ({ currentUser: { username: "demo_admin", roleName: "ADMIN" }, logout: vi.fn() }),
}));

describe("ConsoleLayout", () => {
  it("exposes the primary workspace navigation with an accessible mobile control", async () => {
    const user = userEvent.setup();
    renderWithTheme(
      <MemoryRouter initialEntries={["/"]}>
        <Routes><Route path="/" element={<ConsoleLayout />}><Route index element={<div>Schedule content</div>} /></Route></Routes>
      </MemoryRouter>,
    );

    expect(screen.getByRole("navigation", { name: "Primary navigation" })).toBeInTheDocument();
    expect(screen.getByRole("link", { name: "Open display" })).toHaveAttribute("href", "/board");
    const menuButton = screen.getByRole("button", { name: "Open navigation" });
    expect(menuButton).toBeInTheDocument();
    await user.click(menuButton);
    expect(screen.getByRole("link", { name: /Schedule/ })).toHaveAttribute("href", "/");
    expect(screen.getByRole("link", { name: /Patients/ })).toHaveAttribute("href", "/patients");
    expect(screen.getByRole("link", { name: /Therapists/ })).toHaveAttribute("href", "/therapists");
    expect(screen.getByRole("link", { name: "Clinic Display" })).toHaveAttribute("href", "/board");
    expect(screen.getByRole("button", { name: "Log out" })).toBeInTheDocument();
  });
});
