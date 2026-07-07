import { useEffect, useMemo, useRef, useState } from "react";
import AddRoundedIcon from "@mui/icons-material/AddRounded";
import CalendarTodayRoundedIcon from "@mui/icons-material/CalendarTodayRounded";
import DeleteOutlineRoundedIcon from "@mui/icons-material/DeleteOutlineRounded";
import EditRoundedIcon from "@mui/icons-material/EditRounded";
import EventAvailableRoundedIcon from "@mui/icons-material/EventAvailableRounded";
import Alert from "@mui/material/Alert";
import Autocomplete from "@mui/material/Autocomplete";
import Box from "@mui/material/Box";
import Button from "@mui/material/Button";
import Chip from "@mui/material/Chip";
import Dialog from "@mui/material/Dialog";
import DialogActions from "@mui/material/DialogActions";
import DialogContent from "@mui/material/DialogContent";
import DialogTitle from "@mui/material/DialogTitle";
import IconButton from "@mui/material/IconButton";
import MenuItem from "@mui/material/MenuItem";
import Paper from "@mui/material/Paper";
import Snackbar from "@mui/material/Snackbar";
import Stack from "@mui/material/Stack";
import Table from "@mui/material/Table";
import TableBody from "@mui/material/TableBody";
import TableCell from "@mui/material/TableCell";
import TableHead from "@mui/material/TableHead";
import TableRow from "@mui/material/TableRow";
import TableContainer from "@mui/material/TableContainer";
import TextField from "@mui/material/TextField";
import Tooltip from "@mui/material/Tooltip";
import Typography from "@mui/material/Typography";
import { alpha } from "@mui/material/styles";
import {
  adminColors,
  adminSelectProps,
  adminTextFieldSx,
} from "../../components/admin/adminStyles";
import { ConfirmDeleteDialog } from "../../components/admin/ConfirmDeleteDialog";
import { EmptyState } from "../../components/ui/EmptyState";
import { PageHeader } from "../../components/ui/PageHeader";
import { colors } from "../../theme";
import {
  createAppointment,
  listAppointmentsByDate,
  removeAppointment,
  updateAppointment,
} from "../../services/appointmentService";
import { searchPatients } from "../../services/patientService";
import { listTherapists } from "../../services/therapistService";
import {
  appointmentStatusLabels,
  appointmentTypeOptions,
  type AppointmentType,
  type BoardRow,
} from "../../types/appointment";
import type { Therapist } from "../../types/therapist";
import { formatTime, getTodayDateString, splitName, statusChipColor } from "./scheduleUtils";

type SnackbarState = { open: boolean; severity: "success" | "error"; message: string };
type PatientOption =
  | { kind: "existing"; id: number; firstName: string; lastName: string }
  | { kind: "create"; firstName: string; lastName: string };

function optionLabel(option: PatientOption): string {
  const name = `${option.firstName} ${option.lastName}`.trim();
  return option.kind === "existing" ? name : `Add new patient: "${name}"`;
}

function AppointmentActions({
  row,
  onStatus,
  onEdit,
  onDelete,
}: {
  row: BoardRow;
  onStatus: (status: BoardRow["status"]) => void;
  onEdit: () => void;
  onDelete: () => void;
}) {
  return (
    <Stack direction="row" spacing={0.75} alignItems="center" flexWrap="wrap" useFlexGap>
      {row.status === "SCHEDULED" && (
        <Button size="small" variant="outlined" color="primary" onClick={() => onStatus("WAITING")}>
          Arrived
        </Button>
      )}
      {row.status === "WAITING" && (
        <Button size="small" variant="outlined" color="success" onClick={() => onStatus("DONE")}>
          Start visit
        </Button>
      )}
      <Tooltip title="Edit appointment">
        <IconButton
          aria-label={`Edit ${row.patientName}'s appointment`}
          size="small"
          onClick={onEdit}
        >
          <EditRoundedIcon fontSize="small" />
        </IconButton>
      </Tooltip>
      <Tooltip title="Cancel appointment">
        <IconButton
          aria-label={`Cancel ${row.patientName}'s appointment`}
          size="small"
          color="error"
          onClick={onDelete}
        >
          <DeleteOutlineRoundedIcon fontSize="small" />
        </IconButton>
      </Tooltip>
    </Stack>
  );
}

