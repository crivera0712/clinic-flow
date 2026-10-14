import type { BoardRow } from "../../types/appointment";

// Give each therapist on the board a stable accent. `palette` is the theme's `palette.therapist`
// (pass `theme.vars.palette.therapist` so the colors follow light/dark mode).
export function buildTherapistAccentMap(
  appointments: BoardRow[],
  palette: readonly string[],
): Map<number, string> {
  const therapistIds = [
    ...new Set(appointments.map((appointment) => appointment.therapistId)),
  ].sort((left, right) => left - right);

  return new Map(
    therapistIds.map((therapistId, index) => [
      therapistId,
      // Past the palette, spread extra hues by the golden angle at a mid lightness that stays
      // readable on both light and dark surfaces.
      palette[index] ?? `hsl(${Math.round((index * 137.508) % 360)} 30% 50%)`,
    ]),
  );
}
