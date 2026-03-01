export default function ScheduleDisplayPage() {
    const therapists = [
        {
            name: "Leon Kennedy",
            patientFirstName: "Wesker,",
            patientLastName: "A",
            region: "Knee"
        },
        {
            name: "Jill Valentine",
            patientFirstName: "Carlos",
            patientLastName: "Olivera",
            region: "Low back"
        },
        {
            name: "Chris Redfield",
            patientFirstName: "Ada",
            patientLastName: "Wong",
            region: "Hip"
        }
    ];

    return (
        <div className="min-h-screen mt-0">
            <div className="m-6 grid grid-cols-3 gap-3">
                {therapists.map((t) => (
                    <div className="mt-20 rounded-3xl bg-[#FAF9F6] p-10 shadow-md flex flex-col">
                        <div className="text-3xl font-semibold text-gray-600">
                            {t.name}
                        </div>
                        <div className="mt-8 text-5xl font-semibold text-gray-900 text-center flex-1 flex items-center ">
                            {t.patientFirstName} {t.patientLastName}
                        </div>
                        <div className="mt-2 text-4xl text-gray-600">
                            {t.region}
                        </div>
                    </div>
                ))}
            </div>
        </div>
    );
}