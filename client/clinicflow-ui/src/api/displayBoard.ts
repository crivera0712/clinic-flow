import type { DisplayBoardData } from "../types/appointment"

export async function fetchAppointmentsByDate(date: string) {
    const params = new URLSearchParams({ date });

    const res = await fetch(`/appointments/date?date=${params}`)
    if (!res.ok) {
        throw new Error(`Failed to load appointments: ${res.status}`);
    }
    return res.json();
}