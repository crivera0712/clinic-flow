import { screen } from "@testing-library/react";
import userEvent from "@testing-library/user-event";
import { vi } from "vitest";
import { renderWithTheme } from "../../test/render";
import { ConfirmDialog } from "./ConfirmDialog";

function renderDialog(props: Partial<Parameters<typeof ConfirmDialog>[0]> = {}) {
  const onClose = vi.fn();
  const onConfirm = vi.fn();
  renderWithTheme(
    <ConfirmDialog
      open
      title="Remove therapist"
      description="Remove Dr. Lee?"
      confirmLabel="Remove"
      tone="danger"
      onClose={onClose}
      onConfirm={onConfirm}
      {...props}
    />,
  );
  return { onClose, onConfirm };
}

describe("ConfirmDialog", () => {
  it("confirms and cancels through labelled buttons", async () => {
    const user = userEvent.setup();
    const { onClose, onConfirm } = renderDialog({ cancelLabel: "Keep" });

    expect(screen.getByRole("dialog", { name: "Remove therapist" })).toHaveTextContent(
      "Remove Dr. Lee?",
    );
    await user.click(screen.getByRole("button", { name: "Remove" }));
    expect(onConfirm).toHaveBeenCalledTimes(1);
    await user.click(screen.getByRole("button", { name: "Keep" }));
    expect(onClose).toHaveBeenCalledTimes(1);
  });

  it("locks both buttons while the action is running", () => {
    renderDialog({ loading: true });

    expect(screen.getByRole("button", { name: "Remove" })).toBeDisabled();
    expect(screen.getByRole("button", { name: "Cancel" })).toBeDisabled();
  });
});
