import { useEffect, useMemo, useState } from "react";
import { useNavigate } from "react-router-dom";
import ArrowBackRoundedIcon from "@mui/icons-material/ArrowBackRounded";
import EventAvailableRoundedIcon from "@mui/icons-material/EventAvailableRounded";
import GroupsRoundedIcon from "@mui/icons-material/GroupsRounded";
import LogoutRoundedIcon from "@mui/icons-material/LogoutRounded";
import Alert from "@mui/material/Alert";
import Box from "@mui/material/Box";
import Button from "@mui/material/Button";
import Card from "@mui/material/Card";
import CardContent from "@mui/material/CardContent";
import Chip from "@mui/material/Chip";
import Paper from "@mui/material/Paper";
import Stack from "@mui/material/Stack";
import Typography from "@mui/material/Typography";
import { keyframes } from "@mui/material/styles";
import { useAuth } from "./auth/AuthContext";
import { ColorModeIconButton } from "./components/ui/ColorModeToggle";
import { useTherapistAccents } from "./components/ui/therapistAccents";
import { useAppointments } from "./queries/appointments";
import { useTherapists } from "./queries/therapists";
import { appointmentTypeOptions, type BoardRow } from "./types/appointment";
import { boardName } from "./types/patient";

const CLINIC_NAME = import.meta.env.VITE_CLINIC_NAME || "Mill Valley Physical Therapy";

// The board runs all day on a TV: reload every 30s, and whenever the tab regains focus.
const BOARD_REFRESH_MS = 30_000;
// No successful update for this long means the board is probably offline.
const STALE_AFTER_MS = 2 * 60_000;

// Type sizes that grow with the screen width, so the board reads from across the room on a
// 1080p TV and keeps growing on a 4K TV running at 100% scaling (3840 CSS px wide).
const size = {
  clinic: "clamp(1.45rem, 2.2vw, 4.4rem)",
  clock: "clamp(1.35rem, 2.1vw, 4.2rem)",
  date: "clamp(.85rem, 1.1vw, 2.2rem)",
  legend: "clamp(.85rem, 1vw, 2rem)",
  waitingTitle: "clamp(1.45rem, 2.1vw, 4.2rem)",
  upNextTitle: "clamp(1.2rem, 1.6vw, 3.2rem)",
  primary: {
    therapist: "clamp(1rem, 1.35vw, 2.7rem)",
    patient: "clamp(1.8rem, 3vw, 6rem)",
    type: "clamp(.95rem, 1.25vw, 2.5rem)",
    time: "clamp(1rem, 1.5vw, 3rem)",
  },
  secondary: {
    therapist: "clamp(.9rem, 1vw, 2rem)",
    patient: "clamp(1.35rem, 2vw, 4rem)",
    type: "clamp(.85rem, 1vw, 2rem)",
    time: "clamp(.9rem, 1.05vw, 2.1rem)",
  },
};

const pulse = keyframes`
  0% { box-shadow: 0 0 0 0 currentColor; }
  70% { box-shadow: 0 0 0 8px transparent; }
  100% { box-shadow: 0 0 0 0 transparent; }
`;

