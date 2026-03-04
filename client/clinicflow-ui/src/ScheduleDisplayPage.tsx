import { useEffect, useState } from "react";
import { fetchAppointmentsByDate } from "./api/displayBoard";

function ScheduleCard({ item }) {
    return (
        <div className="relative bg-gray-50 rounded-2xl p-8 border border-gray-200 flex flex-col justify-between overflow-hidden">
            {/* Accent Bar */}
            <div className={`absolute left-0 top-0 h-full w-3 ${item.accent}`} />

            {/* Appointment time (top-right chip) */}
            <div className="absolute right-6 top-6 rounded-xl bg-white/80 px-4 py-2 text-3xl font-semibold text-gray-700 border border-gray-200">
                {item.time}
            </div>

            {/* Therapist name */}
            <div className="pl-4 text-4xl font-semibold text-gray-600">{item.name}</div>

            {/* Patient name (vertically centered like your Up Next) */}
            <div className="pl-4 text-4xl font-bold text-gray-900 flex-1 flex items-center">
                {item.patientLastName}, {item.patientFirstName}
            </div>

            {/* Region */}
            <div className="pl-4 text-4xl font-bold text-gray-600">{item.region}</div>
        </div>
    );
}

export default function ScheduleDisplayPage() {

    const [appointments, setAppointments] = useState([]);

    const therapists = [
        {
            name: "Leon Kennedy",
            patientFirstName: "A",
            patientLastName: "Wesker",
            region: "Knee",
            time: "9:00 AM",
            accent: "bg-blue-500",
        },
        {
            name: "Jill Valentine",
            patientFirstName: "C",
            patientLastName: "Olivera",
            region: "Low back",
            time: "9:15 AM",
            accent: "bg-green-600",
        },
        {
            name: "Leon Kennedy",
            patientFirstName: "A",
            patientLastName: "Wong",
            region: "Hip",
            time: "9:30 AM",
            accent: "bg-blue-500",
        },
        {
            name: "Jill Valentine",
            patientFirstName: "C",
            patientLastName: "Olivera",
            region: "Low back",
            time: "9:45 AM",
            accent: "bg-green-600",
        },
    ];

    const waiting = [
        {
            name: "Leon Kennedy",
            patientFirstName: "J",
            patientLastName: "Morrison",
            region: "Knee",
            time: "8:30 AM",
            accent: "bg-blue-500",
        },
        {
            name: "Jill Valentine",
            patientFirstName: "L",
            patientLastName: "Walker",
            region: "Low back",
            time: "8:45 AM",
            accent: "bg-green-600",
        },

    ];

    // Live clock
    const [now, setNow] = useState(() => new Date());

    useEffect(() => {
        const timer = setInterval(() => setNow(new Date()), 60000); // update every minute
        return () => clearInterval(timer);
    }, []);

    const timeFormatter = new Intl.DateTimeFormat("en-US", {
        hour: "numeric",
        minute: "2-digit",
    });

    const dateFormatter = new Intl.DateTimeFormat("en-US", {
        weekday: "long",
        month: "long",
        day: "numeric",
    });

    const currentTime = timeFormatter.format(now);
    const currentDate = dateFormatter.format(now);

    const visibleWaiting = waiting.slice(0, 2);
    const waitingSlots =
        visibleWaiting.length === 1 ? [visibleWaiting[0], null] : visibleWaiting;

    const visibleUpNext = therapists.slice(0, 4);

// If odd (1 or 3), add one null placeholder to keep the 2-col grid balanced
    const upNextSlots =
        visibleUpNext.length > 0 && visibleUpNext.length % 2 === 1
            ? [...visibleUpNext, null]
            : visibleUpNext;

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

            <button
                className="px-4 py-2 rounded-lg bg-black text-white"
                onClick={async () => {
                    try {
                        const data = await fetchAppointmentsByDate("2026-02-23");
                        console.log("APPOINTMENTS:", data);
                    } catch (e) {
                        console.error(e);
                    }
                }}
            >
                Test API
            </button>

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
                            <div className="text-3xl font-semibold text-gray-700">
                                No one waiting
                            </div>
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
                            <div className="text-3xl font-semibold text-gray-700">
                                No upcoming appointments
                            </div>
                            <div className="text-2xl text-gray-500 mt-2">End of day ✅</div>
                        </div>
                    ) : (
                        <div className="grid grid-cols-2 gap-4">
                            {upNextSlots.map((t, index) =>
                                t ? (
                                    <ScheduleCard key={index} item={t} />
                                ) : (
                                    <div
                                        key={index}
                                        className="rounded-2xl border border-dashed border-gray-200 bg-gray-50 p-8 flex items-center justify-center"
                                    >
                                        <div className="text-2xl font-semibold text-gray-400">
                                            {/* optional: leave blank instead */}
                                            Empty
                                        </div>
                                    </div>
                                )
                            )}
                        </div>
                    )}
                </div>
            </div>
        </div>
    );
}