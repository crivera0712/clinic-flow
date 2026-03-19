import { useEffect, useMemo, useState } from "react";
//import { fetchAppointmentsByDate } from "./api/displayBoard";

// ---- Types (match your backend JSON) ----
export type Status = "CHECKED_IN" | "IN_SESSION" | "FINISHED" | "SCHEDULED";

export interface AppointmentDisplay {
    aptId: number;
    scheduledAt: string; // e.g. "2026-02-23 10:00"
    caseId: number;

    firstName: string;
    lastName: string;

    therapistId: number;
    therapistName: string;
    therapistType: string;

    bodyRegionDisplayName: string;
    status: Status;
}

// ---- Helpers ----
function getTodayISODate(): string {
    const d = new Date();
    const yyyy = d.getFullYear();
    const mm = String(d.getMonth() + 1).padStart(2, "0");
    const dd = String(d.getDate()).padStart(2, "0");
    return `${yyyy}-${mm}-${dd}`;
}

// Backend sends "YYYY-MM-DD HH:mm" — convert to "YYYY-MM-DDTHH:mm" for safe parsing.
function parseBackendDateTime(dt: string): Date {
    // Example: "2026-02-23 10:00" -> "2026-02-23T10:00"
    const isoLike = dt.includes(" ") ? dt.replace(" ", "T") : dt;
    return new Date(isoLike);
}

function formatTime(dt: string): string {
    return new Intl.DateTimeFormat("en-US", {
        hour: "numeric",
        minute: "2-digit",
    }).format(parseBackendDateTime(dt));
}

// Simple accent mapping (edit to match your clinic colors)
const therapistAccentMap: Record<string, string> = {
    "Ada Wong": "bg-blue-500",
    "Leon Kennedy": "bg-green-600",
    "Jill Valentine": "bg-purple-600",
};

type CardItem = AppointmentDisplay & { accent: string };

function toCardItem(a: AppointmentDisplay): CardItem {
    return {
        ...a,
        accent: therapistAccentMap[a.therapistName] ?? "bg-gray-400",
    };
}

// ---- UI Components ----
function ScheduleCard({ item }: { item: CardItem }) {
    return (
        <div className="relative bg-gray-50 rounded-2xl p-8 border border-gray-200 flex flex-col justify-between overflow-hidden">
            {/* Accent Bar */}
            <div className={`absolute left-0 top-0 h-full w-3 ${item.accent}`} />

            {/* Appointment time (top-right chip) */}
            <div className="absolute right-6 top-6 rounded-xl bg-white/80 px-4 py-2 text-3xl font-semibold text-gray-700 border border-gray-200">
                {formatTime(item.scheduledAt)}
            </div>

            {/* Therapist name */}
            <div className="pl-4 text-4xl font-semibold text-gray-600">
                {item.therapistName}
            </div>

            {/* Patient name (vertically centered) */}
            <div className="pl-4 text-4xl font-bold text-gray-900 flex-1 flex items-center">
                {item.lastName}, {item.firstName}
            </div>

            {/* Region */}
            <div className="pl-4 text-4xl font-bold text-gray-600">
                {item.bodyRegionDisplayName}
            </div>
        </div>
    );
}

