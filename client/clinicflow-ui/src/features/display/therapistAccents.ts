import type { BoardRow } from "../../types/appointment";

const THERAPIST_ACCENT_PALETTE = [
  "#2F6F62",
  "#3F6F82",
  "#75673D",
  "#675F7D",
  "#875C62",
  "#4E7462",
  "#596B73",
  "#79634F",
];

export function buildTherapistAccentMap(appointments: BoardRow[]): Map<number, string> {
  const therapistIds = [
    ...new Set(appointments.map((appointment) => appointment.therapistId)),
  ].sort((left, right) => left - right);

  return new Map(
    therapistIds.map((therapistId, index) => [
      therapistId,
      THERAPIST_ACCENT_PALETTE[index] ?? `hsl(${Math.round((index * 137.508) % 360)} 24% 38%)`,
    ]),
  );
}
