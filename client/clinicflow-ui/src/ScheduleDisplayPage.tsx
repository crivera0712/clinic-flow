import { useEffect, useMemo, useState } from "react";
//import { fetchAppointmentsByDate } from "./api/displayBoard";

import Alert from "@mui/material/Alert";
import Box from "@mui/material/Box";
import Card from "@mui/material/Card";
import CardContent from "@mui/material/CardContent";
import Chip from "@mui/material/Chip";
import Paper from "@mui/material/Paper";
import Typography from "@mui/material/Typography";

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
    "Ada Wong":       "#3b82f6",
    "Leon Kennedy":   "#16a34a",
    "Jill Valentine": "#9333ea",
};
const DEFAULT_ACCENT = "#9ca3af";

type CardItem = AppointmentDisplay & { accent: string };

function toCardItem(a: AppointmentDisplay): CardItem {
    return {
        ...a,
        accent: therapistAccentMap[a.therapistName] ?? DEFAULT_ACCENT,
    };
}

// ---- UI Components ----
function ScheduleCard({ item }: { item: CardItem }) {
    return (
        <Card
            sx={{
                position: "relative",
                borderRadius: "16px",
                overflow: "hidden",
                bgcolor: "#263348",
                boxShadow: "0 4px 24px rgba(0,0,0,0.4)",
                display: "flex",
                flexDirection: "column",
                justifyContent: "space-between",
                minHeight: 160,
            }}
        >
            {/* Accent bar */}
            <Box
                sx={{
                    position: "absolute",
                    left: 0,
                    top: 0,
                    height: "100%",
                    width: 12,
                    bgcolor: item.accent,
                    zIndex: 1,
                }}
            />

            {/* Time chip — top-right */}
            <Chip
                label={formatTime(item.scheduledAt)}
                sx={{
                    position: "absolute",
                    top: 16,
                    right: 16,
                    fontSize: "1.5rem",
                    fontWeight: 600,
                    height: "auto",
                    py: 0.5,
                    px: 1,
                    bgcolor: "rgba(255,255,255,0.08)",
                    color: "#e2e8f0",
                    borderRadius: "10px",
                    "& .MuiChip-label": { px: 0 },
                    zIndex: 2,
                }}
            />

            <CardContent
                sx={{
                    pl: "28px",
                    pr: 3,
                    pt: 3,
                    pb: "16px !important",
                    display: "flex",
                    flexDirection: "column",
                    justifyContent: "space-between",
                    flex: 1,
                    gap: 1,
                }}
            >
                {/* Therapist name */}
                <Typography sx={{ fontSize: "2.25rem", fontWeight: 600, color: "#94a3b8", lineHeight: 1.2 }}>
                    {item.therapistName}
                </Typography>

                {/* Patient name */}
                <Typography
                    sx={{
                        fontSize: "2.25rem",
                        fontWeight: 700,
                        color: "#f1f5f9",
                        flex: 1,
                        display: "flex",
                        alignItems: "center",
                        lineHeight: 1.2,
                    }}
                >
                    {item.lastName}, {item.firstName}
                </Typography>

                {/* Body region */}
                <Typography sx={{ fontSize: "2.25rem", fontWeight: 700, color: "#94a3b8", lineHeight: 1.2 }}>
                    {item.bodyRegionDisplayName}
                </Typography>
            </CardContent>
        </Card>
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
        <Box sx={{ minHeight: "100vh", display: "flex", flexDirection: "column", bgcolor: "#0f172a", p: 2, gap: 5 }}>
            {/* HEADER */}
            <Box sx={{ display: "flex", alignItems: "flex-start", justifyContent: "space-between" }}>
                <Typography sx={{ fontSize: "2.25rem", fontWeight: 700, color: "#fff" }}>
                    Mill Valley Physical Therapy
                </Typography>

                <Box sx={{ textAlign: "right" }}>
                    <Typography sx={{ fontSize: "1.875rem", fontWeight: 600, color: "grey.100" }}>
                        {currentTime}
                    </Typography>
                    <Typography sx={{ fontSize: "1.5rem", color: "grey.400" }}>
                        {currentDate}
                    </Typography>
                </Box>
            </Box>

            {error && (
                <Alert severity="error" sx={{ borderRadius: "16px", fontSize: "1.5rem" }}>
                    {error}
                </Alert>
            )}

            {/* WAITING SECTION */}
            <Box>
                <Typography sx={{ fontSize: "2.25rem", fontWeight: 700, mb: 2, color: "#fff" }}>
                    Waiting Room
                </Typography>

                <Paper elevation={0} sx={{ borderRadius: "24px", p: 3, bgcolor: "#1e293b" }}>
                    {waiting.length > 2 && (
                        <Typography sx={{ fontSize: "1.125rem", color: "#64748b", textAlign: "right", mb: 1 }}>
                            +{waiting.length - 2} more waiting
                        </Typography>
                    )}

                    {visibleWaiting.length === 0 ? (
                        <Box
                            sx={{
                                borderRadius: "16px",
                                border: "2px dashed",
                                borderColor: "rgba(255,255,255,0.12)",
                                p: 5,
                                textAlign: "center",
                            }}
                        >
                            <Typography sx={{ fontSize: "1.875rem", fontWeight: 600, color: "#64748b" }}>
                                No one waiting
                            </Typography>
                        </Box>
                    ) : (
                        <Box sx={{ display: "grid", gridTemplateColumns: "1fr 1fr", gap: 2 }}>
                            {waitingSlots.map((w, i) =>
                                w ? (
                                    <ScheduleCard key={i} item={w} />
                                ) : (
                                    <Box
                                        key={i}
                                        sx={{
                                            borderRadius: "16px",
                                            border: "2px dashed",
                                            borderColor: "rgba(255,255,255,0.08)",
                                            p: 4,
                                            display: "flex",
                                            alignItems: "center",
                                            justifyContent: "center",
                                            minHeight: 160,
                                        }}
                                    >
                                        <Typography sx={{ fontSize: "1.5rem", fontWeight: 600, color: "#475569" }}>
                                            Empty
                                        </Typography>
                                    </Box>
                                )
                            )}
                        </Box>
                    )}
                </Paper>
            </Box>

            {/* UP NEXT SECTION */}
            <Box>
                <Typography sx={{ fontSize: "2.25rem", fontWeight: 700, mb: 2, color: "#fff" }}>
                    Up Next
                </Typography>

                <Paper elevation={0} sx={{ borderRadius: "24px", p: 4, bgcolor: "#1e293b" }}>
                    {visibleUpNext.length === 0 ? (
                        <Box
                            sx={{
                                borderRadius: "16px",
                                border: "2px dashed",
                                borderColor: "rgba(255,255,255,0.12)",
                                p: 5,
                                textAlign: "center",
                            }}
                        >
                            <Typography sx={{ fontSize: "1.875rem", fontWeight: 600, color: "#64748b" }}>
                                No upcoming appointments
                            </Typography>
                            <Typography sx={{ fontSize: "1.5rem", color: "#475569", mt: 1 }}>
                                End of day ✅
                            </Typography>
                        </Box>
                    ) : (
                        <Box
                            sx={{
                                display: "grid",
                                gridTemplateColumns: "repeat(auto-fill, minmax(calc(50% - 8px), 1fr))",
                                gap: 2,
                            }}
                        >
                            {visibleUpNext.map((t, index) => (
                                <ScheduleCard key={index} item={t} />
                            ))}
                        </Box>
                    )}
                </Paper>
            </Box>
        </Box>
    );
}
