// Every query key lives here so reads and invalidations always agree.
// Invalidating a prefix (e.g. `queryKeys.appointments.all`) refreshes every query under it.
export const queryKeys = {
  appointments: {
    all: ["appointments"] as const,
    byDate: (date: string) => ["appointments", "date", date] as const,
  },
  patients: {
    all: ["patients"] as const,
    list: ["patients", "list"] as const,
    search: (query: string) => ["patients", "search", query] as const,
  },
  therapists: {
    all: ["therapists"] as const,
  },
};