export function SchedulePage() {
  const [selectedDate, setSelectedDate] = useState(getTodayDateString);
  const [rows, setRows] = useState<BoardRow[]>([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);
  const [therapists, setTherapists] = useState<Therapist[]>([]);
  const [snackbar, setSnackbar] = useState<SnackbarState>({
    open: false,
    severity: "success",
    message: "",
  });

  // Fast-add form
  const [patientInput, setPatientInput] = useState("");
  const [selectedPatient, setSelectedPatient] = useState<PatientOption | null>(null);
  const [patientMatches, setPatientMatches] = useState<PatientOption[]>([]);
  const [therapistId, setTherapistId] = useState("");
  const [time, setTime] = useState("09:00");
  const [type, setType] = useState<AppointmentType>("EVALUATION");
  const [adding, setAdding] = useState(false);
  const patientInputRef = useRef<HTMLInputElement>(null);

  const [editTarget, setEditTarget] = useState<BoardRow | null>(null);
  const [deleteTarget, setDeleteTarget] = useState<BoardRow | null>(null);

  async function refresh(date = selectedDate) {
    try {
      setLoading(true);
      setError(null);
      setRows(await listAppointmentsByDate(date));
    } catch (loadError) {
      setError(loadError instanceof Error ? loadError.message : "Unable to load the schedule.");
    } finally {
      setLoading(false);
    }
  }

  useEffect(() => {
    void refresh(selectedDate);
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [selectedDate]);

  useEffect(() => {
    listTherapists()
      .then(setTherapists)
      .catch(() => setTherapists([]));
  }, []);

  // Debounced patient typeahead.
  useEffect(() => {
    const query = patientInput.trim();
    if (!query) {
      setPatientMatches([]);
      return;
    }
    const id = window.setTimeout(() => {
      searchPatients(query)
        .then((found) =>
          setPatientMatches(
            found.map((p) => ({
              kind: "existing",
              id: p.id,
              firstName: p.firstName,
              lastName: p.lastName,
            })),
          ),
        )
        .catch(() => setPatientMatches([]));
    }, 200);
    return () => window.clearTimeout(id);
  }, [patientInput]);

  const patientOptions = useMemo<PatientOption[]>(() => {
    const typed = patientInput.trim();
    const hasExactMatch = patientMatches.some(
      (o) => optionLabel(o).toLowerCase() === typed.toLowerCase(),
    );
    if (!typed || hasExactMatch) return patientMatches;
    const { firstName, lastName } = splitName(typed);
    return [...patientMatches, { kind: "create", firstName, lastName }];
  }, [patientInput, patientMatches]);

  function resetAddForm() {
    setSelectedPatient(null);
    setPatientInput("");
    setPatientMatches([]);
    patientInputRef.current?.focus();
  }

  async function handleAdd() {
    const typed = patientInput.trim();
    const patientChoice =
      selectedPatient ??
      (typed ? ({ kind: "create", ...splitName(typed) } as PatientOption) : null);
    if (!patientChoice || !therapistId || !time) {
      setSnackbar({
        open: true,
        severity: "error",
        message: "Patient, therapist and time are required.",
      });
      return;
    }
    setAdding(true);
    try {
      await createAppointment({
        therapistId: Number(therapistId),
        scheduledAt: `${selectedDate}T${time}`,
        type,
        ...(patientChoice.kind === "existing"
          ? { patientId: patientChoice.id }
          : { patient: { firstName: patientChoice.firstName, lastName: patientChoice.lastName } }),
      });
      await refresh();
      resetAddForm();
      setSnackbar({ open: true, severity: "success", message: "Appointment added." });
    } catch (addError) {
      const message = addError instanceof Error ? addError.message : "Unable to add appointment.";
      setSnackbar({ open: true, severity: "error", message });
    } finally {
      setAdding(false);
    }
  }

  async function setStatus(row: BoardRow, status: BoardRow["status"]) {
    try {
      await updateAppointment(row.id, { status });
      await refresh();
    } catch (statusError) {
      const message =
        statusError instanceof Error ? statusError.message : "Unable to update status.";
      setSnackbar({ open: true, severity: "error", message });
    }
  }

  async function handleDelete() {
    if (!deleteTarget) return;
    try {
      await removeAppointment(deleteTarget.id);
      await refresh();
      setDeleteTarget(null);
      setSnackbar({ open: true, severity: "success", message: "Appointment cancelled." });
    } catch (deleteError) {
      const message =
        deleteError instanceof Error ? deleteError.message : "Unable to cancel appointment.";
      setSnackbar({ open: true, severity: "error", message });
    }
  }

  return (
    <Stack spacing={3.5}>
      <PageHeader
        eyebrow="Daily operations"
        title="Schedule"
        description="Add patients to the day and move each visit from scheduled to waiting to complete."
      />

      {/* Fast-add row */}
      <Paper
        sx={{
          p: { xs: 2, md: 3 },
          bgcolor: colors.surface,
          boxShadow: "0 18px 50px rgba(0,0,0,.18)",
        }}
      >
        <Stack direction="row" spacing={1.25} alignItems="center" sx={{ mb: 2.5 }}>
          <Box
            sx={{
              width: 36,
              height: 36,
              borderRadius: 2,
              display: "grid",
              placeItems: "center",
              color: "primary.main",
              bgcolor: alpha(colors.primary, 0.11),
            }}
          >
            <AddRoundedIcon />
          </Box>
          <Box>
            <Typography variant="h6">Quick add</Typography>
            <Typography variant="body2" color="text.secondary">
              Create an appointment without leaving the schedule.
            </Typography>
          </Box>
        </Stack>
        <Stack
          direction={{ xs: "column", lg: "row" }}
          spacing={2}
          alignItems={{ lg: "center" }}
          component="form"
          onSubmit={(e) => {
            e.preventDefault();
            void handleAdd();
          }}
        >
          <Autocomplete
            sx={{ flex: 1, minWidth: 220 }}
            options={patientOptions}
            value={selectedPatient}
            inputValue={patientInput}
            onInputChange={(_, value) => setPatientInput(value)}
            onChange={(_, value) => setSelectedPatient(value)}
            getOptionLabel={optionLabel}
            isOptionEqualToValue={(a, b) => optionLabel(a) === optionLabel(b)}
            filterOptions={(opts) => opts}
            renderInput={(params) => (
              <TextField
                {...params}
                inputRef={patientInputRef}
                label="Patient"
                required={!patientInput}
                sx={adminTextFieldSx}
              />
            )}
            slotProps={{
              paper: { sx: { bgcolor: adminColors.panelElevated, color: adminColors.textStrong } },
            }}
          />
          <TextField
            select
            label="Therapist"
            value={therapistId}
            onChange={(e) => setTherapistId(e.target.value)}
            required
            sx={{ minWidth: 180, ...adminTextFieldSx }}
            SelectProps={adminSelectProps}
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
            sx={{ minWidth: 130, ...adminTextFieldSx }}
            InputLabelProps={{ shrink: true }}
          />
          <TextField
            select
            label="Type"
            value={type}
            onChange={(e) => setType(e.target.value as AppointmentType)}
            sx={{ minWidth: 160, ...adminTextFieldSx }}
            SelectProps={adminSelectProps}
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
            disabled={adding}
            startIcon={<AddRoundedIcon />}
            sx={{ minWidth: 118 }}
          >
            Add visit
          </Button>
        </Stack>
      </Paper>

      {/* Schedule table */}
      <Paper sx={{ overflow: "hidden", bgcolor: colors.surface }}>
        <Stack
          direction={{ xs: "column", sm: "row" }}
          spacing={2}
          alignItems={{ sm: "center" }}
          justifyContent="space-between"
          sx={{ p: { xs: 2, md: 2.5 }, borderBottom: `1px solid ${colors.borderSoft}` }}
        >
          <Box>
            <Stack direction="row" spacing={1} alignItems="center">
              <CalendarTodayRoundedIcon color="primary" fontSize="small" />
              <Typography variant="h6">Day schedule</Typography>
            </Stack>
            <Typography variant="body2" color="text.secondary" sx={{ mt: 0.5 }}>
              {loading
                ? "Loading appointments…"
                : `${rows.length} appointment${rows.length === 1 ? "" : "s"}`}
            </Typography>
          </Box>
          <TextField
            label="Date"
            type="date"
            value={selectedDate}
            onChange={(e) => setSelectedDate(e.target.value)}
            sx={{ width: { xs: "100%", sm: 210 }, ...adminTextFieldSx }}
            InputLabelProps={{ shrink: true }}
          />
        </Stack>
        <Stack>
          {error && <Alert severity="error">{error}</Alert>}
          {rows.length === 0 && !loading ? (
            <EmptyState
              icon={<EventAvailableRoundedIcon sx={{ fontSize: 42 }} />}
              title="The day is clear"
              description="Use Quick add to schedule the first appointment."
            />
          ) : (
            <>
              <TableContainer sx={{ display: { xs: "none", md: "block" } }}>
                <Table
                  sx={{ minWidth: 900, "& td, & th": { borderColor: adminColors.borderMuted } }}
                >
                  <TableHead>
                    <TableRow sx={{ bgcolor: alpha(colors.surfaceRaised, 0.55) }}>
                      {["Time", "Patient", "Therapist", "Type", "Status", "Actions"].map((h) => (
                        <TableCell
                          key={h}
                          sx={{
                            color: "text.secondary",
                            fontSize: "0.75rem",
                            textTransform: "uppercase",
                            letterSpacing: ".08em",
                            fontWeight: 750,
                          }}
                        >
                          {h}
                        </TableCell>
                      ))}
                    </TableRow>
                  </TableHead>
                  <TableBody>
                    {rows.map((row) => (
                      <TableRow key={row.id} hover sx={{ "&:last-child td": { borderBottom: 0 } }}>
                        <TableCell sx={{ fontWeight: 750, color: "primary.light" }}>
                          {formatTime(row.scheduledAt)}
                        </TableCell>
                        <TableCell>
                          <Typography fontWeight={700}>{row.patientName}</Typography>
                        </TableCell>
                        <TableCell color="text.secondary">{row.therapistName}</TableCell>
                        <TableCell>
                          {appointmentTypeOptions.find((o) => o.value === row.type)?.label ??
                            row.type}
                        </TableCell>
                        <TableCell>
                          <Chip
                            size="small"
                            label={appointmentStatusLabels[row.status]}
                            color={statusChipColor[row.status]}
                            variant={row.status === "SCHEDULED" ? "outlined" : "filled"}
                          />
                        </TableCell>
                        <TableCell>
                          <AppointmentActions
                            row={row}
                            onStatus={(status) => void setStatus(row, status)}
                            onEdit={() => setEditTarget(row)}
                            onDelete={() => setDeleteTarget(row)}
                          />
                        </TableCell>
                      </TableRow>
                    ))}
                  </TableBody>
                </Table>
              </TableContainer>

              <Stack spacing={1.5} sx={{ display: { xs: "flex", md: "none" }, p: 2 }}>
                {rows.map((row) => (
                  <Paper
                    key={row.id}
                    sx={{
                      p: 2,
                      bgcolor: alpha(colors.surfaceRaised, 0.52),
                      borderColor: colors.borderSoft,
                    }}
                  >
                    <Stack
                      direction="row"
                      spacing={2}
                      justifyContent="space-between"
                      alignItems="flex-start"
                    >
                      <Box>
                        <Typography color="primary.light" fontWeight={750}>
                          {formatTime(row.scheduledAt)}
                        </Typography>
                        <Typography variant="h6" sx={{ mt: 0.25 }}>
                          {row.patientName}
                        </Typography>
                        <Typography variant="body2" color="text.secondary" sx={{ mt: 0.5 }}>
                          {row.therapistName} ·{" "}
                          {appointmentTypeOptions.find((o) => o.value === row.type)?.label ??
                            row.type}
                        </Typography>
                      </Box>
                      <Chip
                        size="small"
                        label={appointmentStatusLabels[row.status]}
                        color={statusChipColor[row.status]}
                        variant={row.status === "SCHEDULED" ? "outlined" : "filled"}
                      />
                    </Stack>
                    <Box sx={{ mt: 2, pt: 1.5, borderTop: `1px solid ${colors.borderSoft}` }}>
                      <AppointmentActions
                        row={row}
                        onStatus={(status) => void setStatus(row, status)}
                        onEdit={() => setEditTarget(row)}
                        onDelete={() => setDeleteTarget(row)}
                      />
                    </Box>
                  </Paper>
                ))}
              </Stack>
            </>
          )}
        </Stack>
      </Paper>

      {editTarget && (
        <EditAppointmentDialog
          row={editTarget}
          therapists={therapists}
          onClose={() => setEditTarget(null)}
          onSaved={async () => {
            setEditTarget(null);
            await refresh();
            setSnackbar({ open: true, severity: "success", message: "Appointment updated." });
          }}
          onError={(message) => setSnackbar({ open: true, severity: "error", message })}
        />
      )}

      <ConfirmDeleteDialog
        open={deleteTarget !== null}
        title="Cancel appointment"
        confirmLabel="Cancel appointment"
        description={
          deleteTarget
            ? `Cancel ${deleteTarget.patientName}'s ${formatTime(deleteTarget.scheduledAt)} appointment?`
            : ""
        }
        onClose={() => setDeleteTarget(null)}
        onConfirm={() => void handleDelete()}
      />

      <Snackbar
        open={snackbar.open}
        autoHideDuration={4000}
        onClose={() => setSnackbar((c) => ({ ...c, open: false }))}
        anchorOrigin={{ vertical: "bottom", horizontal: "right" }}
      >
        <Alert
          onClose={() => setSnackbar((c) => ({ ...c, open: false }))}
          severity={snackbar.severity}
          variant="filled"
        >
          {snackbar.message}
        </Alert>
      </Snackbar>
    </Stack>
  );
}

function EditAppointmentDialog({
  row,
  therapists,
  onClose,
  onSaved,
  onError,
}: {
  row: BoardRow;
  therapists: Therapist[];
  onClose: () => void;
  onSaved: () => Promise<void>;
  onError: (message: string) => void;
}) {
  const [scheduledAt, setScheduledAt] = useState(row.scheduledAt.slice(0, 16));
  const [therapistId, setTherapistId] = useState(String(row.therapistId));
  const [type, setType] = useState<AppointmentType>(row.type);
  const [saving, setSaving] = useState(false);

  async function handleSave() {
    setSaving(true);
    try {
      await updateAppointment(row.id, { scheduledAt, therapistId: Number(therapistId), type });
      await onSaved();
    } catch (saveError) {
      onError(saveError instanceof Error ? saveError.message : "Unable to update appointment.");
    } finally {
      setSaving(false);
    }
  }

  return (
    <Dialog
      open
      onClose={saving ? undefined : onClose}
      fullWidth
      maxWidth="xs"
      PaperProps={{
        sx: {
          bgcolor: adminColors.panelElevated,
          color: adminColors.textStrong,
          border: `1px solid ${adminColors.border}`,
          backgroundImage: "none",
        },
      }}
    >
      <DialogTitle sx={{ color: adminColors.textStrong }}>Edit Appointment</DialogTitle>
      <DialogContent>
        <Stack spacing={2.5} sx={{ pt: 1 }}>
          <TextField
            label="Scheduled At"
            type="datetime-local"
            value={scheduledAt}
            onChange={(e) => setScheduledAt(e.target.value)}
            sx={adminTextFieldSx}
            InputLabelProps={{ shrink: true }}
          />
          <TextField
            select
            label="Therapist"
            value={therapistId}
            onChange={(e) => setTherapistId(e.target.value)}
            sx={adminTextFieldSx}
            SelectProps={adminSelectProps}
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
            sx={adminTextFieldSx}
            SelectProps={adminSelectProps}
          >
            {appointmentTypeOptions.map((o) => (
              <MenuItem key={o.value} value={o.value}>
                {o.label}
              </MenuItem>
            ))}
          </TextField>
        </Stack>
      </DialogContent>
      <DialogActions sx={{ px: 3, pb: 2.5 }}>
        <Button onClick={onClose} disabled={saving}>
          Cancel
        </Button>
        <Button variant="contained" onClick={() => void handleSave()} disabled={saving}>
          Save
        </Button>
      </DialogActions>
    </Dialog>
  );
}
