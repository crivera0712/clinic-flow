import type { AppointmentStatus } from "../../types/appointment";

export function getTodayDateString(): string {
  const now = new Date();
  return `${now.getFullYear()}-${String(now.getMonth() + 1).padStart(2, "0")}-${String(now.getDate()).padStart(2, "0")}`;
}

// Parse "YYYY-MM-DDTHH:mm" (also tolerates a space separator) into a Date.
export function parseScheduledAt(value: string): Date {
  return new Date(value.includes("T") ? value : value.replace(" ", "T"));
}

export function formatTime(value: string): string {
  const parsed = parseScheduledAt(value);
  if (Number.isNaN(parsed.getTime())) return value;
  return new Intl.DateTimeFormat("en-US", { hour: "numeric", minute: "2-digit" }).format(parsed);
}

// Split a typed full name into first/last for inline patient creation.
export function splitName(input: string): { firstName: string; lastName: string } {
  const parts = input.trim().split(/\s+/);
  return { firstName: parts[0] ?? "", lastName: parts.slice(1).join(" ") };
}

export const statusChipColor: Record<AppointmentStatus, "default" | "info" | "success"> = {
  SCHEDULED: "default",
  WAITING: "info",
  DONE: "success",
};
