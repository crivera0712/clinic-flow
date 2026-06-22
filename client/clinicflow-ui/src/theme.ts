import { alpha, createTheme } from "@mui/material/styles";

export const colors = {
  canvas: "#050b18",
  canvasRaised: "#081222",
  surface: "#0d192b",
  surfaceRaised: "#132238",
  surfaceHover: "#192b43",
  border: "#243750",
  borderSoft: "rgba(148, 163, 184, 0.14)",
  primary: "#38bdf8",
  primaryStrong: "#0ea5e9",
  success: "#2dd4bf",
  warning: "#fbbf24",
  error: "#fb7185",
  text: "#f1f5f9",
  textSecondary: "#a8b7ca",
  textMuted: "#718299",
} as const;

export const clinicTheme = createTheme({
  palette: {
    mode: "dark",
    primary: { main: colors.primary, dark: colors.primaryStrong, contrastText: "#03111b" },
    secondary: { main: "#a78bfa" },
    success: { main: colors.success, contrastText: "#031713" },
    warning: { main: colors.warning, contrastText: "#1d1402" },
    error: { main: colors.error, contrastText: "#20070c" },
    background: { default: colors.canvas, paper: colors.surface },
    text: { primary: colors.text, secondary: colors.textSecondary, disabled: colors.textMuted },
    divider: colors.borderSoft,
  },
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
      styleOverrides: {
        body: {
          backgroundImage:
            "radial-gradient(circle at 15% -10%, rgba(56, 189, 248, 0.08), transparent 32%), linear-gradient(180deg, #050b18 0%, #07101e 100%)",
        },
        "::selection": { backgroundColor: alpha(colors.primary, 0.3) },
        "*:focus-visible": { outline: `3px solid ${alpha(colors.primary, 0.55)}`, outlineOffset: 2 },
      },
    },
    MuiButton: {
      defaultProps: { disableElevation: true },
      styleOverrides: {
        root: { minHeight: 44, borderRadius: 11, paddingInline: 18, textTransform: "none" },
        containedPrimary: {
          background: `linear-gradient(135deg, ${colors.primary} 0%, ${colors.primaryStrong} 100%)`,
          boxShadow: `0 10px 28px ${alpha(colors.primaryStrong, 0.22)}`,
          "&:hover": { boxShadow: `0 12px 32px ${alpha(colors.primaryStrong, 0.32)}` },
        },
      },
    },
    MuiIconButton: { styleOverrides: { root: { minWidth: 44, minHeight: 44, borderRadius: 11 } } },
    MuiPaper: {
      defaultProps: { elevation: 0 },
      styleOverrides: {
        root: { backgroundImage: "none", border: `1px solid ${colors.borderSoft}` },
      },
    },
    MuiCard: { styleOverrides: { root: { backgroundImage: "none", border: `1px solid ${colors.borderSoft}` } } },
    MuiOutlinedInput: {
      styleOverrides: {
        root: {
          minHeight: 48,
          backgroundColor: alpha(colors.canvas, 0.55),
          transition: "border-color 160ms ease, background-color 160ms ease, box-shadow 160ms ease",
          "& .MuiOutlinedInput-notchedOutline": { borderColor: colors.border },
          "&:hover .MuiOutlinedInput-notchedOutline": { borderColor: alpha(colors.primary, 0.6) },
          "&.Mui-focused": { boxShadow: `0 0 0 3px ${alpha(colors.primary, 0.12)}` },
        },
      },
    },
    MuiInputLabel: { styleOverrides: { root: { color: colors.textSecondary } } },
    MuiDialog: {
      styleOverrides: {
        paper: { backgroundColor: colors.surfaceRaised, border: `1px solid ${colors.border}`, boxShadow: "0 30px 90px rgba(0,0,0,.55)" },
      },
    },
    MuiDialogTitle: { styleOverrides: { root: { padding: "24px 24px 8px", fontWeight: 720 } } },
    MuiDialogActions: { styleOverrides: { root: { padding: "8px 24px 24px" } } },
    MuiChip: { styleOverrides: { root: { borderRadius: 9, fontWeight: 700 } } },
    MuiAlert: { styleOverrides: { root: { borderRadius: 12, border: "1px solid currentColor" } } },
    MuiTooltip: { styleOverrides: { tooltip: { fontSize: "0.8rem", borderRadius: 8 } } },
  },
});
