import DarkModeRoundedIcon from "@mui/icons-material/DarkModeRounded";
import Stack from "@mui/material/Stack";
import Switch from "@mui/material/Switch";
import Typography from "@mui/material/Typography";
import { useColorScheme } from "@mui/material/styles";
import { clinicTheme } from "../../theme";

// "Dark mode" switch row for the console sidebar. MUI persists the choice in localStorage
// (`mui-mode`); the inline script in index.html re-applies it (and the browser-bar theme-color)
// on the next load.
export function ColorModeToggle() {
  const { mode, systemMode, setMode } = useColorScheme();
  if (!mode) return null;

  const isDark = (mode === "system" ? systemMode : mode) === "dark";

  function handleChange(checked: boolean) {
    const next = checked ? "dark" : "light";
    setMode(next);
    document
      .querySelector('meta[name="theme-color"]')
      ?.setAttribute("content", clinicTheme.colorSchemes[next]!.palette.background.default);
  }

  return (
    <Stack
      component="label"
      direction="row"
      alignItems="center"
      spacing={1.5}
      sx={{ minHeight: 50, pl: 1.5, pr: 0.5, borderRadius: 2.5, cursor: "pointer" }}
    >
      <DarkModeRoundedIcon sx={{ color: "text.disabled" }} />
      <Typography fontWeight={600} color="text.secondary" sx={{ flexGrow: 1, pl: 0.5 }}>
        Dark mode
      </Typography>
      <Switch checked={isDark} onChange={(event) => handleChange(event.target.checked)} />
    </Stack>
  );
}
