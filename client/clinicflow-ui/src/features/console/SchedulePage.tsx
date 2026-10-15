import { useEffect, useMemo, useRef, useState } from "react";
import AddRoundedIcon from "@mui/icons-material/AddRounded";
import CalendarMonthRoundedIcon from "@mui/icons-material/CalendarMonthRounded";
import ChevronLeftRoundedIcon from "@mui/icons-material/ChevronLeftRounded";
import ChevronRightRoundedIcon from "@mui/icons-material/ChevronRightRounded";
import CloseRoundedIcon from "@mui/icons-material/CloseRounded";
import DeleteOutlineRoundedIcon from "@mui/icons-material/DeleteOutlineRounded";
import EditRoundedIcon from "@mui/icons-material/EditRounded";
import EventAvailableRoundedIcon from "@mui/icons-material/EventAvailableRounded";
import MoreVertRoundedIcon from "@mui/icons-material/MoreVertRounded";
import Alert from "@mui/material/Alert";
import Autocomplete from "@mui/material/Autocomplete";
import Box from "@mui/material/Box";
import Button from "@mui/material/Button";
import Drawer from "@mui/material/Drawer";
import IconButton from "@mui/material/IconButton";
import LinearProgress from "@mui/material/LinearProgress";
import ListItemIcon from "@mui/material/ListItemIcon";
import ListItemText from "@mui/material/ListItemText";
import Menu from "@mui/material/Menu";
import MenuItem from "@mui/material/MenuItem";
import Paper from "@mui/material/Paper";
import Stack from "@mui/material/Stack";
import TextField from "@mui/material/TextField";
import Tooltip from "@mui/material/Tooltip";
import Typography from "@mui/material/Typography";
import { ConfirmDialog } from "../../components/ui/ConfirmDialog";
import { EmptyState } from "../../components/ui/EmptyState";
import { PageHeader } from "../../components/ui/PageHeader";
import { ListSkeleton } from "../../components/ui/Skeletons";
import { StatCard } from "../../components/ui/StatCard";
import { StatusChip } from "../../components/ui/StatusChip";
import { useTherapistAccents } from "../../components/ui/therapistAccents";
import { useToast } from "../../components/ui/useToast";
import {
  useAppointments,
  useCreateAppointment,
  useRemoveAppointment,
  useUpdateAppointment,
  useUpdateAppointmentStatus,
} from "../../queries/appointments";
import { usePatientSearch } from "../../queries/patients";
import { useTherapists } from "../../queries/therapists";
import {
  appointmentStatusLabels,
  appointmentTypeOptions,
  type AppointmentStatus,
  type AppointmentType,
  type BoardRow,
} from "../../types/appointment";
import type { Therapist } from "../../types/therapist";
import {
  formatLongDate,
  formatTime,
  getTodayDateString,
  shiftDate,
  splitName,
} from "./scheduleUtils";

const statuses: AppointmentStatus[] = ["SCHEDULED", "WAITING", "DONE"];

type PatientOption =
  | { kind: "existing"; id: number; firstName: string; lastName: string }
  | { kind: "create"; firstName: string; lastName: string };

function optionLabel(option: PatientOption): string {
  const name = `${option.firstName} ${option.lastName}`.trim();
  return option.kind === "existing" ? name : `Add new patient: "${name}"`;
}

function errorMessage(error: unknown, fallback: string): string {
  return error instanceof Error ? error.message : fallback;
}

// "2026-10-15" -> "Oct 15"
function formatShortDate(date: string): string {
  return new Intl.DateTimeFormat("en-US", { month: "short", day: "numeric" }).format(
    new Date(`${date}T00:00`),
  );
}

function typeLabel(type: AppointmentType): string {
  return appointmentTypeOptions.find((o) => o.value === type)?.label ?? type;
}

