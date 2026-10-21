import DarkModeRoundedIcon from "@mui/icons-material/DarkModeRounded";
import LightModeRoundedIcon from "@mui/icons-material/LightModeRounded";
import IconButton from "@mui/material/IconButton";
import Stack from "@mui/material/Stack";
import Switch from "@mui/material/Switch";
import Tooltip from "@mui/material/Tooltip";
import Typography from "@mui/material/Typography";
import { useColorScheme } from "@mui/material/styles";
import { clinicTheme } from "../../theme";

// MUI persists the mode in localStorage (`mui-mode`); the inline script in index.html re-applies
// it (and the browser-bar theme-color) on the next load. `isDark` is null until the mode is known.
function useDarkMode(): { isDark: boolean | null; setDark: (dark: boolean) => void } {
  const { mode, systemMode, setMode } = useColorScheme();
  const isDark = mode ? (mode === "system" ? systemMode : mode) === "dark" : null;

  function setDark(dark: boolean) {
    const next = dark ? "dark" : "light";
    setMode(next);
    document
      .querySelector('meta[name="theme-color"]')
      ?.setAttribute("content", clinicTheme.colorSchemes[next]!.palette.background.default);
  }

  return { isDark, setDark };
}

// "Dark mode" switch row for the console sidebar.
export function ColorModeToggle() {
  const { isDark, setDark } = useDarkMode();
  if (isDark === null) return null;

  return (
    // Icon column and text size match the sidebar nav items above it.
    <Stack
      component="label"
      direction="row"
      alignItems="center"
      sx={{ minHeight: 44, pl: 1.5, borderRadius: 2, cursor: "pointer" }}
    >
      <DarkModeRoundedIcon sx={{ color: "text.disabled", mr: 1.5 }} />
      <Typography
        fontWeight={600}
        fontSize="0.95rem"
        color="text.secondary"
        noWrap
        sx={{ flexGrow: 1, minWidth: 0 }}
      >
        Dark mode
      </Typography>
      <Switch checked={isDark} onChange={(event) => setDark(event.target.checked)} />
    </Stack>
  );
}

// Compact sun/moon button, for screens without the console sidebar (the display board).
export function ColorModeIconButton() {
  const { isDark, setDark } = useDarkMode();
  if (isDark === null) return null;
  const label = isDark ? "Switch to light mode" : "Switch to dark mode";

  return (
    <Tooltip title={label}>
      <IconButton
        aria-label={label}
        onClick={() => setDark(!isDark)}
        sx={{ color: "text.secondary" }}
      >
        {isDark ? <LightModeRoundedIcon /> : <DarkModeRoundedIcon />}
      </IconButton>
    </Tooltip>
  );
}
