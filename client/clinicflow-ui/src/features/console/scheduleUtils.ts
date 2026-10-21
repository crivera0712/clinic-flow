function toDateString(d: Date): string {
  return `${d.getFullYear()}-${String(d.getMonth() + 1).padStart(2, "0")}-${String(d.getDate()).padStart(2, "0")}`;
}

export function getTodayDateString(): string {
  return toDateString(new Date());
}

// "2026-10-14" + 1 -> "2026-10-15", in local time so month and DST boundaries are handled.
export function shiftDate(date: string, days: number): string {
  const d = new Date(`${date}T00:00`);
  d.setDate(d.getDate() + days);
  return toDateString(d);
}

// "2026-10-14" -> "Wednesday, October 14"
export function formatLongDate(date: string): string {
  return new Intl.DateTimeFormat("en-US", {
    weekday: "long",
    month: "long",
    day: "numeric",
  }).format(new Date(`${date}T00:00`));
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
