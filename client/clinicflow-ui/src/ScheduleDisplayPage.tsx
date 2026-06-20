import { useEffect, useMemo, useState } from "react";
import { useNavigate } from "react-router-dom";
import Alert from "@mui/material/Alert";
import Box from "@mui/material/Box";
import Button from "@mui/material/Button";
import Card from "@mui/material/Card";
import CardContent from "@mui/material/CardContent";
import Chip from "@mui/material/Chip";
import Paper from "@mui/material/Paper";
import Stack from "@mui/material/Stack";
import Typography from "@mui/material/Typography";
import { useAuth } from "./auth/AuthContext";
import { listAppointmentsByDate } from "./services/appointmentService";
import { appointmentTypeOptions, type BoardRow } from "./types/appointment";

const CLINIC_NAME = import.meta.env.VITE_CLINIC_NAME || "Mill Valley Physical Therapy";

const THERAPIST_ACCENT_PALETTE = [
  "#38bdf8", "#22c55e", "#f59e0b", "#f97316", "#a78bfa", "#ef4444", "#14b8a6", "#eab308",
];

function getTodayISODate(): string {
  const d = new Date();
  return `${d.getFullYear()}-${String(d.getMonth() + 1).padStart(2, "0")}-${String(d.getDate()).padStart(2, "0")}`;
}

function parseScheduledAt(value: string): Date {
  return new Date(value.includes("T") ? value : value.replace(" ", "T"));
}

function formatTime(value: string): string {
  const parsed = parseScheduledAt(value);
  if (Number.isNaN(parsed.getTime())) return value;
  return new Intl.DateTimeFormat("en-US", { hour: "numeric", minute: "2-digit" }).format(parsed);
}

function typeLabel(type: BoardRow["type"]): string {
  return appointmentTypeOptions.find((o) => o.value === type)?.label ?? "";
}

function accentFor(therapistId: number): string {
  return THERAPIST_ACCENT_PALETTE[Math.abs(therapistId) % THERAPIST_ACCENT_PALETTE.length];
}

function ScheduleCard({ row, size }: { row: BoardRow; size: "lg" | "sm" }) {
  const accent = accentFor(row.therapistId);
  const nameSize = size === "lg" ? "2.5rem" : "1.6rem";
  const metaSize = size === "lg" ? "1.6rem" : "1.1rem";
  const timeSize = size === "lg" ? "1.75rem" : "1.25rem";

  return (
    <Card sx={{ position: "relative", borderRadius: "16px", overflow: "hidden", bgcolor: "#263348", boxShadow: "0 4px 24px rgba(0,0,0,0.4)", height: "100%" }}>
      <Box sx={{ position: "absolute", left: 0, top: 0, height: "100%", width: 12, bgcolor: accent }} />
      <Chip
        label={formatTime(row.scheduledAt)}
        sx={{ position: "absolute", top: 16, right: 16, fontSize: timeSize, fontWeight: 600, height: "auto", py: 0.5, px: 1, bgcolor: "rgba(255,255,255,0.08)", color: "#e2e8f0", borderRadius: "10px", "& .MuiChip-label": { px: 0 } }}
      />
      <CardContent sx={{ pl: "28px", pr: 3, py: size === "lg" ? 3 : 2, display: "flex", flexDirection: "column", gap: 0.5 }}>
        <Typography sx={{ fontSize: metaSize, fontWeight: 600, color: "#94a3b8", lineHeight: 1.2 }}>
          {row.therapistName}
        </Typography>
        <Typography sx={{ fontSize: nameSize, fontWeight: 700, color: "#f1f5f9", lineHeight: 1.2 }}>
          {row.patientName}
        </Typography>
        {typeLabel(row.type) && (
          <Typography sx={{ fontSize: metaSize, fontWeight: 700, color: "#cbd5e1", lineHeight: 1.2 }}>
            {typeLabel(row.type)}
          </Typography>
        )}
      </CardContent>
    </Card>
  );
}

function EmptyPanel({ label }: { label: string }) {
  return (
    <Box sx={{ borderRadius: "16px", border: "2px dashed rgba(255,255,255,0.12)", p: 5, textAlign: "center" }}>
      <Typography sx={{ fontSize: "1.875rem", fontWeight: 600, color: "#64748b" }}>{label}</Typography>
    </Box>
  );
}

