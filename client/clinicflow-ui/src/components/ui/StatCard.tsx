import ButtonBase from "@mui/material/ButtonBase";
import Typography from "@mui/material/Typography";
import type { AppointmentStatus } from "../../types/appointment";

type StatTone = Lowercase<AppointmentStatus>;

// A number with a label. With `onClick` it becomes a toggle button (e.g. a list filter);
// `selected` shows it as the active one.
export function StatCard({
  label,
  value,
  tone,
  selected = false,
  onClick,
}: {
  label: string;
  value: number;
  // Colors the label and selected state from the theme's `status.*` tokens.
  tone?: StatTone;
  selected?: boolean;
  onClick?: () => void;
}) {
  return (
    <ButtonBase
      component={onClick ? "button" : "div"}
      onClick={onClick}
      disabled={!onClick}
      aria-pressed={onClick ? selected : undefined}
      sx={(theme) => {
        const color = tone ? theme.vars.palette.status[tone] : theme.vars.palette.primary.main;
        return {
          display: "block",
          width: "100%",
          textAlign: "left",
          px: 2,
          py: 1.5,
          borderRadius: 1, // theme radius (14px), same as cards
          border: "1px solid",
          borderColor: selected ? theme.alpha(color, 0.5) : theme.vars.palette.divider,
          bgcolor: selected ? theme.alpha(color, 0.08) : theme.vars.palette.background.paper,
          boxShadow: theme.vars.palette.elevation.card,
          transition: "border-color 160ms ease, background-color 160ms ease",
          "&:hover": onClick ? { borderColor: theme.alpha(color, 0.4) } : undefined,
          "&.Mui-disabled": { pointerEvents: "none" },
          "&.Mui-focusVisible": { outline: `3px solid ${theme.alpha(color, 0.5)}` },
        };
      }}
    >
      <Typography
        variant="body2"
        fontWeight={650}
        sx={{ color: tone ? `status.${tone}` : "text.secondary" }}
      >
        {label}
      </Typography>
      <Typography sx={{ fontSize: "1.75rem", fontWeight: 720, lineHeight: 1.2 }}>
        {value}
      </Typography>
    </ButtonBase>
  );
}
