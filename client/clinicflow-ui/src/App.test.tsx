import { MemoryRouter } from "react-router-dom";
import { screen } from "@testing-library/react";
import { vi } from "vitest";
import App from "./App";
import { renderWithTheme } from "./test/render";

vi.mock("./auth/AuthContext", () => ({
  useAuth: () => ({ status: "anonymous", currentUser: null, login: vi.fn(), loginAsDemo: vi.fn() }),
}));

function renderAt(path: string) {
  renderWithTheme(
    <MemoryRouter initialEntries={[path]}>
      <App />
    </MemoryRouter>,
  );
}

describe("App routing when signed out", () => {
  it("shows the landing page at /", () => {
    renderAt("/");
    expect(screen.getByRole("button", { name: "Try as Front Desk" })).toBeInTheDocument();
  });

  it("shows the sign-in form at /login", () => {
    renderAt("/login");
    expect(screen.getByRole("heading", { name: "Sign in" })).toBeInTheDocument();
  });

  it("sends any other path (e.g. after logging out of /patients) to the landing page", () => {
    renderAt("/patients");
    expect(screen.getByRole("button", { name: "Try as Display" })).toBeInTheDocument();
  });
});
