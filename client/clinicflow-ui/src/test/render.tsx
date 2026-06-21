import type { ReactElement } from "react";
import { render } from "@testing-library/react";
import { ThemeProvider } from "@mui/material/styles";
import { clinicTheme } from "../theme";

export function renderWithTheme(ui: ReactElement) {
  return render(<ThemeProvider theme={clinicTheme}>{ui}</ThemeProvider>);
}
