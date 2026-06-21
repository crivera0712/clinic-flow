import { useEffect, useMemo, useState } from "react";
import { useNavigate } from "react-router-dom";
import ArrowBackRoundedIcon from "@mui/icons-material/ArrowBackRounded";
import EventAvailableRoundedIcon from "@mui/icons-material/EventAvailableRounded";
import GroupsRoundedIcon from "@mui/icons-material/GroupsRounded";
import Alert from "@mui/material/Alert";
import Box from "@mui/material/Box";
import Button from "@mui/material/Button";
import Card from "@mui/material/Card";
import CardContent from "@mui/material/CardContent";
import Chip from "@mui/material/Chip";
import Paper from "@mui/material/Paper";
import Stack from "@mui/material/Stack";
import Typography from "@mui/material/Typography";
import { alpha } from "@mui/material/styles";
import { useAuth } from "./auth/AuthContext";
import { listAppointmentsByDate } from "./services/appointmentService";
import { colors } from "./theme";
import { appointmentTypeOptions, type BoardRow } from "./types/appointment";

const CLINIC_NAME = import.meta.env.VITE_CLINIC_NAME || "Mill Valley Physical Therapy";

const THERAPIST_ACCENT_PALETTE = [
  "#38bdf8", "#2dd4bf", "#fbbf24", "#fb923c", "#a78bfa", "#fb7185", "#34d399", "#facc15",
];

function getTodayISODate(): string {
  const d = new Date();
  return `${d.getFullYear()}-${String(d.getMonth() + 1).padStart(2, "0")}-${String(d.getDate()).padStart(2, "0")}`;
}

function formatTime(value: string): string {
  const parsed = new Date(value.includes("T") ? value : value.replace(" ", "T"));
  if (Number.isNaN(parsed.getTime())) return value;
  return new Intl.DateTimeFormat("en-US", { hour: "numeric", minute: "2-digit" }).format(parsed);
}

function typeLabel(type: BoardRow["type"]): string {
  return appointmentTypeOptions.find((option) => option.value === type)?.label ?? "";
}

function accentFor(therapistId: number): string {
  return THERAPIST_ACCENT_PALETTE[Math.abs(therapistId) % THERAPIST_ACCENT_PALETTE.length];
}

function ScheduleCard({ row, prominence }: { row: BoardRow; prominence: "primary" | "secondary" }) {
  const accent = accentFor(row.therapistId);
  const large = prominence === "primary";

  return (
    <Card
      sx={{
        position: "relative",
        overflow: "hidden",
        height: "100%",
        bgcolor: large ? colors.surfaceRaised : alpha(colors.surfaceRaised, 0.65),
        borderColor: alpha(accent, large ? 0.34 : 0.18),
        boxShadow: large ? "0 18px 45px rgba(0,0,0,.27)" : "none",
        "&::before": { content: '""', position: "absolute", inset: "0 auto 0 0", width: large ? 8 : 5, bgcolor: accent },
      }}
    >
      <CardContent sx={{ p: { xs: 2.5, lg: large ? 3.5 : 2.75 }, pl: { xs: 3.25, lg: large ? 4.25 : 3.5 }, "&:last-child": { pb: { xs: 2.5, lg: large ? 3.5 : 2.75 } } }}>
        <Stack direction="row" spacing={2} justifyContent="space-between" alignItems="flex-start">
          <Box sx={{ minWidth: 0 }}>
            <Typography sx={{ color: accent, fontSize: large ? "clamp(1rem, 1.35vw, 1.4rem)" : "clamp(.9rem, 1vw, 1.1rem)", fontWeight: 750 }}>
              {row.therapistName}
            </Typography>
            <Typography sx={{ mt: 0.6, fontSize: large ? "clamp(1.8rem, 3vw, 3.35rem)" : "clamp(1.35rem, 2vw, 2.05rem)", fontWeight: 760, lineHeight: 1.08, letterSpacing: "-.03em" }}>
              {row.patientName}
            </Typography>
            <Typography color="text.secondary" sx={{ mt: 1, fontSize: large ? "clamp(.95rem, 1.25vw, 1.3rem)" : "clamp(.85rem, 1vw, 1.05rem)", fontWeight: 600 }}>
              {typeLabel(row.type)}
            </Typography>
          </Box>
          <Chip
            label={formatTime(row.scheduledAt)}
            sx={{ flexShrink: 0, height: "auto", py: 0.75, px: 0.5, bgcolor: alpha(accent, 0.11), color: accent, border: `1px solid ${alpha(accent, 0.26)}`, fontSize: large ? "clamp(1rem, 1.45vw, 1.5rem)" : "clamp(.9rem, 1vw, 1.1rem)" }}
          />
        </Stack>
      </CardContent>
    </Card>
  );
}