// True when a key press is meant for a form field or an open dialog/menu, not a page shortcut.
function isTypingTarget(target: EventTarget | null): boolean {
  return (
    target instanceof HTMLElement &&
    target.closest(
      "input, textarea, select, [contenteditable='true'], [role='dialog'], [role='menu']",
    ) !== null
  );
}

function AppointmentRow({
  row,
  accent,
  disabled,
  onStatus,
  onEdit,
  onCancel,
}: {
  row: BoardRow;
  accent?: string;
  // True while these rows are the previous date's, shown until the new date loads.
  disabled: boolean;
  onStatus: (status: AppointmentStatus) => void;
  onEdit: () => void;
  onCancel: () => void;
}) {
  const [menuAnchor, setMenuAnchor] = useState<HTMLElement | null>(null);
  const done = row.status === "DONE";
  const nextStep =
    row.status === "SCHEDULED"
      ? { label: "Arrived", status: "WAITING" as const, color: "primary" as const }
      : row.status === "WAITING"
        ? { label: "Start visit", status: "DONE" as const, color: "success" as const }
        : null;

  return (
    <Stack
      direction="row"
      alignItems="center"
      flexWrap="wrap"
      columnGap={2}
      rowGap={1}
      sx={{ px: { xs: 2, md: 2.5 }, py: 1.25, "&:hover": { bgcolor: "action.hover" } }}
    >
      <Stack
        direction="row"
        spacing={1.5}
        alignItems="center"
        sx={{ flex: "1 1 220px", minWidth: 0 }}
      >
        <Box
          aria-hidden="true"
          sx={(theme) => ({
            width: 10,
            height: 10,
            flexShrink: 0,
            borderRadius: "50%",
            bgcolor: done ? theme.alpha(accent ?? theme.vars.palette.text.disabled, 0.4) : accent,
          })}
        />
        <Box sx={{ minWidth: 0 }}>
          <Typography fontWeight={700} noWrap color={done ? "text.secondary" : "text.primary"}>
            {row.patientName}
          </Typography>
          <Typography variant="body2" color="text.secondary" noWrap>
            {row.therapistName} · {typeLabel(row.type)}
          </Typography>
        </Box>
      </Stack>
      <Stack direction="row" spacing={1} alignItems="center" sx={{ ml: "auto" }}>
        <StatusChip status={row.status} />
        {/* Fixed width so chips and buttons line up down the list, with or without an action. */}
        <Box sx={{ width: 112, display: "flex", justifyContent: "flex-end" }}>
          {nextStep && (
            <Button
              size="small"
              variant="outlined"
              color={nextStep.color}
              disabled={disabled}
              onClick={() => onStatus(nextStep.status)}
            >
              {nextStep.label}
            </Button>
          )}
        </Box>
        <IconButton
          aria-label={`More actions for ${row.patientName}`}
          size="small"
          disabled={disabled}
          onClick={(event) => setMenuAnchor(event.currentTarget)}
        >
          <MoreVertRoundedIcon fontSize="small" />
        </IconButton>
        <Menu
          anchorEl={menuAnchor}
          open={menuAnchor !== null}
          onClose={() => setMenuAnchor(null)}
          anchorOrigin={{ vertical: "bottom", horizontal: "right" }}
          transformOrigin={{ vertical: "top", horizontal: "right" }}
        >
          <MenuItem
            onClick={() => {
              setMenuAnchor(null);
              onEdit();
            }}
          >
            <ListItemIcon>
              <EditRoundedIcon fontSize="small" />
            </ListItemIcon>
            <ListItemText>Edit appointment</ListItemText>
          </MenuItem>
          <MenuItem
            onClick={() => {
              setMenuAnchor(null);
              onCancel();
            }}
            sx={{ color: "error.main" }}
          >
            <ListItemIcon sx={{ color: "inherit" }}>
              <DeleteOutlineRoundedIcon fontSize="small" />
            </ListItemIcon>
            <ListItemText>Cancel appointment</ListItemText>
          </MenuItem>
        </Menu>
      </Stack>
    </Stack>
  );
}

