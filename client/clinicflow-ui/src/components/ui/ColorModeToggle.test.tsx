import { screen } from "@testing-library/react";
import userEvent from "@testing-library/user-event";
import { renderWithTheme } from "../../test/render";
import { ColorModeToggle } from "./ColorModeToggle";

describe("ColorModeToggle", () => {
  afterEach(() => {
    document.documentElement.removeAttribute("data-dark");
    document.documentElement.removeAttribute("data-light");
  });

  it("starts in light mode and switches the whole document to dark", async () => {
    const user = userEvent.setup();
    renderWithTheme(<ColorModeToggle />);

    const toggle = screen.getByRole("switch", { name: "Dark mode" });
    expect(toggle).not.toBeChecked();
    expect(document.documentElement).toHaveAttribute("data-light");

    await user.click(toggle);

    expect(toggle).toBeChecked();
    expect(document.documentElement).toHaveAttribute("data-dark");
  });
});