function EmptyPanel({ title, description }: { title: string; description: string }) {
  return (
    <Stack alignItems="center" justifyContent="center" spacing={1.25} sx={{ minHeight: 150, p: 4, textAlign: "center", borderRadius: 3, border: `1px dashed ${colors.border}` }}>
      <EventAvailableRoundedIcon sx={{ fontSize: 38, color: "text.disabled" }} />
      <Typography variant="h5">{title}</Typography>
      <Typography color="text.secondary">{description}</Typography>
    </Stack>
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
      } catch (loadError) {
        if (!cancelled) setError(loadError instanceof Error ? loadError.message : "Failed to load appointments");
      }
    }
    void load();
    const interval = setInterval(load, 30000);
    return () => { cancelled = true; clearInterval(interval); };
  }, []);

  const currentTime = useMemo(() => new Intl.DateTimeFormat("en-US", { hour: "numeric", minute: "2-digit" }).format(now), [now]);
  const currentDate = useMemo(() => new Intl.DateTimeFormat("en-US", { weekday: "long", month: "long", day: "numeric" }).format(now), [now]);
  const waiting = useMemo(() => appointments.filter((appointment) => appointment.status === "WAITING").sort((a, b) => a.scheduledAt.localeCompare(b.scheduledAt)), [appointments]);
  const upNext = useMemo(() => appointments.filter((appointment) => appointment.status === "SCHEDULED").sort((a, b) => a.scheduledAt.localeCompare(b.scheduledAt)).slice(0, 6), [appointments]);

  return (
    <Box sx={{ minHeight: "100vh", p: { xs: 2, sm: 3, lg: 4 }, background: "radial-gradient(circle at 0% 0%, rgba(56,189,248,.08), transparent 28%), #050b18" }}>
      <Stack spacing={{ xs: 3, lg: 4 }} sx={{ maxWidth: 1800, mx: "auto" }}>
        <Stack direction={{ xs: "column", sm: "row" }} spacing={2} alignItems={{ sm: "center" }} justifyContent="space-between">
          <Typography component="h1" sx={{ fontSize: "clamp(1.45rem, 2.3vw, 2.5rem)", fontWeight: 750, lineHeight: 1.15 }}>{CLINIC_NAME}</Typography>
          <Stack direction="row" spacing={{ xs: 2, sm: 3 }} alignItems="center" justifyContent="space-between">
            {currentUser?.roleName === "ADMIN" && (
              <Button variant="outlined" startIcon={<ArrowBackRoundedIcon />} onClick={() => navigate("/")}>Console</Button>
            )}
            <Box sx={{ textAlign: "right" }}>
              <Typography sx={{ fontSize: "clamp(1.35rem, 2vw, 2.2rem)", fontWeight: 720, lineHeight: 1 }}>{currentTime}</Typography>
              <Typography color="text.secondary" sx={{ mt: 0.75, fontSize: "clamp(.85rem, 1.1vw, 1.15rem)" }}>{currentDate}</Typography>
            </Box>
          </Stack>
        </Stack>

        {error && <Alert severity="error">{error}</Alert>}

        <Stack spacing={1.5}>
          <Stack direction="row" spacing={1.25} alignItems="center">
            <GroupsRoundedIcon sx={{ color: "success.main" }} />
            <Typography component="h2" sx={{ fontSize: "clamp(1.45rem, 2.1vw, 2.35rem)", fontWeight: 750 }}>Currently waiting</Typography>
            <Chip label={waiting.length} color="success" variant="outlined" />
          </Stack>
          <Paper sx={{ p: { xs: 1.5, sm: 2, lg: 2.5 }, bgcolor: alpha(colors.surface, 0.82) }}>
            {waiting.length === 0 ? <EmptyPanel title="No one is waiting" description="Checked-in patients will appear here." /> : (
              <Box sx={{ display: "grid", gridTemplateColumns: { xs: "1fr", md: "repeat(2, minmax(0, 1fr))", xl: "repeat(3, minmax(0, 1fr))" }, gap: { xs: 1.5, lg: 2 } }}>
                {waiting.map((row) => <ScheduleCard key={row.id} row={row} prominence="primary" />)}
              </Box>
            )}
          </Paper>
        </Stack>

        <Stack spacing={1.5}>
          <Stack direction="row" spacing={1.25} alignItems="center">
            <EventAvailableRoundedIcon sx={{ color: "primary.main" }} />
            <Typography component="h2" sx={{ fontSize: "clamp(1.2rem, 1.55vw, 1.65rem)", fontWeight: 720 }}>Up next</Typography>
            <Chip label={upNext.length} color="primary" variant="outlined" size="small" />
          </Stack>
          <Paper sx={{ p: { xs: 1.5, sm: 2 }, bgcolor: alpha(colors.surface, 0.5) }}>
            {upNext.length === 0 ? <EmptyPanel title="Nothing upcoming" description="Scheduled patients will appear here." /> : (
              <Box sx={{ display: "grid", gridTemplateColumns: { xs: "1fr", sm: "repeat(2, minmax(0, 1fr))", xl: "repeat(3, minmax(0, 1fr))" }, gap: 1.5 }}>
                {upNext.map((row) => <ScheduleCard key={row.id} row={row} prominence="secondary" />)}
              </Box>
            )}
          </Paper>
        </Stack>
      </Stack>
    </Box>
  );
}
