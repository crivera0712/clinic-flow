import { screen } from "@testing-library/react";
import userEvent from "@testing-library/user-event";
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
    renderWithTheme(<LoginPage />);

    expect(screen.getByText("A calmer way to run the day.")).toBeInTheDocument();
    await user.type(screen.getByLabelText("Username"), "demo_admin");
    await user.type(screen.getByLabelText("Password"), "secret");
    await user.click(screen.getByRole("button", { name: "Login" }));

    expect(login).toHaveBeenCalledWith({ username: "demo_admin", password: "secret" });
  });
});
