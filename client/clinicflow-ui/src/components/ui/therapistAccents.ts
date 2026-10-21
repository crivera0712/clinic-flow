import { useMemo } from "react";
import { useTheme } from "@mui/material/styles";

// Each therapist gets an accent color by position in the clinic's therapist list (sorted by id),
// so a therapist keeps the same color on every day and on every screen. Pass the full therapist
// list's ids, plus any ids seen only on appointments (e.g. a since-removed therapist).
export function buildTherapistAccentMap(
  therapistIds: Iterable<number>,
  palette: readonly string[],
): Map<number, string> {
  const ids = [...new Set(therapistIds)].sort((left, right) => left - right);

  return new Map(
    ids.map((therapistId, index) => [
      therapistId,
      // Past the palette, spread extra hues by the golden angle at a mid lightness that stays
      // readable on both light and dark surfaces.
      palette[index] ?? `hsl(${Math.round((index * 137.508) % 360)} 30% 50%)`,
    ]),
  );
}

// Accent map using the theme's `palette.therapist` CSS variables, so colors follow light/dark.
export function useTherapistAccents(therapistIds: Iterable<number>): Map<number, string> {
  const theme = useTheme();
  const key = [...new Set(therapistIds)].sort((left, right) => left - right).join(",");

  return useMemo(
    () =>
      buildTherapistAccentMap(key ? key.split(",").map(Number) : [], theme.vars.palette.therapist),
    [key, theme],
  );
}
