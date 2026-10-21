import { screen } from "@testing-library/react";
import userEvent from "@testing-library/user-event";
import { MemoryRouter } from "react-router-dom";
import { vi } from "vitest";
import LoginPage from "./LoginPage";
import { renderWithTheme } from "./test/render";

const login = vi.fn();

vi.mock("./auth/AuthContext", () => ({
  useAuth: () => ({ login }),
}));

describe("LoginPage", () => {
  it("presents the refreshed sign-in form and submits credentials", async () => {
    const user = userEvent.setup();
    renderWithTheme(
      <MemoryRouter>
        <LoginPage />
      </MemoryRouter>,
    );

    expect(screen.getByRole("heading", { name: "Sign in" })).toBeInTheDocument();
    expect(screen.getByRole("link", { name: "Try the demo" })).toHaveAttribute("href", "/");
    await user.type(screen.getByLabelText("Username"), "demo_admin");
    await user.type(screen.getByLabelText("Password"), "secret");
    await user.click(screen.getByRole("button", { name: "Sign in" }));

    expect(login).toHaveBeenCalledWith({ username: "demo_admin", password: "secret" });
  });
});
