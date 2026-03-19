// // src/api/displayBoard.ts (or src/api/appointments.ts)
// import type { AppointmentDisplay } from "../types/appointment";
//
// export async function fetchAppointmentsByDate(
//     date: string
// ): Promise<AppointmentDisplay[]> {
//     const params = new URLSearchParams({ date });
//
//     const res = await fetch(`/api/appointments/date?${params}`);
//
//     if (!res.ok) {
//         const text = await res.text().catch(() => "");
//         throw new Error(`Failed to load appointments: ${res.status} ${text}`);
//     }
//
//     return res.json();
// }