export default function ScheduleDisplayPage() {
  const { currentUser } = useAuth();
  const navigate = useNavigate();
  const [appointments, setAppointments] = useState<BoardRow[]>([]);
  const [error, setError] = useState<string | null>(null);
  const [now, setNow] = useState(() => new Date());

  useEffect(() => {
    const timer = setInterval(() => setNow(new Date()), 60000);
    return () => clearInterval(timer);
  }, []);

  useEffect(() => {
    let cancelled = false;
    async function load() {
      try {
        setError(null);
        const data = await listAppointmentsByDate(getTodayISODate());
        if (!cancelled) setAppointments(data);
      } catch (e) {
        if (!cancelled) setError(e instanceof Error ? e.message : "Failed to load appointments");
      }
    }
    void load();
    const interval = setInterval(load, 30000);
    return () => {
      cancelled = true;
      clearInterval(interval);
    };
  }, []);

  const currentTime = useMemo(() => new Intl.DateTimeFormat("en-US", { hour: "numeric", minute: "2-digit" }).format(now), [now]);
  const currentDate = useMemo(() => new Intl.DateTimeFormat("en-US", { weekday: "long", month: "long", day: "numeric" }).format(now), [now]);

  const waiting = useMemo(
    () => appointments.filter((a) => a.status === "WAITING").sort((a, b) => a.scheduledAt.localeCompare(b.scheduledAt)),
    [appointments],
  );
  const upNext = useMemo(
    () => appointments.filter((a) => a.status === "SCHEDULED").sort((a, b) => a.scheduledAt.localeCompare(b.scheduledAt)).slice(0, 6),
    [appointments],
  );

  return (
    <Stack sx={{ minHeight: "100vh", bgcolor: "#0f172a", p: 3 }} spacing={5}>
      <Box sx={{ display: "flex", alignItems: "flex-start", justifyContent: "space-between" }}>
        <Stack spacing={1} alignItems="flex-start">
          <Typography sx={{ fontSize: "2.5rem", fontWeight: 700, color: "#fff" }}>{CLINIC_NAME}</Typography>
          {currentUser?.roleName === "ADMIN" && (
            <Button variant="outlined" size="small" onClick={() => navigate("/")} sx={{ color: "#94a3b8", borderColor: "rgba(148,163,184,0.3)", textTransform: "none", fontWeight: 600 }}>
              Back to Console
            </Button>
          )}
        </Stack>
        <Box sx={{ textAlign: "right" }}>
          <Typography sx={{ fontSize: "2rem", fontWeight: 600, color: "grey.100" }}>{currentTime}</Typography>
          <Typography sx={{ fontSize: "1.5rem", color: "grey.400" }}>{currentDate}</Typography>
        </Box>
      </Box>

      {error && <Alert severity="error" sx={{ borderRadius: "16px", fontSize: "1.25rem" }}>{error}</Alert>}

      {/* PRIMARY: Currently Waiting */}
      <Stack spacing={2}>
        <Typography sx={{ fontSize: "2.5rem", fontWeight: 800, color: "#fff" }}>Currently Waiting</Typography>
        <Paper elevation={0} sx={{ borderRadius: "24px", p: 3, bgcolor: "#1e293b" }}>
          {waiting.length === 0 ? (
            <EmptyPanel label="No one waiting" />
          ) : (
            <Box sx={{ display: "grid", gridTemplateColumns: { xs: "1fr", md: "repeat(2, 1fr)", xl: "repeat(3, 1fr)" }, gap: 2 }}>
              {waiting.map((row) => (
                <ScheduleCard key={row.id} row={row} size="lg" />
              ))}
            </Box>
          )}
        </Paper>
      </Stack>

      {/* SECONDARY: Up Next */}
      <Stack spacing={1.5}>
        <Typography sx={{ fontSize: "1.5rem", fontWeight: 700, color: "#94a3b8" }}>Up Next</Typography>
        <Paper elevation={0} sx={{ borderRadius: "20px", p: 2.5, bgcolor: "rgba(30, 41, 59, 0.6)" }}>
          {upNext.length === 0 ? (
            <EmptyPanel label="Nothing upcoming" />
          ) : (
            <Box sx={{ display: "grid", gridTemplateColumns: { xs: "1fr", md: "repeat(2, 1fr)", xl: "repeat(3, 1fr)" }, gap: 1.5 }}>
              {upNext.map((row) => (
                <ScheduleCard key={row.id} row={row} size="sm" />
              ))}
            </Box>
          )}
        </Paper>
      </Stack>
    </Stack>
  );
}
