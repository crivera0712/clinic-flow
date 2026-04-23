import { useEffect, useMemo, useState } from "react";
import { useNavigate } from "react-router-dom";
import { fetchAppointmentsByDate } from "./api/displayBoard";
import { useAuth } from "./auth/AuthContext";

import Alert from "@mui/material/Alert";
import Box from "@mui/material/Box";
import Button from "@mui/material/Button";
import Card from "@mui/material/Card";
import CardContent from "@mui/material/CardContent";
import Chip from "@mui/material/Chip";
import Paper from "@mui/material/Paper";
import Stack from "@mui/material/Stack";
import Typography from "@mui/material/Typography";
import type { AppointmentDisplay } from "./types/appointment";

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

function formatAppointmentType(type: AppointmentDisplay["type"]): string | null {
    switch (type) {
        case "EVALUATION":
            return "Evaluation";
        case "REASSESSMENT":
            return "Reassessment";
        default:
            return null;
    }
}

const THERAPIST_ACCENT_PALETTE = [
    "#38bdf8",
    "#22c55e",
    "#f59e0b",
    "#f97316",
    "#a78bfa",
    "#ef4444",
    "#14b8a6",
    "#eab308",
];

function getTherapistAccent(therapistId: number): string {
    return THERAPIST_ACCENT_PALETTE[Math.abs(therapistId) % THERAPIST_ACCENT_PALETTE.length];
}

type CardItem = AppointmentDisplay & { accent: string };

function toCardItem(a: AppointmentDisplay): CardItem {
    return {
        ...a,
        accent: getTherapistAccent(a.therapistId),
    };
}

// ---- UI Components ----
function ScheduleCard({ item }: { item: CardItem }) {
    const appointmentType = formatAppointmentType(item.type);

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
                height: "100%",
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
                    {item.displayName}
                </Typography>

                <Stack spacing={0.25}>
                    {appointmentType && (
                        <Typography sx={{ fontSize: "1.5rem", fontWeight: 700, color: "#cbd5e1", lineHeight: 1.2 }}>
                            {appointmentType}
                        </Typography>
                    )}

                    {/* Body region */}
                    <Typography sx={{ fontSize: "2.25rem", fontWeight: 700, color: "#94a3b8", lineHeight: 1.2 }}>
                        {item.bodyRegionDisplayName}
                    </Typography>
                </Stack>
            </CardContent>
        </Card>
    );
}

export default function ScheduleDisplayPage() {
    const { currentUser } = useAuth();
    const navigate = useNavigate();
    const [appointments, setAppointments] = useState<AppointmentDisplay[]>([]);
    const [error, setError] = useState<string | null>(null);
    const [loading, setLoading] = useState(true);

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
                setLoading(true);
                setError(null);
                const today = getTodayISODate();
                const data = await fetchAppointmentsByDate(today);
                if (!cancelled) setAppointments(data);
            } catch (e) {
                if (!cancelled) setError(e instanceof Error ? e.message : "Failed to load appointments");
            } finally {
                if (!cancelled) setLoading(false);
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
        <Stack sx={{ minHeight: "100vh", bgcolor: "#0f172a", p: 2 }} spacing={5}>
            {/* HEADER */}
            <Box sx={{ display: "flex", alignItems: "flex-start", justifyContent: "space-between" }}>
                <Stack spacing={1} alignItems="flex-start">
                    <Typography sx={{ fontSize: "2.25rem", fontWeight: 700, color: "#fff" }}>
                        Mill Valley Physical Therapy
                    </Typography>
                    {currentUser?.roleName === "ADMIN" && (
                        <Button
                            variant="outlined"
                            size="small"
                            onClick={() => navigate("/admin")}
                            sx={{
                                color: "#94a3b8",
                                borderColor: "rgba(148,163,184,0.3)",
                                "&:hover": { borderColor: "#94a3b8", bgcolor: "rgba(148,163,184,0.08)" },
                                textTransform: "none",
                                fontWeight: 600,
                            }}
                        >
                            Admin Panel
                        </Button>
                    )}
                </Stack>

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

            {loading && !error && (
                <Alert severity="info" sx={{ borderRadius: "16px", fontSize: "1.125rem" }}>
                    Loading today&apos;s appointments...
                </Alert>
            )}

            {/* WAITING SECTION */}
            <Stack spacing={2}>
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
                        <Stack direction="row" spacing={2}>
                            {waitingSlots.map((w, i) =>
                                w ? (
                                    <Box key={i} sx={{ flex: 1 }}>
                                        <ScheduleCard item={w} />
                                    </Box>
                                ) : (
                                    <Box
                                        key={i}
                                        sx={{
                                            flex: 1,
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
                        </Stack>
                    )}
                </Paper>
            </Stack>

            {/* UP NEXT SECTION */}
            <Stack spacing={2}>
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
                        <Stack
                            direction="row"
                            spacing={2}
                            useFlexGap
                            sx={{
                                flexWrap: "wrap",
                            }}
                        >
                            {visibleUpNext.map((t, index) => (
                                <Box
                                    key={index}
                                    sx={{
                                        flex: {
                                            xs: "1 1 100%",
                                            md: "1 1 calc(50% - 8px)",
                                        },
                                        minWidth: 0,
                                    }}
                                >
                                    <ScheduleCard item={t} />
                                </Box>
                            ))}
                        </Stack>
                    )}
                </Paper>
            </Stack>
        </Stack>
    );
}
