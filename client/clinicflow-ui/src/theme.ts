import { createTheme } from "@mui/material/styles";
import type {} from "@mui/x-data-grid/themeAugmentation";
import type { AppointmentStatus } from "./types/appointment";

// Custom tokens. Every value below exists once per color scheme and is exposed as a CSS variable,
// so components read them from `theme.vars.palette.*` and follow light/dark automatically.
declare module "@mui/material/styles" {
  interface CssThemeVariables {
    enabled: true;
  }
  interface TypeBackground {
    subtle: string;
  }
  interface Palette {
    border: { strong: string };
    elevation: { card: string; raised: string; overlay: string };
    status: Record<Lowercase<AppointmentStatus>, string>;
    therapist: string[];
  }
  interface PaletteOptions {
    border?: Palette["border"];
    elevation?: Palette["elevation"];
    status?: Palette["status"];
    therapist?: Palette["therapist"];
  }
}

// Light is the designed default: calm sage "clinical" tones seeded from the display board.
// Dark reuses the same hues, lightened for dark surfaces. Every text color passes WCAG AA (4.5:1)
// against background.default, background.paper and background.subtle in its scheme.
const light = {
  primary: { main: "#2F6F62", light: "#4E8C7E", dark: "#24574D", contrastText: "#FFFFFF" },
  secondary: { main: "#3F6F82", light: "#6A93A4", dark: "#2F5666", contrastText: "#FFFFFF" },
  info: { main: "#3F6F82", light: "#6A93A4", dark: "#2F5666", contrastText: "#FFFFFF" },
  success: { main: "#2E6F4E", light: "#5A9878", dark: "#225A3D", contrastText: "#FFFFFF" },
  warning: { main: "#8F5B0F", light: "#B98232", dark: "#73480A", contrastText: "#FFFFFF" },
  error: { main: "#B03A3A", light: "#D06A6A", dark: "#8E2C2C", contrastText: "#FFFFFF" },
  background: { default: "#EEF3F1", paper: "#FFFFFF", subtle: "#F7F9F8" },
  text: { primary: "#18363B", secondary: "#53696B", disabled: "#5D7072" },
  divider: "#D3DFDB",
  border: { strong: "#B9CBC5" },
  elevation: {
    card: "0 1px 2px rgba(24, 54, 59, 0.04), 0 4px 14px rgba(24, 54, 59, 0.06)",
    raised: "0 8px 24px rgba(24, 54, 59, 0.08)",
    overlay: "0 24px 64px rgba(24, 54, 59, 0.18)",
  },
  status: { scheduled: "#3F6F82", waiting: "#8F5B0F", done: "#2E6F4E" },
  DataGrid: { bg: "#FFFFFF", headerBg: "#F7F9F8", pinnedBg: "#FFFFFF" },
  therapist: [
    "#2F6F62",
    "#3F6F82",
    "#75673D",
    "#675F7D",
    "#875C62",
    "#4E7462",
    "#596B73",
    "#79634F",
  ],
};

const dark = {
  primary: { main: "#7CC2B0", light: "#A3D6C9", dark: "#5FA897", contrastText: "#0B1F1A" },
  secondary: { main: "#8DB9CB", light: "#B0D0DC", dark: "#6E9FB3", contrastText: "#0A1A20" },
  info: { main: "#8DB9CB", light: "#B0D0DC", dark: "#6E9FB3", contrastText: "#0A1A20" },
  success: { main: "#86C9A2", light: "#A9DABD", dark: "#68B286", contrastText: "#0B1F14" },
  warning: { main: "#E3B45F", light: "#EDCA8C", dark: "#CC9A40", contrastText: "#241703" },
  error: { main: "#F0918C", light: "#F5B3AF", dark: "#E2716B", contrastText: "#2A0C0B" },
  background: { default: "#0E1A1B", paper: "#152426", subtle: "#1B2D2F" },
  text: { primary: "#E4EEEC", secondary: "#A6B8B6", disabled: "#8A9D9B" },
  divider: "#29403F",
  border: { strong: "#3A5452" },
  elevation: {
    card: "0 1px 2px rgba(0, 0, 0, 0.3), 0 6px 18px rgba(0, 0, 0, 0.22)",
    raised: "0 18px 50px rgba(0, 0, 0, 0.28)",
    overlay: "0 30px 90px rgba(0, 0, 0, 0.55)",
  },
  status: { scheduled: "#8DB9CB", waiting: "#E3B45F", done: "#86C9A2" },
  DataGrid: { bg: "#152426", headerBg: "#1B2D2F", pinnedBg: "#152426" },
  therapist: [
    "#90D5C7",
    "#92C1D3",
    "#D1C294",
    "#ABA2C3",
    "#C79EA4",
    "#9DC8B4",
    "#A3B9C2",
    "#C9B19C",
  ],
};