function toISODate(d: Date): string {
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

// "Updated 12s ago", turning amber ("Reconnecting…") when updates stop.
function LiveIndicator({ updatedAt, failing }: { updatedAt: number; failing: boolean }) {
  const [now, setNow] = useState(() => Date.now());

  useEffect(() => {
    const timer = setInterval(() => setNow(Date.now()), 1000);
    return () => clearInterval(timer);
  }, []);

  const ageMs = Math.max(0, now - updatedAt);
  const seconds = Math.floor(ageMs / 1000);
  const ago =
    seconds < 10
      ? "just now"
      : seconds < 60
        ? `${seconds}s ago`
        : `${Math.floor(seconds / 60)} min ago`;
  const live = updatedAt > 0 && !failing && ageMs < STALE_AFTER_MS;
  const label =
    updatedAt === 0 ? "Connecting…" : live ? `Updated ${ago}` : `Reconnecting… last updated ${ago}`;

  return (
    <Stack direction="row" spacing={1} alignItems="center" sx={{ flexShrink: 0 }}>
      <Box
        aria-hidden="true"
        sx={{
          width: 10,
          height: 10,
          borderRadius: "50%",
          color: live ? "success.main" : "warning.main",
          bgcolor: "currentColor",
          animation: live ? `${pulse} 2s ease-out infinite` : "none",
        }}
      />
      <Typography
        sx={{
          fontSize: size.legend,
          fontWeight: 600,
          color: live ? "text.secondary" : "warning.main",
        }}
      >
        {label}
      </Typography>
    </Stack>
  );
}

function ScheduleCard({
  row,
  prominence,
  accent,
}: {
  row: BoardRow;
  prominence: "primary" | "secondary";
  accent: string;
}) {
  const large = prominence === "primary";
  const sizes = large ? size.primary : size.secondary;

  return (
    <Card
      sx={(theme) => ({
        position: "relative",
        overflow: "hidden",
        height: "100%",
        bgcolor: large ? "background.paper" : "background.subtle",
        color: "text.primary",
        borderColor: large ? theme.alpha(accent, 0.32) : "divider",
        borderRadius: 2,
        boxShadow: large ? theme.vars.palette.elevation.card : "none",
        "&::before": {
          content: '""',
          position: "absolute",
          inset: "0 auto 0 0",
          width: large ? "clamp(8px, 0.45vw, 16px)" : "clamp(5px, 0.3vw, 10px)",
          bgcolor: accent,
        },
      })}
    >
      <CardContent
        sx={{
          p: { xs: 2.5, lg: large ? 3.5 : 2.75 },
          pl: { xs: 3.25, lg: large ? 4.25 : 3.5 },
          "&:last-child": { pb: { xs: 2.5, lg: large ? 3.5 : 2.75 } },
        }}
      >
        <Stack direction="row" spacing={2} justifyContent="space-between" alignItems="flex-start">
          <Box sx={{ minWidth: 0 }}>
            <Typography sx={{ color: accent, fontSize: sizes.therapist, fontWeight: 750 }}>
              {row.therapistName}
            </Typography>
            <Typography
              sx={{
                mt: 0.6,
                fontSize: sizes.patient,
                fontWeight: 760,
                lineHeight: 1.08,
                letterSpacing: "-.03em",
              }}
            >
              {boardName(row.patientName)}
            </Typography>
            <Typography
              color="text.secondary"
              sx={{ mt: 1, fontSize: sizes.type, fontWeight: 600 }}
            >
              {typeLabel(row.type)}
            </Typography>
          </Box>
          <Chip
            label={formatTime(row.scheduledAt)}
            sx={(theme) => ({
              flexShrink: 0,
              height: "auto",
              py: 0.75,
              px: 0.5,
              bgcolor: theme.alpha(accent, 0.08),
              color: accent,
              border: `1px solid ${theme.alpha(accent, 0.22)}`,
              borderRadius: 1.25,
              fontSize: sizes.time,
            })}
          />
        </Stack>
      </CardContent>
    </Card>
  );
}

function EmptyPanel({ title, description }: { title: string; description: string }) {
  return (
    <Stack
      alignItems="center"
      justifyContent="center"
      spacing={1.25}
      sx={{
        minHeight: "clamp(150px, 12vw, 320px)",
        p: 4,
        textAlign: "center",
        borderRadius: 2,
        bgcolor: "background.subtle",
        color: "text.primary",
        border: "1px dashed",
        borderColor: "border.strong",
      }}
    >
      <EventAvailableRoundedIcon
        sx={{ fontSize: "clamp(38px, 2.5vw, 76px)", color: "text.disabled" }}
      />
      <Typography sx={{ fontSize: size.upNextTitle, fontWeight: 700 }}>{title}</Typography>
      <Typography color="text.secondary" sx={{ fontSize: size.legend }}>
        {description}
      </Typography>
    </Stack>
  );
}

export default function ScheduleDisplayPage() {
  const { currentUser, logout } = useAuth();
  const navigate = useNavigate();
  const [now, setNow] = useState(() => new Date());

  useEffect(() => {
    const timer = setInterval(() => setNow(new Date()), 60000);
    return () => clearInterval(timer);
  }, []);

  // `now` ticks every minute, so the board moves to the next day's schedule after midnight.
  const appointmentsQuery = useAppointments(toISODate(now), { refetchInterval: BOARD_REFRESH_MS });
  const appointments = useMemo(() => appointmentsQuery.data ?? [], [appointmentsQuery.data]);
  // Accent colors come from the full therapist list, so they match the console.
  const therapists = useTherapists({ refetchInterval: BOARD_REFRESH_MS }).data ?? [];

  const currentTime = useMemo(
    () => new Intl.DateTimeFormat("en-US", { hour: "numeric", minute: "2-digit" }).format(now),
    [now],
  );
  const currentDate = useMemo(
    () =>
      new Intl.DateTimeFormat("en-US", { weekday: "long", month: "long", day: "numeric" }).format(
        now,
      ),
    [now],
  );
  const waiting = useMemo(
    () =>
      appointments
        .filter((appointment) => appointment.status === "WAITING")
        .sort((a, b) => a.scheduledAt.localeCompare(b.scheduledAt)),
    [appointments],
  );
  const upNext = useMemo(
    () =>
      appointments
        .filter((appointment) => appointment.status === "SCHEDULED")
        .sort((a, b) => a.scheduledAt.localeCompare(b.scheduledAt))
        .slice(0, 6),
    [appointments],
  );
  const therapistAccents = useTherapistAccents([
    ...therapists.map((therapist) => therapist.id),
    ...appointments.map((appointment) => appointment.therapistId),
  ]);

  // Legend: only the therapists with someone on the board, in first-appearance order.
  const legend = useMemo(() => {
    const seen = new Map<number, string>();
    for (const row of [...waiting, ...upNext]) {
      if (!seen.has(row.therapistId)) seen.set(row.therapistId, row.therapistName);
    }
    return [...seen.entries()];
  }, [waiting, upNext]);

  // A failed refresh keeps showing the last data (the live indicator turns amber); only a board
  // that never loaded shows an error.
  const loadFailed = appointmentsQuery.isError && !appointmentsQuery.data;

  return (
    <Box
      sx={{
        minHeight: "100vh",
        p: { xs: 2, sm: 3, lg: 4 },
        bgcolor: "background.default",
        color: "text.primary",
      }}
    >
      <Stack spacing={{ xs: 3, lg: 4 }} sx={{ maxWidth: 3400, mx: "auto" }}>
        <Stack
          direction={{ xs: "column", sm: "row" }}
          spacing={2}
          alignItems={{ sm: "center" }}
          justifyContent="space-between"
        >
          <Typography
            component="h1"
            sx={{ fontSize: size.clinic, fontWeight: 750, lineHeight: 1.15 }}
          >
            {CLINIC_NAME}
          </Typography>
          <Stack
            direction="row"
            spacing={{ xs: 2, sm: 3 }}
            alignItems="center"
            justifyContent="space-between"
          >
            {currentUser?.roleName === "ADMIN" && (
              <Button
                variant="outlined"
                startIcon={<ArrowBackRoundedIcon />}
                onClick={() => navigate("/")}
                sx={(theme) => ({
                  color: "primary.dark",
                  borderColor: "border.strong",
                  bgcolor: "background.paper",
                  "&:hover": {
                    borderColor: "primary.main",
                    bgcolor: theme.alpha(theme.vars.palette.primary.main, 0.04),
                  },
                })}
              >
                Console
              </Button>
            )}
            <Box sx={{ textAlign: "right" }}>
              <Typography sx={{ fontSize: size.clock, fontWeight: 720, lineHeight: 1 }}>
                {currentTime}
              </Typography>
              <Typography sx={{ mt: 0.75, color: "text.secondary", fontSize: size.date }}>
                {currentDate}
              </Typography>
            </Box>
          </Stack>
        </Stack>

        <Stack
          direction={{ xs: "column", md: "row" }}
          spacing={{ xs: 1.5, md: 3 }}
          alignItems={{ md: "center" }}
          justifyContent="space-between"
        >
          <Stack
            component="ul"
            aria-label="Therapists"
            direction="row"
            flexWrap="wrap"
            columnGap={3}
            rowGap={1}
            sx={{ listStyle: "none", m: 0, p: 0 }}
          >
            {legend.map(([therapistId, name]) => (
              <Stack
                component="li"
                key={therapistId}
                direction="row"
                spacing={1}
                alignItems="center"
              >
                <Box
                  aria-hidden="true"
                  sx={{
                    width: "clamp(10px, 0.7vw, 22px)",
                    height: "clamp(10px, 0.7vw, 22px)",
                    borderRadius: "50%",
                    bgcolor: therapistAccents.get(therapistId),
                  }}
                />
                <Typography sx={{ fontSize: size.legend, fontWeight: 650 }}>{name}</Typography>
              </Stack>
            ))}
          </Stack>
          <LiveIndicator
            updatedAt={appointmentsQuery.dataUpdatedAt}
            failing={appointmentsQuery.isError}
          />
        </Stack>

        {loadFailed && (
          <Alert severity="error">
            {appointmentsQuery.error instanceof Error
              ? appointmentsQuery.error.message
              : "Failed to load appointments"}
          </Alert>
        )}

        <Stack spacing={1.5}>
          <Stack direction="row" spacing={1.25} alignItems="center">
            <GroupsRoundedIcon sx={{ color: "status.waiting", fontSize: size.waitingTitle }} />
            <Typography component="h2" sx={{ fontSize: size.waitingTitle, fontWeight: 750 }}>
              Currently waiting
            </Typography>
            <Chip
              label={waiting.length}
              variant="outlined"
              sx={(theme) => ({
                color: "status.waiting",
                fontSize: size.legend,
                borderColor: theme.alpha(theme.vars.palette.status.waiting, 0.35),
                bgcolor: theme.alpha(theme.vars.palette.status.waiting, 0.05),
              })}
            />
          </Stack>
          <Paper
            sx={(theme) => ({
              p: { xs: 1.5, sm: 2, lg: 2.5 },
              borderRadius: 2.5,
              boxShadow: theme.vars.palette.elevation.raised,
            })}
          >
            {waiting.length === 0 ? (
              <EmptyPanel
                title="No one is waiting"
                description="Checked-in patients will appear here."
              />
            ) : (
              <Box
                sx={{
                  display: "grid",
                  gridTemplateColumns: {
                    xs: "1fr",
                    md: "repeat(2, minmax(0, 1fr))",
                    xl: "repeat(3, minmax(0, 1fr))",
                  },
                  "@media (min-width: 2400px)": {
                    gridTemplateColumns: "repeat(4, minmax(0, 1fr))",
                  },
                  gap: { xs: 1.5, lg: 2 },
                }}
              >
                {waiting.map((row) => (
                  <ScheduleCard
                    key={row.id}
                    row={row}
                    prominence="primary"
                    accent={therapistAccents.get(row.therapistId)!}
                  />
                ))}
              </Box>
            )}
          </Paper>
        </Stack>

        <Stack spacing={1.5}>
          <Stack direction="row" spacing={1.25} alignItems="center">
            <EventAvailableRoundedIcon
              sx={{ color: "status.scheduled", fontSize: size.upNextTitle }}
            />
            <Typography component="h2" sx={{ fontSize: size.upNextTitle, fontWeight: 720 }}>
              Up next
            </Typography>
            <Chip
              label={upNext.length}
              variant="outlined"
              size="small"
              sx={(theme) => ({
                color: "status.scheduled",
                fontSize: size.legend,
                borderColor: theme.alpha(theme.vars.palette.status.scheduled, 0.3),
                bgcolor: theme.alpha(theme.vars.palette.status.scheduled, 0.04),
              })}
            />
          </Stack>
          <Paper sx={{ p: { xs: 1.5, sm: 2 }, borderRadius: 2.5 }}>
            {upNext.length === 0 ? (
              <EmptyPanel
                title="No more visits today"
                description="Upcoming patients will appear here."
              />
            ) : (
              <Box
                sx={{
                  display: "grid",
                  gridTemplateColumns: {
                    xs: "1fr",
                    sm: "repeat(2, minmax(0, 1fr))",
                    xl: "repeat(3, minmax(0, 1fr))",
                  },
                  gap: 1.5,
                }}
              >
                {upNext.map((row) => (
                  <ScheduleCard
                    key={row.id}
                    row={row}
                    prominence="secondary"
                    accent={therapistAccents.get(row.therapistId)!}
                  />
                ))}
              </Box>
            )}
          </Paper>
        </Stack>

        {/* Quiet footer: dark mode for the TV, and log out for display accounts. */}
        <Stack direction="row" spacing={1} alignItems="center" justifyContent="flex-end">
          <ColorModeIconButton />
          {currentUser?.roleName === "DISPLAY" && (
            <Button
              variant="text"
              size="small"
              startIcon={<LogoutRoundedIcon fontSize="small" />}
              onClick={() => void logout()}
              sx={(theme) => ({
                color: "text.secondary",
                "&:hover": { bgcolor: theme.alpha(theme.vars.palette.primary.main, 0.05) },
              })}
            >
              Log out
            </Button>
          )}
        </Stack>
      </Stack>
    </Box>
  );
}