function DateStepper({ date, onChange }: { date: string; onChange: (date: string) => void }) {
  const pickerRef = useRef<HTMLInputElement>(null);
  const isToday = date === getTodayDateString();

  return (
    <Stack direction="row" alignItems="center" flexWrap="wrap" columnGap={0.5} rowGap={1}>
      <Tooltip title="Previous day">
        <IconButton aria-label="Previous day" onClick={() => onChange(shiftDate(date, -1))}>
          <ChevronLeftRoundedIcon />
        </IconButton>
      </Tooltip>
      <Button
        variant="outlined"
        size="small"
        disabled={isToday}
        onClick={() => onChange(getTodayDateString())}
      >
        Today
      </Button>
      <Tooltip title="Next day">
        <IconButton aria-label="Next day" onClick={() => onChange(shiftDate(date, 1))}>
          <ChevronRightRoundedIcon />
        </IconButton>
      </Tooltip>
      <Typography component="h2" variant="h6" sx={{ mx: 1 }}>
        {formatLongDate(date)}
      </Typography>
      {/* The browser's own date picker, opened from the calendar button. */}
      <Box sx={{ position: "relative" }}>
        <Tooltip title="Pick a date">
          <IconButton
            aria-label="Pick a date"
            onClick={() => {
              const input = pickerRef.current;
              if (!input) return;
              // Older browsers (and jsdom) lack showPicker(); focusing the input is the fallback.
              try {
                input.showPicker();
              } catch {
                input.focus();
              }
            }}
          >
            <CalendarMonthRoundedIcon />
          </IconButton>
        </Tooltip>
        <Box
          component="input"
          ref={pickerRef}
          type="date"
          aria-label="Date"
          tabIndex={-1}
          value={date}
          onChange={(event: React.ChangeEvent<HTMLInputElement>) => {
            if (event.target.value) onChange(event.target.value);
          }}
          sx={{
            position: "absolute",
            inset: 0,
            opacity: 0,
            pointerEvents: "none",
            border: 0,
            width: "100%",
          }}
        />
      </Box>
    </Stack>
  );
}