export const clinicTheme = createTheme({
  // `data` puts `data-light` / `data-dark` on <html>; the inline script in index.html sets it
  // before first paint. `nativeColor` lets theme.alpha() tint any token, custom ones included.
  cssVariables: { colorSchemeSelector: "data", nativeColor: true },
  colorSchemes: { light: { palette: light }, dark: { palette: dark } },
  shape: { borderRadius: 14 },
  spacing: 8,
  typography: {
    fontFamily: 'Inter, "Segoe UI", system-ui, -apple-system, sans-serif',
    h1: { fontWeight: 750, letterSpacing: "-0.035em" },
    h2: { fontWeight: 750, letterSpacing: "-0.03em" },
    h3: { fontWeight: 730, letterSpacing: "-0.025em" },
    h4: { fontWeight: 720, letterSpacing: "-0.02em" },
    h5: { fontWeight: 700, letterSpacing: "-0.015em" },
    h6: { fontWeight: 680 },
    button: { fontWeight: 700, letterSpacing: "0.01em" },
    overline: { fontWeight: 750, letterSpacing: "0.18em" },
  },
  components: {
    MuiCssBaseline: {
      styleOverrides: (theme) => ({
        body: {
          // Times and counts line up in tables and on the board.
          fontVariantNumeric: "tabular-nums",
        },
        "::selection": { backgroundColor: theme.alpha(theme.vars.palette.primary.main, 0.25) },
        "*:focus-visible": {
          outline: `3px solid ${theme.alpha(theme.vars.palette.primary.main, 0.55)}`,
          outlineOffset: 2,
        },
      }),
    },
    MuiButton: {
      defaultProps: { disableElevation: true },
      styleOverrides: {
        root: { minHeight: 44, borderRadius: 11, paddingInline: 18, textTransform: "none" },
        containedPrimary: ({ theme }) => ({
          background: `linear-gradient(135deg, ${theme.vars.palette.primary.main} 0%, ${theme.vars.palette.primary.dark} 100%)`,
          boxShadow: `0 8px 22px ${theme.alpha(theme.vars.palette.primary.main, 0.22)}`,
          "&:hover": {
            boxShadow: `0 10px 26px ${theme.alpha(theme.vars.palette.primary.main, 0.32)}`,
          },
        }),
      },
    },
    MuiIconButton: { styleOverrides: { root: { minWidth: 44, minHeight: 44, borderRadius: 11 } } },
    MuiPaper: {
      defaultProps: { elevation: 0 },
      styleOverrides: {
        root: ({ theme }) => ({
          backgroundImage: "none",
          border: `1px solid ${theme.vars.palette.divider}`,
        }),
      },
    },
    MuiCard: {
      styleOverrides: {
        root: ({ theme }) => ({
          backgroundImage: "none",
          border: `1px solid ${theme.vars.palette.divider}`,
        }),
      },
    },
    MuiOutlinedInput: {
      styleOverrides: {
        root: ({ theme }) => ({
          minHeight: 48,
          backgroundColor: theme.vars.palette.background.paper,
          transition: "border-color 160ms ease, background-color 160ms ease, box-shadow 160ms ease",
          "& .MuiOutlinedInput-notchedOutline": { borderColor: theme.vars.palette.border.strong },
          "&:hover .MuiOutlinedInput-notchedOutline": {
            borderColor: theme.alpha(theme.vars.palette.primary.main, 0.6),
          },
          "&.Mui-focused": {
            boxShadow: `0 0 0 3px ${theme.alpha(theme.vars.palette.primary.main, 0.14)}`,
          },
          // Keep browser autofill on the input's own colors instead of the browser's yellow/blue.
          "& .MuiOutlinedInput-input:-webkit-autofill": {
            WebkitBoxShadow: `0 0 0 100px ${theme.vars.palette.background.paper} inset`,
            WebkitTextFillColor: theme.vars.palette.text.primary,
            caretColor: theme.vars.palette.text.primary,
            borderRadius: "inherit",
          },
        }),
      },
    },
    MuiInputLabel: {
      styleOverrides: { root: ({ theme }) => ({ color: theme.vars.palette.text.secondary }) },
    },
    // Select menus and Autocomplete listboxes.
    MuiPopover: {
      styleOverrides: {
        paper: ({ theme }) => ({ boxShadow: theme.vars.palette.elevation.raised }),
      },
    },
    MuiAutocomplete: {
      styleOverrides: {
        paper: ({ theme }) => ({ boxShadow: theme.vars.palette.elevation.raised }),
      },
    },
    MuiMenuItem: {
      styleOverrides: {
        root: ({ theme }) => ({
          "&:hover": { backgroundColor: theme.alpha(theme.vars.palette.primary.main, 0.08) },
          "&.Mui-selected": {
            backgroundColor: theme.alpha(theme.vars.palette.primary.main, 0.14),
            "&:hover": { backgroundColor: theme.alpha(theme.vars.palette.primary.main, 0.2) },
          },
        }),
      },
    },
    MuiDialog: {
      styleOverrides: {
        paper: ({ theme }) => ({ boxShadow: theme.vars.palette.elevation.overlay }),
      },
    },
    MuiDialogTitle: { styleOverrides: { root: { padding: "24px 24px 8px", fontWeight: 720 } } },
    MuiDialogActions: { styleOverrides: { root: { padding: "8px 24px 24px" } } },
    MuiDrawer: {
      styleOverrides: {
        paper: ({ theme }) => ({
          border: 0,
          borderRight: `1px solid ${theme.vars.palette.divider}`,
        }),
      },
    },
    MuiChip: { styleOverrides: { root: { borderRadius: 9, fontWeight: 700 } } },
    MuiAlert: { styleOverrides: { root: { borderRadius: 12, border: "1px solid currentColor" } } },
    MuiTooltip: { styleOverrides: { tooltip: { fontSize: "0.8rem", borderRadius: 8 } } },
    MuiTableHead: {
      styleOverrides: {
        root: ({ theme }) => ({ backgroundColor: theme.vars.palette.background.subtle }),
      },
    },
    MuiTableCell: {
      styleOverrides: {
        root: ({ theme }) => ({ borderColor: theme.vars.palette.divider }),
        head: ({ theme }) => ({
          color: theme.vars.palette.text.secondary,
          fontSize: "0.75rem",
          textTransform: "uppercase",
          letterSpacing: ".08em",
          fontWeight: 750,
        }),
      },
    },
    MuiDataGrid: {
      styleOverrides: {
        root: ({ theme }) => ({
          border: 0,
          "--DataGrid-rowBorderColor": theme.vars.palette.divider,
          "& .MuiDataGrid-columnHeaderTitle": { fontWeight: 700, letterSpacing: "0.02em" },
          "& .MuiDataGrid-row:hover": {
            backgroundColor: theme.alpha(theme.vars.palette.primary.main, 0.05),
          },
          "& .MuiDataGrid-cell:focus-visible, & .MuiDataGrid-columnHeader:focus-visible": {
            outline: `2px solid ${theme.vars.palette.primary.main}`,
            outlineOffset: -2,
          },
          "& .MuiDataGrid-footerContainer": {
            backgroundColor: theme.vars.palette.background.subtle,
          },
        }),
      },
    },
  },
});