export default function ScheduleDisplayPage() {
    const [appointments, setAppointments] = useState<AppointmentDisplay[]>([]);
    const [error, setError] = useState<string | null>(null);

    // Live clock (top-right)
    const [now, setNow] = useState(() => new Date());
    useEffect(() => {
        const timer = setInterval(() => setNow(new Date()), 60000);
        return () => clearInterval(timer);
    }, []);

    const currentTime = useMemo(() => {
        return new Intl.DateTimeFormat("en-US", { hour: "numeric", minute: "2-digit" }).format(now);
    }, [now]);

    const currentDate = useMemo(() => {
        return new Intl.DateTimeFormat("en-US", {
            weekday: "long",
            month: "long",
            day: "numeric",
        }).format(now);
    }, [now]);

    // Load appointments (poll every 30s)
    useEffect(() => {
        let cancelled = false;

        async function load() {
            try {
                setError(null);
                const today = getTodayISODate();
                const data: AppointmentDisplay[] = [
                    {
                        aptId: 1,
                        scheduledAt: `${today} 09:00`,
                        caseId: 101,
                        firstName: "John",
                        lastName: "Doe",
                        therapistId: 1,
                        therapistName: "Ada Wong",
                        therapistType: "Physical Therapist",
                        bodyRegionDisplayName: "Lower Back",
                        status: "CHECKED_IN",
                    },
                    {
                        aptId: 2,
                        scheduledAt: `${today} 09:30`,
                        caseId: 102,
                        firstName: "Jane",
                        lastName: "Smith",
                        therapistId: 2,
                        therapistName: "Leon Kennedy",
                        therapistType: "Physical Therapist",
                        bodyRegionDisplayName: "Shoulder",
                        status: "SCHEDULED",
                    },
                    {
                        aptId: 3,
                        scheduledAt: `${today} 10:00`,
                        caseId: 103,
                        firstName: "Carlos",
                        lastName: "Rivera",
                        therapistId: 3,
                        therapistName: "Jill Valentine",
                        therapistType: "Physical Therapist",
                        bodyRegionDisplayName: "Knee",
                        status: "SCHEDULED",
                    },
                    {
                        aptId: 4,
                        scheduledAt: `${today} 10:15`,
                        caseId: 103,
                        firstName: "name2",
                        lastName: "Rivera",
                        therapistId: 3,
                        therapistName: "Jill Valentine",
                        therapistType: "Physical Therapist",
                        bodyRegionDisplayName: "Knee",
                        status: "SCHEDULED",
                    },
                ];
                if (!cancelled) setAppointments(data);
            } catch (e) {
                if (!cancelled) setError(e instanceof Error ? e.message : "Failed to load appointments");
            }
        }

        load();
        const interval = setInterval(load, 30000);
        return () => {
            cancelled = true;
            clearInterval(interval);
        };
    }, []);

    // Split into sections based on backend status
    const waiting = useMemo(
        () => appointments.filter((a) => a.status === "CHECKED_IN").map(toCardItem),
        [appointments]
    );

    const upNext = useMemo(
        () => appointments.filter((a) => a.status === "SCHEDULED").map(toCardItem),
        [appointments]
    );

    // Waiting: max 2 + placeholder to keep 2-col look when only 1
    const visibleWaiting = waiting.slice(0, 2);
    const waitingSlots = visibleWaiting.length === 1 ? [visibleWaiting[0], null] : visibleWaiting;

    const visibleUpNext = upNext.slice(0, 4);

    return (
        <div className="min-h-screen flex flex-col bg-gray-100 p-4 gap-10">
            {/* HEADER */}
            <div className="flex items-start justify-between">
                <h2 className="text-4xl font-bold">Mill Valley Physical Therapy</h2>

                <div className="text-right">
                    <div className="text-3xl font-semibold text-gray-800">{currentTime}</div>
                    <div className="text-2xl text-gray-600">{currentDate}</div>
                </div>
            </div>

            {error && (
                <div className="bg-red-50 border border-red-200 text-red-800 rounded-2xl p-4 text-2xl">
                    {error}
                </div>
            )}

            {/* WAITING SECTION */}
            <div>
                <h2 className="text-4xl font-bold mb-4">Waiting Room</h2>

                <div className="bg-white rounded-3xl shadow-lg p-6">
                    {waiting.length > 2 && (
                        <div className="text-lg text-gray-500 text-right">
                            +{waiting.length - 2} more waiting
                        </div>
                    )}

                    {visibleWaiting.length === 0 ? (
                        <div className="rounded-2xl border border-dashed border-gray-300 bg-gray-50 p-10 text-center">
                            <div className="text-3xl font-semibold text-gray-700">No one waiting</div>
                        </div>
                    ) : (
                        <div className="grid grid-cols-2 gap-4">
                            {waitingSlots.map((w, i) =>
                                w ? (
                                    <ScheduleCard key={i} item={w} />
                                ) : (
                                    <div
                                        key={i}
                                        className="rounded-2xl border border-dashed border-gray-200 bg-gray-50 p-8 flex items-center justify-center"
                                    >
                                        <div className="text-2xl font-semibold text-gray-400">Empty</div>
                                    </div>
                                )
                            )}
                        </div>
                    )}
                </div>
            </div>

            {/* UP NEXT SECTION */}
            <div>
                <h2 className="text-4xl font-bold mb-4">Up Next</h2>

                <div className="bg-white rounded-3xl shadow-lg p-8">
                    {visibleUpNext.length === 0 ? (
                        <div className="rounded-2xl border border-dashed border-gray-300 bg-gray-50 p-10 text-center">
                            <div className="text-3xl font-semibold text-gray-700">No upcoming appointments</div>
                            <div className="text-2xl text-gray-500 mt-2">End of day ✅</div>
                        </div>
                    ) : (
                        <div className="grid gap-4" style={{ gridTemplateColumns: "repeat(auto-fill, minmax(calc(50% - 0.5rem), 1fr))" }}>
                            {visibleUpNext.map((t, index) => (
                                <ScheduleCard key={index} item={t} />
                            ))}
                        </div>
                    )}
                </div>
            </div>
        </div>
    );
}