import { useEffect, useMemo, useRef, useState } from "react";
import Alert from "@mui/material/Alert";
import Autocomplete from "@mui/material/Autocomplete";
import Box from "@mui/material/Box";
import Button from "@mui/material/Button";
import Chip from "@mui/material/Chip";
import Dialog from "@mui/material/Dialog";
import DialogActions from "@mui/material/DialogActions";
import DialogContent from "@mui/material/DialogContent";
import DialogTitle from "@mui/material/DialogTitle";
import MenuItem from "@mui/material/MenuItem";
import Paper from "@mui/material/Paper";
import Snackbar from "@mui/material/Snackbar";
import Stack from "@mui/material/Stack";
import Table from "@mui/material/Table";
import TableBody from "@mui/material/TableBody";
import TableCell from "@mui/material/TableCell";
import TableHead from "@mui/material/TableHead";
import TableRow from "@mui/material/TableRow";
import TextField from "@mui/material/TextField";
import Typography from "@mui/material/Typography";
import { adminColors, adminSelectProps, adminTextFieldSx } from "../../components/admin/adminStyles";
import { ConfirmDeleteDialog } from "../../components/admin/ConfirmDeleteDialog";
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

export function SchedulePage() {
  const [selectedDate, setSelectedDate] = useState(getTodayDateString);
  const [rows, setRows] = useState<BoardRow[]>([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);
  const [therapists, setTherapists] = useState<Therapist[]>([]);
  const [snackbar, setSnackbar] = useState<SnackbarState>({ open: false, severity: "success", message: "" });

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
    listTherapists().then(setTherapists).catch(() => setTherapists([]));
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
            found.map((p) => ({ kind: "existing", id: p.id, firstName: p.firstName, lastName: p.lastName })),
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
      selectedPatient ?? (typed ? ({ kind: "create", ...splitName(typed) } as PatientOption) : null);
    if (!patientChoice || !therapistId || !time) {
      setSnackbar({ open: true, severity: "error", message: "Patient, therapist and time are required." });
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
      const message = statusError instanceof Error ? statusError.message : "Unable to update status.";
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
      const message = deleteError instanceof Error ? deleteError.message : "Unable to cancel appointment.";
      setSnackbar({ open: true, severity: "error", message });
    }
  }

  return (
    <Stack spacing={3}>
      <Box>
        <Typography variant="h4" sx={{ color: "#f8fafc", fontWeight: 700 }}>
          Schedule
        </Typography>
        <Typography sx={{ color: adminColors.textSecondary, mt: 1 }}>
          Add patients to the day and check them in as they arrive.
        </Typography>
      </Box>

      {/* Fast-add row */}
      <Paper elevation={0} sx={{ p: 2.5, borderRadius: 4, bgcolor: adminColors.panelBg, border: `1px solid ${adminColors.border}` }}>
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
              <TextField {...params} inputRef={patientInputRef} label="Patient" required={!patientInput} sx={adminTextFieldSx} />
            )}
            slotProps={{ paper: { sx: { bgcolor: adminColors.panelElevated, color: adminColors.textStrong } } }}
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
              <MenuItem key={t.id} value={String(t.id)}>{t.name}</MenuItem>
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
              <MenuItem key={o.value} value={o.value}>{o.label}</MenuItem>
            ))}
          </TextField>
          <Button type="submit" variant="contained" disabled={adding} sx={{ height: 40 }}>
            Add
          </Button>
        </Stack>
      </Paper>

      {/* Schedule table */}
      <Paper elevation={0} sx={{ p: 2, borderRadius: 4, bgcolor: adminColors.panelBg, border: `1px solid ${adminColors.border}` }}>
        <Stack spacing={2}>
          <TextField
            label="Date"
            type="date"
            value={selectedDate}
            onChange={(e) => setSelectedDate(e.target.value)}
            sx={{ maxWidth: 220, ...adminTextFieldSx }}
            InputLabelProps={{ shrink: true }}
          />
          {error && <Alert severity="error">{error}</Alert>}
          <Table size="small" sx={{ "& td, & th": { borderColor: adminColors.borderMuted, color: adminColors.textStrong } }}>
            <TableHead>
              <TableRow>
                {["Time", "Patient", "Therapist", "Type", "Status", "Actions"].map((h) => (
                  <TableCell key={h} sx={{ color: "#f8fafc", fontWeight: 700 }}>{h}</TableCell>
                ))}
              </TableRow>
            </TableHead>
            <TableBody>
              {rows.length === 0 && !loading && (
                <TableRow>
                  <TableCell colSpan={6} sx={{ color: adminColors.textSecondary, textAlign: "center", py: 4 }}>
                    No appointments for this day.
                  </TableCell>
                </TableRow>
              )}
              {rows.map((row) => (
                <TableRow key={row.id} hover>
                  <TableCell sx={{ fontWeight: 600 }}>{formatTime(row.scheduledAt)}</TableCell>
                  <TableCell>{row.patientName}</TableCell>
                  <TableCell>{row.therapistName}</TableCell>
                  <TableCell>{appointmentTypeOptions.find((o) => o.value === row.type)?.label ?? row.type}</TableCell>
                  <TableCell>
                    <Chip size="small" label={appointmentStatusLabels[row.status]} color={statusChipColor[row.status]} variant={row.status === "SCHEDULED" ? "outlined" : "filled"} />
                  </TableCell>
                  <TableCell>
                    <Stack direction="row" spacing={1}>
                      {row.status === "SCHEDULED" && (
                        <Button size="small" variant="outlined" onClick={() => void setStatus(row, "WAITING")}>Arrived</Button>
                      )}
                      {row.status === "WAITING" && (
                        <Button size="small" variant="outlined" color="success" onClick={() => void setStatus(row, "DONE")}>Start</Button>
                      )}
                      <Button size="small" onClick={() => setEditTarget(row)}>Edit</Button>
                      <Button size="small" color="error" onClick={() => setDeleteTarget(row)}>Cancel</Button>
                    </Stack>
                  </TableCell>
                </TableRow>
              ))}
            </TableBody>
          </Table>
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
        description={deleteTarget ? `Cancel ${deleteTarget.patientName}'s ${formatTime(deleteTarget.scheduledAt)} appointment?` : ""}
        onClose={() => setDeleteTarget(null)}
        onConfirm={() => void handleDelete()}
      />

      <Snackbar open={snackbar.open} autoHideDuration={4000} onClose={() => setSnackbar((c) => ({ ...c, open: false }))} anchorOrigin={{ vertical: "bottom", horizontal: "right" }}>
        <Alert onClose={() => setSnackbar((c) => ({ ...c, open: false }))} severity={snackbar.severity} variant="filled">{snackbar.message}</Alert>
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
    <Dialog open onClose={saving ? undefined : onClose} fullWidth maxWidth="xs" PaperProps={{ sx: { bgcolor: adminColors.panelElevated, color: adminColors.textStrong, border: `1px solid ${adminColors.border}`, backgroundImage: "none" } }}>
      <DialogTitle sx={{ color: adminColors.textStrong }}>Edit Appointment</DialogTitle>
      <DialogContent>
        <Stack spacing={2.5} sx={{ pt: 1 }}>
          <TextField label="Scheduled At" type="datetime-local" value={scheduledAt} onChange={(e) => setScheduledAt(e.target.value)} sx={adminTextFieldSx} InputLabelProps={{ shrink: true }} />
          <TextField select label="Therapist" value={therapistId} onChange={(e) => setTherapistId(e.target.value)} sx={adminTextFieldSx} SelectProps={adminSelectProps}>
            {therapists.map((t) => (
              <MenuItem key={t.id} value={String(t.id)}>{t.name}</MenuItem>
            ))}
          </TextField>
          <TextField select label="Type" value={type} onChange={(e) => setType(e.target.value as AppointmentType)} sx={adminTextFieldSx} SelectProps={adminSelectProps}>
            {appointmentTypeOptions.map((o) => (
              <MenuItem key={o.value} value={o.value}>{o.label}</MenuItem>
            ))}
          </TextField>
        </Stack>
      </DialogContent>
      <DialogActions sx={{ px: 3, pb: 2.5 }}>
        <Button onClick={onClose} disabled={saving}>Cancel</Button>
        <Button variant="contained" onClick={() => void handleSave()} disabled={saving}>Save</Button>
      </DialogActions>
    </Dialog>
  );
}
