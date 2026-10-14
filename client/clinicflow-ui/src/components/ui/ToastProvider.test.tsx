import { act, fireEvent, render, screen } from "@testing-library/react";
import { vi } from "vitest";
import { ToastProvider } from "./ToastProvider";
import { useToast } from "./useToast";

function Triggers() {
  const toast = useToast();
  return (
    <>
      <button onClick={() => toast.success("First")}>first</button>
      <button onClick={() => toast.success("Second")}>second</button>
      <button onClick={() => toast.error("Network down")}>fail</button>
    </>
  );
}

function renderToasts() {
  render(
    <ToastProvider>
      <Triggers />
    </ToastProvider>,
  );
}

const click = (name: string) => fireEvent.click(screen.getByRole("button", { name }));

describe("ToastProvider", () => {
  afterEach(() => vi.useRealTimers());

  it("shows success and error toasts with matching severity", () => {
    renderToasts();

    click("first");
    expect(screen.getByRole("alert")).toHaveTextContent("First");
    expect(screen.getByRole("alert")).toHaveClass("MuiAlert-filledSuccess");

    click("fail");
    expect(screen.getByRole("alert")).toHaveTextContent("Network down");
    expect(screen.getByRole("alert")).toHaveClass("MuiAlert-filledError");
  });

  it("replaces the current toast and restarts its timer", () => {
    vi.useFakeTimers();
    renderToasts();

    click("first");
    act(() => vi.advanceTimersByTime(3000));
    click("second");
    expect(screen.getAllByRole("alert")).toHaveLength(1);
    expect(screen.getByRole("alert")).toHaveTextContent("Second");

    // 3s into the second toast it is still showing, though the first would have hidden by now.
    act(() => vi.advanceTimersByTime(3000));
    expect(screen.getByRole("alert")).toHaveTextContent("Second");
  });

  it("keeps errors on screen longer than successes", () => {
    vi.useFakeTimers();
    renderToasts();

    click("fail");
    act(() => vi.advanceTimersByTime(5000));
    expect(screen.getByRole("alert")).toHaveTextContent("Network down");
  });

  it("throws a clear error outside the provider", () => {
    vi.spyOn(console, "error").mockImplementation(() => undefined);
    expect(() => render(<Triggers />)).toThrow("useToast must be used within a ToastProvider");
  });
});
