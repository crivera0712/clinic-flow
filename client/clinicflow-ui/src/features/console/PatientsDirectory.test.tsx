import { screen, waitFor, within } from "@testing-library/react";
import userEvent from "@testing-library/user-event";
import { beforeEach, vi } from "vitest";
import { renderWithTheme } from "../../test/render";
import { PatientsDirectory } from "./PatientsDirectory";

const listPatients = vi.fn();
const createPatient = vi.fn();
const updatePatient = vi.fn();

vi.mock("../../services/patientService", () => ({
  listPatients: (...args: unknown[]) => listPatients(...args),
  createPatient: (...args: unknown[]) => createPatient(...args),
  updatePatient: (...args: unknown[]) => updatePatient(...args),
  searchPatients: vi.fn().mockResolvedValue([]),
}));

const patients = [
  { id: 1, firstName: "Maya", lastName: "Lopez" },
  { id: 2, firstName: "Omar", lastName: "Haddad" },
  { id: 3, firstName: "Grace", lastName: "Liu" },
];

describe("PatientsDirectory", () => {
  beforeEach(() => {
    vi.clearAllMocks();
    listPatients.mockResolvedValue({ rows: patients, rowCount: patients.length });
    createPatient.mockResolvedValue({ id: 4, firstName: "Iris", lastName: "Bell" });
    updatePatient.mockResolvedValue({ id: 1, firstName: "Mia", lastName: "Lopez" });
  });

  it("searches names case-insensitively and shows how many match", async () => {
    const user = userEvent.setup();
    renderWithTheme(<PatientsDirectory />);
    expect(await screen.findByText("3 patients")).toBeInTheDocument();

    await user.type(screen.getByRole("textbox", { name: "Search patients" }), "maya");

    expect(screen.getByText("1 of 3 patients")).toBeInTheDocument();
    expect(screen.getByRole("button", { name: "Edit Maya Lopez" })).toBeInTheDocument();
    expect(screen.queryByRole("button", { name: "Edit Omar Haddad" })).not.toBeInTheDocument();

    await user.click(screen.getByRole("button", { name: "Clear search" }));
    expect(screen.getByText("3 patients")).toBeInTheDocument();
  });

  it("adds a patient from the drawer", async () => {
    const user = userEvent.setup();
    renderWithTheme(<PatientsDirectory />);
    await screen.findByText("3 patients");

    await user.click(screen.getByRole("button", { name: "Add patient" }));
    const drawer = screen.getByRole("dialog", { name: "Add patient" });
    await user.type(within(drawer).getByRole("textbox", { name: "First name" }), "Iris");
    await user.type(within(drawer).getByRole("textbox", { name: "Last name" }), "Bell");
    await user.click(within(drawer).getByRole("button", { name: "Add patient" }));

    await waitFor(() =>
      expect(createPatient).toHaveBeenCalledWith({ firstName: "Iris", lastName: "Bell" }),
    );
    expect(await screen.findByText("Patient added.")).toBeInTheDocument();
  });

  it("edits a patient from the drawer, starting from their current name", async () => {
    const user = userEvent.setup();
    renderWithTheme(<PatientsDirectory />);
    await screen.findByText("3 patients");

    await user.click(screen.getByRole("button", { name: "Edit Maya Lopez" }));
    const drawer = screen.getByRole("dialog", { name: "Edit patient" });
    const firstName = within(drawer).getByRole("textbox", { name: "First name" });
    expect(firstName).toHaveValue("Maya");
    await user.clear(firstName);
    await user.type(firstName, "Mia");
    await user.click(within(drawer).getByRole("button", { name: "Save" }));

    await waitFor(() =>
      expect(updatePatient).toHaveBeenCalledWith(1, { firstName: "Mia", lastName: "Lopez" }),
    );
  });
});
