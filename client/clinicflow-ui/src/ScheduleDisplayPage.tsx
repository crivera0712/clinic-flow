export default function ScheduleDisplayPage() {
    const therapists = [
        {
            name: "Leon Kennedy",
            patientFirstName: "A",
            patientLastName: "Wesker",
            region: "Knee",
            accent: "bg-blue-500",
        },
        {
            name: "Jill Valentine",
            patientFirstName: "C",
            patientLastName: "Olivera",
            region: "Low back",
            accent: "bg-green-600",
        },
        {
            name: "Chris Redfield",
            patientFirstName: "A",
            patientLastName: "Wong",
            region: "Hip",
            accent: "bg-slate-600",
        },
    ];

    const waiting = [
        {
            patientFirstName: "J",
            patientLastName: "Morrison",
            region: "Knee",
        },
        {
            patientFirstName: "L",
            patientLastName: "Walker",
            region: "Low back",
        },
    ];

    return (
        <div className="min-h-screen flex flex-col bg-gray-100 p-4 gap-10">
            {/* HEADING */}
            <div>
                <h2 className="text-4xl font-bold">Mill Valley Physical Therapy</h2>
            </div>

            {/* WAITING SECTION */}
            <div>
                <h2 className="text-4xl font-bold mb-4">Waiting Room</h2>

                {/* SECTION BOX */}
                <div className="bg-white rounded-3xl shadow-lg p-6">
                    <div className="grid grid-cols-2 gap-4">
                        {waiting.map((w, index) => (
                            <div
                                key={index}
                                className="bg-gray-50 rounded-2xl p-10 border border-gray-200"
                            >
                                <div className="text-4xl font-semibold text-gray-800">
                                    {w.patientLastName}, {w.patientFirstName}
                                </div>

                                <div className="text-4xl text-gray-600">{w.region}</div>
                            </div>
                        ))}
                    </div>
                </div>
            </div>

            {/* UP NEXT SECTION */}
            <div>
                <h2 className="text-4xl font-bold mb-4">Up Next</h2>

                {/* SECTION BOX */}
                <div className="bg-white rounded-3xl shadow-lg p-8">
                    <div className="grid grid-cols-3 gap-4">
                        {therapists.map((t, index) => (
                            <div
                                key={index}
                                className="relative bg-gray-50 rounded-2xl p-8 border border-gray-200 flex flex-col justify-between overflow-hidden"
                            >
                                {/* Accent Bar */}
                                <div className={`absolute left-0 top-0 h-full w-3 ${t.accent}`} />

                                <div className="pl-4 text-4xl font-semibold text-gray-600">
                                    {t.name}
                                </div>

                                <div className="pl-4 text-4xl font-bold text-gray-900 flex-1 flex items-center">
                                    {t.patientLastName}, {t.patientFirstName}
                                </div>

                                <div className="pl-4 text-4xl font-bold text-gray-600">
                                    {t.region}
                                </div>
                            </div>
                        ))}
                    </div>
                </div>
            </div>
        </div>
    );
}