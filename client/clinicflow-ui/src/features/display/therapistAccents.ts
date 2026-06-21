import type { BoardRow } from "../../types/appointment";

const THERAPIST_ACCENT_PALETTE = [
  "#38bdf8", "#2dd4bf", "#fbbf24", "#fb923c", "#a78bfa", "#fb7185", "#34d399", "#facc15",
];

export function buildTherapistAccentMap(appointments: BoardRow[]): Map<number, string> {
  const therapistIds = [...new Set(appointments.map((appointment) => appointment.therapistId))]
    .sort((left, right) => left - right);

  return new Map(
    therapistIds.map((therapistId, index) => [
      therapistId,
      THERAPIST_ACCENT_PALETTE[index]
        ?? `hsl(${Math.round((index * 137.508) % 360)} 72% 62%)`,
    ]),
  );
}