export function SchedulePage() {
  const [selectedDate, setSelectedDate] = useState(getTodayDateString);
  const [statusFilter, setStatusFilter] = useState<AppointmentStatus | null>(null);
  const toast = useToast();

  // Reload every 60s (and when the tab regains focus) in case another front-desk computer
  // checks someone in.
  const appointments = useAppointments(selectedDate, { refetchInterval: 60_000 });
  const rows = useMemo(
    () => [...(appointments.data ?? [])].sort((a, b) => a.scheduledAt.localeCompare(b.scheduledAt)),
    [appointments.data],
  );
  // The previous date's rows, kept on screen (dimmed, actions disabled) until the new date loads.
  const showingPreviousDate = appointments.isPlaceholderData;
  const therapists = useTherapists().data ?? [];

  const createAppointment = useCreateAppointment();
  const updateStatus = useUpdateAppointmentStatus();
  const removeAppointment = useRemoveAppointment();

  // Quick add
  const [patientInput, setPatientInput] = useState("");
  const [selectedPatient, setSelectedPatient] = useState<PatientOption | null>(null);
  const [therapistId, setTherapistId] = useState("");
  const [time, setTime] = useState("09:00");
  const [type, setType] = useState<AppointmentType>("EVALUATION");
  const patientInputRef = useRef<HTMLInputElement>(null);
  const patientSearch = usePatientSearch(patientInput);

  const [editTarget, setEditTarget] = useState<BoardRow | null>(null);
  const [cancelTarget, setCancelTarget] = useState<BoardRow | null>(null);

  const accents = useTherapistAccents([
    ...therapists.map((t) => t.id),
    ...rows.map((row) => row.therapistId),
  ]);

  // Press N anywhere on the page (outside a field or dialog) to jump to Quick add.
  useEffect(() => {
    function onKeyDown(event: KeyboardEvent) {
      if (event.key.toLowerCase() !== "n") return;
      if (event.metaKey || event.ctrlKey || event.altKey || event.defaultPrevented) return;
      if (isTypingTarget(event.target)) return;
      event.preventDefault();
      patientInputRef.current?.focus();
    }
    window.addEventListener("keydown", onKeyDown);
    return () => window.removeEventListener("keydown", onKeyDown);
  }, []);

  const counts = useMemo(() => {
    const byStatus: Record<AppointmentStatus, number> = { SCHEDULED: 0, WAITING: 0, DONE: 0 };
    for (const row of rows) byStatus[row.status] += 1;
    return byStatus;
  }, [rows]);

  // Visible rows grouped by start time, in order.
  const groups = useMemo(() => {
    const visible = statusFilter ? rows.filter((row) => row.status === statusFilter) : rows;
    const byTime = new Map<string, BoardRow[]>();
    for (const row of visible) {
      byTime.set(row.scheduledAt, [...(byTime.get(row.scheduledAt) ?? []), row]);
    }
    return [...byTime.entries()];
  }, [rows, statusFilter]);

  const patientOptions = useMemo<PatientOption[]>(() => {
    const typed = patientInput.trim();
    if (!typed) return [];
    const matches: PatientOption[] = (patientSearch.data ?? []).map((p) => ({
      kind: "existing",
      id: p.id,
      firstName: p.firstName,
      lastName: p.lastName,
    }));
    const hasExactMatch = matches.some((o) => optionLabel(o).toLowerCase() === typed.toLowerCase());
    if (hasExactMatch) return matches;
    return [...matches, { kind: "create", ...splitName(typed) }];
  }, [patientInput, patientSearch.data]);

  function resetAddForm() {
    setSelectedPatient(null);
    setPatientInput("");
    patientInputRef.current?.focus();
  }

  function handleAdd() {
    const typed = patientInput.trim();
    const patientChoice =
      selectedPatient ??
      (typed ? ({ kind: "create", ...splitName(typed) } as PatientOption) : null);
    if (!patientChoice || !therapistId || !time) {
      toast.error("Patient, therapist and time are required.");
      return;
    }
    createAppointment.mutate(
      {
        therapistId: Number(therapistId),
        scheduledAt: `${selectedDate}T${time}`,
        type,
        ...(patientChoice.kind === "existing"
          ? { patientId: patientChoice.id }
          : { patient: { firstName: patientChoice.firstName, lastName: patientChoice.lastName } }),
      },
      {
        onSuccess: () => {
          resetAddForm();
          toast.success("Appointment added.");
        },
        onError: (error) => toast.error(errorMessage(error, "Unable to add appointment.")),
      },
    );
  }

  // Updates the row instantly; it reverts with an error toast if the server rejects the change.
  function setStatus(row: BoardRow, status: AppointmentStatus) {
    updateStatus.mutate(
      { row, status },
      { onError: (error) => toast.error(errorMessage(error, "Unable to update status.")) },
    );
  }

  function handleCancel() {
    if (!cancelTarget) return;
    removeAppointment.mutate(cancelTarget.id, {
      onSuccess: () => {
        setCancelTarget(null);
        toast.success("Appointment cancelled.");
      },
      onError: (error) => toast.error(errorMessage(error, "Unable to cancel appointment.")),
    });
  }

  function toggleFilter(status: AppointmentStatus) {
    setStatusFilter((current) => (current === status ? null : status));
  }

  // A failed first load shows only the error, not "The day is clear".
  const loadFailed = appointments.isError && !appointments.data;
  const dayIsEmpty = !appointments.isPending && rows.length === 0;

  function countLabel(): string {
    if (appointments.isPending) return "Loading appointments…";
    if (showingPreviousDate) return `Loading ${formatShortDate(selectedDate)}…`;
    return `${rows.length} appointment${rows.length === 1 ? "" : "s"}`;
  }

  function renderList() {
    if (loadFailed) return null;
    if (appointments.isPending) return <ListSkeleton rows={5} />;
    if (dayIsEmpty) {
      return (
        <EmptyState
          icon={<EventAvailableRoundedIcon sx={{ fontSize: 42 }} />}
          title="The day is clear"
          description="Use Quick add to schedule the first appointment."
        />
      );
    }
    if (groups.length === 0 && statusFilter) {
      return (
        <EmptyState
          title={`No ${appointmentStatusLabels[statusFilter].toLowerCase()} visits`}
          description="Nobody on this day is in that state right now."
          action={<Button onClick={() => setStatusFilter(null)}>Show all visits</Button>}
        />
      );
    }
    return groups.map(([scheduledAt, groupRows]) => (
      <Box
        key={scheduledAt}
        component="section"
        aria-label={formatTime(scheduledAt)}
        sx={{ "& + &": { borderTop: 1, borderColor: "divider" } }}
      >
        <Typography
          variant="body2"
          fontWeight={700}
          color="text.secondary"
          sx={{ px: { xs: 2, md: 2.5 }, pt: 1.5, pb: 0.25 }}
        >
          {formatTime(scheduledAt)}
        </Typography>
        {groupRows.map((row) => (
          <AppointmentRow
            key={row.id}
            row={row}
            accent={accents.get(row.therapistId)}
            disabled={showingPreviousDate}
            onStatus={(status) => setStatus(row, status)}
            onEdit={() => setEditTarget(row)}
            onCancel={() => setCancelTarget(row)}
          />
        ))}
      </Box>
    ));
  }

  return (
    <Stack spacing={3}>
      <PageHeader
        eyebrow="Daily operations"
        title="Schedule"
        description="Add patients to the day and move each visit from scheduled to waiting to complete."
        onRefresh={() => void appointments.refetch()}
        refreshing={appointments.isFetching}
      />

      {/* Day summary; each status card also filters the list. */}
      <Box
        sx={{
          display: "grid",
          gridTemplateColumns: { xs: "repeat(2, minmax(0, 1fr))", md: "repeat(4, minmax(0, 1fr))" },
          gap: 1.5,
          opacity: showingPreviousDate ? 0.5 : 1,
          transition: "opacity 160ms ease",
        }}
      >
        <StatCard
          label="Total"
          value={rows.length}
          selected={statusFilter === null}
          onClick={() => setStatusFilter(null)}
        />
        {statuses.map((status) => (
          <StatCard
            key={status}
            label={appointmentStatusLabels[status]}
            value={counts[status]}
            tone={status.toLowerCase() as Lowercase<AppointmentStatus>}
            selected={statusFilter === status}
            onClick={() => toggleFilter(status)}
          />
        ))}
      </Box>

      <Paper
        component="section"
        aria-label="Day schedule"
        sx={(theme) => ({ overflow: "hidden", boxShadow: theme.vars.palette.elevation.card })}
      >
        <Stack
          direction={{ xs: "column", sm: "row" }}
          alignItems={{ sm: "center" }}
          justifyContent="space-between"
          spacing={1}
          sx={{ px: { xs: 1, md: 1.5 }, py: 1.25 }}
        >
          <DateStepper date={selectedDate} onChange={setSelectedDate} />
          <Typography variant="body2" color="text.secondary" sx={{ px: 1 }}>
            {countLabel()}
          </Typography>
        </Stack>

        {/* Quick add: compact bar, adds to the day shown. Press N to focus, Enter to add. */}
        <Box
          sx={{
            px: { xs: 2, md: 2.5 },
            py: 2,
            bgcolor: "background.subtle",
            borderTop: 1,
            borderBottom: 1,
            borderColor: "divider",
          }}
        >
          <Stack direction="row" spacing={1} alignItems="center" sx={{ mb: 1.5 }}>
            <Typography variant="body2" fontWeight={700}>
              Quick add
            </Typography>
            {/* Keyboard hint; hidden on phones, which have no hardware keyboard. */}
            <Typography
              variant="caption"
              color="text.secondary"
              sx={{ display: { xs: "none", md: "inline" } }}
            >
              Press{" "}
              <Box
                component="kbd"
                sx={{
                  px: 0.75,
                  py: 0.125,
                  border: 1,
                  borderColor: "divider",
                  borderRadius: 1,
                  bgcolor: "background.paper",
                  fontFamily: "inherit",
                  fontWeight: 700,
                }}
              >
                N
              </Box>{" "}
              to jump here
            </Typography>
          </Stack>
          <Stack
            direction={{ xs: "column", md: "row" }}
            spacing={1.5}
            alignItems={{ md: "center" }}
            component="form"
            aria-label="Quick add"
            onSubmit={(e) => {
              e.preventDefault();
              handleAdd();
            }}
          >
            <Autocomplete
              sx={{ flex: 1, minWidth: 200 }}
              options={patientOptions}
              value={selectedPatient}
              inputValue={patientInput}
              onInputChange={(_, value) => setPatientInput(value)}
              onChange={(_, value) => setSelectedPatient(value)}
              getOptionLabel={optionLabel}
              isOptionEqualToValue={(a, b) => optionLabel(a) === optionLabel(b)}
              filterOptions={(opts) => opts}
              // Keep a typed new-patient name when moving on to the next field (it's added on
              // submit), and highlight the first match so Enter picks it.
              clearOnBlur={false}
              autoHighlight
              renderInput={(params) => (
                <TextField
                  {...params}
                  inputRef={patientInputRef}
                  label="Patient"
                  required={!patientInput}
                />
              )}
            />
            <TextField
              select
              label="Therapist"
              value={therapistId}
              onChange={(e) => setTherapistId(e.target.value)}
              required
              sx={{ minWidth: 170 }}
            >
              {therapists.map((t) => (
                <MenuItem key={t.id} value={String(t.id)}>
                  {t.name}
                </MenuItem>
              ))}
            </TextField>
            <TextField
              label="Time"
              type="time"
              value={time}
              onChange={(e) => setTime(e.target.value)}
              required
              sx={{ minWidth: 130 }}
              InputLabelProps={{ shrink: true }}
            />
            <TextField
              select
              label="Type"
              value={type}
              onChange={(e) => setType(e.target.value as AppointmentType)}
              sx={{ minWidth: 150 }}
            >
              {appointmentTypeOptions.map((o) => (
                <MenuItem key={o.value} value={o.value}>
                  {o.label}
                </MenuItem>
              ))}
            </TextField>
            <Button
              type="submit"
              variant="contained"
              disabled={createAppointment.isPending}
              startIcon={<AddRoundedIcon />}
              sx={{ minWidth: 112 }}
            >
              Add visit
            </Button>
          </Stack>
        </Box>

        {showingPreviousDate && (
          <LinearProgress aria-label={`Loading ${formatShortDate(selectedDate)}`} />
        )}
        {appointments.isError && (
          <Alert severity="error" sx={{ borderRadius: 0, border: 0 }}>
            {errorMessage(appointments.error, "Unable to load the schedule.")}
          </Alert>
        )}
        <Box
          aria-busy={showingPreviousDate}
          sx={{
            pb: 1,
            opacity: showingPreviousDate ? 0.5 : 1,
            transition: "opacity 160ms ease",
          }}
        >
          {renderList()}
        </Box>
      </Paper>

      {editTarget && (
        <EditAppointmentDrawer
          row={editTarget}
          therapists={therapists}
          onClose={() => setEditTarget(null)}
          onSaved={() => {
            setEditTarget(null);
            toast.success("Appointment updated.");
          }}
          onError={toast.error}
        />
      )}

      <ConfirmDialog
        open={cancelTarget !== null}
        title="Cancel appointment"
        confirmLabel="Cancel appointment"
        cancelLabel="Keep appointment"
        tone="danger"
        loading={removeAppointment.isPending}
        description={
          cancelTarget
            ? `Cancel ${cancelTarget.patientName}'s ${formatTime(cancelTarget.scheduledAt)} appointment?`
            : ""
        }
        onClose={() => setCancelTarget(null)}
        onConfirm={handleCancel}
      />
    </Stack>
  );
}

