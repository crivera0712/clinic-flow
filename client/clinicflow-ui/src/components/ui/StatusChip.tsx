import Chip from "@mui/material/Chip";
import { appointmentStatusLabels, type AppointmentStatus } from "../../types/appointment";

// Check-in status as a soft tinted chip, colored from the theme's `status.*` tokens.
export function StatusChip({ status }: { status: AppointmentStatus }) {
  const key = status.toLowerCase() as Lowercase<AppointmentStatus>;

  return (
    <Chip
      size="small"
      label={appointmentStatusLabels[status]}
      sx={(theme) => {
        const color = theme.vars.palette.status[key];
        return {
          color,
          bgcolor: theme.alpha(color, 0.12),
          border: `1px solid ${theme.alpha(color, 0.3)}`,
        };
      }}
    />
  );
}