function EditAppointmentDrawer({
  row,
  therapists,
  onClose,
  onSaved,
  onError,
}: {
  row: BoardRow;
  therapists: Therapist[];
  onClose: () => void;
  onSaved: () => void;
  onError: (message: string) => void;
}) {
  const [scheduledAt, setScheduledAt] = useState(row.scheduledAt.slice(0, 16));
  const [therapistId, setTherapistId] = useState(String(row.therapistId));
  const [type, setType] = useState<AppointmentType>(row.type);
  const updateAppointment = useUpdateAppointment();
  const saving = updateAppointment.isPending;

  function handleSave() {
    updateAppointment.mutate(
      { id: row.id, patch: { scheduledAt, therapistId: Number(therapistId), type } },
      {
        onSuccess: onSaved,
        onError: (error) => onError(errorMessage(error, "Unable to update appointment.")),
      },
    );
  }

  return (
    <Drawer
      anchor="right"
      open
      onClose={saving ? undefined : onClose}
      PaperProps={{
        role: "dialog",
        "aria-labelledby": "edit-appointment-title",
        sx: { width: { xs: "100%", sm: 400 }, borderLeft: 1, borderColor: "divider" },
      }}
    >
      <Stack
        component="form"
        sx={{ height: "100%" }}
        onSubmit={(e) => {
          e.preventDefault();
          handleSave();
        }}
      >
        <Stack
          direction="row"
          alignItems="flex-start"
          justifyContent="space-between"
          sx={{ px: 3, py: 2.5, borderBottom: 1, borderColor: "divider" }}
        >
          <Box>
            <Typography id="edit-appointment-title" variant="h6" component="h2">
              Edit appointment
            </Typography>
            <Typography variant="body2" color="text.secondary">
              {row.patientName}
            </Typography>
          </Box>
          <IconButton aria-label="Close" onClick={onClose} disabled={saving} edge="end">
            <CloseRoundedIcon />
          </IconButton>
        </Stack>
        <Stack spacing={2.5} sx={{ p: 3, flexGrow: 1, overflowY: "auto" }}>
          <TextField
            label="Scheduled at"
            type="datetime-local"
            value={scheduledAt}
            onChange={(e) => setScheduledAt(e.target.value)}
            InputLabelProps={{ shrink: true }}
          />
          <TextField
            select
            label="Therapist"
            value={therapistId}
            onChange={(e) => setTherapistId(e.target.value)}
          >
            {therapists.map((t) => (
              <MenuItem key={t.id} value={String(t.id)}>
                {t.name}
              </MenuItem>
            ))}
          </TextField>
          <TextField
            select
            label="Type"
            value={type}
            onChange={(e) => setType(e.target.value as AppointmentType)}
          >
            {appointmentTypeOptions.map((o) => (
              <MenuItem key={o.value} value={o.value}>
                {o.label}
              </MenuItem>
            ))}
          </TextField>
        </Stack>
        <Stack
          direction="row"
          spacing={1}
          justifyContent="flex-end"
          sx={{ px: 3, py: 2, borderTop: 1, borderColor: "divider" }}
        >
          <Button onClick={onClose} disabled={saving}>
            Cancel
          </Button>
          <Button type="submit" variant="contained" disabled={saving}>
            Save
          </Button>
        </Stack>
      </Stack>
    </Drawer>
  );
}
