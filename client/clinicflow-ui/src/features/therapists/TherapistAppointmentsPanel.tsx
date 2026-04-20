import DeleteOutlineIcon from "@mui/icons-material/DeleteOutline";
import EditOutlinedIcon from "@mui/icons-material/EditOutlined";
import Alert from "@mui/material/Alert";
import Box from "@mui/material/Box";
import Button from "@mui/material/Button";
import Chip from "@mui/material/Chip";
import IconButton from "@mui/material/IconButton";
import Snackbar from "@mui/material/Snackbar";
import Stack from "@mui/material/Stack";
import TextField from "@mui/material/TextField";
import Typography from "@mui/material/Typography";
import { DataGrid, type GridColDef, type GridRenderCellParams } from "@mui/x-data-grid";
import { useEffect, useMemo, useState } from "react";
import { useAuth } from "../../auth/AuthContext";
import { ConfirmDeleteDialog } from "../../components/admin/ConfirmDeleteDialog";
import { adminColors, adminDataGridSx, adminTextFieldSx } from "../../components/admin/adminStyles";
import { AppointmentDialog } from "../appointments/AppointmentDialog";
import {
  createAppointment,
  listAppointmentsByTherapist,
  removeAppointment,
  updateAppointment,
} from "../../services/appointmentService";
import { listBodyRegions } from "../../services/bodyRegionService";
import { getCaseById, listCasesByPatient } from "../../services/caseService";
import { listPatients } from "../../services/patientService";
import { listTherapists } from "../../services/therapistService";
import type {
  AppointmentCreateRequest,
  AppointmentRecord,
  AppointmentStatusValue,
  AppointmentUpdateRequest,
  BodyRegion,
  CaseSummary,
  Patient,
  Therapist,
} from "../../types/admin";

type DialogState = { mode: "create"; record: null } | { mode: "edit"; record: AppointmentRecord };
type SnackbarState = { open: boolean; severity: "success" | "error"; message: string };

function getTodayDateString() {
  const now = new Date();
  return `${now.getFullYear()}-${String(now.getMonth() + 1).padStart(2, "0")}-${String(now.getDate()).padStart(2, "0")}`;
}

function formatScheduledAt(value: string) {
  const parsed = new Date(value.includes("T") ? value : value.replace(" ", "T"));
  if (Number.isNaN(parsed.getTime())) return value;
  return new Intl.DateTimeFormat(undefined, {
    year: "numeric", month: "short", day: "numeric",
    hour: "numeric", minute: "2-digit",
  }).format(parsed);
}

function formatEnumLabel(value: string) {
  return value.toLowerCase().split("_").map((p) => p.charAt(0).toUpperCase() + p.slice(1)).join(" ");
}

function getStatusChipColor(status: AppointmentStatusValue): "default" | "info" | "warning" | "success" {
  switch (status) {
    case "CHECKED_IN": return "info";
    case "IN_SESSION": return "warning";
    case "FINISHED": return "success";
    default: return "default";
  }
}

type Props = { therapist: Therapist | null };

export function TherapistAppointmentsPanel({ therapist }: Props) {
  const { currentUser } = useAuth();
  const isDemo = currentUser?.isDemo === true;

  const [dateFilter, setDateFilter] = useState(getTodayDateString);
  const [appointments, setAppointments] = useState<AppointmentRecord[]>([]);
  const [appointmentsLoading, setAppointmentsLoading] = useState(false);
  const [appointmentsError, setAppointmentsError] = useState<string | null>(null);

  // Lookup data
  const [caseCache, setCaseCache] = useState<Record<number, CaseSummary>>({});
  const [patients, setPatients] = useState<Patient[]>([]);
  const [bodyRegions, setBodyRegions] = useState<BodyRegion[]>([]);
  const [therapists, setTherapists] = useState<Therapist[]>([]);

  // Dialog state
  const [dialogState, setDialogState] = useState<DialogState | null>(null);
  const [dialogPatientId, setDialogPatientId] = useState<number | null>(null);
  const [dialogCases, setDialogCases] = useState<CaseSummary[]>([]);
  const [dialogCaseLoading, setDialogCaseLoading] = useState(false);
  const [dialogError, setDialogError] = useState<string | null>(null);
  const [dialogSubmitting, setDialogSubmitting] = useState(false);

  // Delete state
  const [deleteTarget, setDeleteTarget] = useState<AppointmentRecord | null>(null);
  const [deleteError, setDeleteError] = useState<string | null>(null);

  const [snackbar, setSnackbar] = useState<SnackbarState>({ open: false, severity: "success", message: "" });

  // Load lookup data once
  useEffect(() => {
    let active = true;
    async function loadLookups() {
      try {
        const [patientResult, bodyRegionResult, therapistResult] = await Promise.all([
          listPatients({ page: 0, size: 1000, search: "" }),
          listBodyRegions(),
          listTherapists(),
        ]);
        if (!active) return;
        setPatients(patientResult.rows);
        setBodyRegions(bodyRegionResult.rows);
        setTherapists(therapistResult.rows);
      } catch {
        // Display falls back to IDs when lookups fail.
      }
    }
    void loadLookups();
    return () => { active = false; };
  }, []);

  // Load appointments when therapist or date changes
  useEffect(() => {
    if (therapist === null) { setAppointments([]); return; }
    let active = true;

    async function load() {
      setAppointmentsLoading(true);
      setAppointmentsError(null);
      try {
        const result = await listAppointmentsByTherapist(therapist.id, { date: dateFilter || undefined });
        if (active) setAppointments(result.rows);
      } catch (e) {
        if (active) setAppointmentsError(e instanceof Error ? e.message : "Unable to load appointments.");
      } finally {
        if (active) setAppointmentsLoading(false);
      }
    }

    void load();
    return () => { active = false; };
  }, [therapist, dateFilter]);

  // Hydrate cases for display
  useEffect(() => {
    let active = true;
    async function hydrateCases() {
      const missing = Array.from(new Set(appointments.map((a) => a.caseId))).filter((id) => !(id in caseCache));
      if (missing.length === 0) return;
      try {
        const fetched = await Promise.all(missing.map((id) => getCaseById(id)));
        if (!active) return;
        setCaseCache((current) => {
          const next = { ...current };
          fetched.forEach((c) => { next[c.id] = c; });
          return next;
        });
      } catch {
        // Rows fall back to IDs when hydration fails.
      }
    }
    void hydrateCases();
    return () => { active = false; };
  }, [appointments, caseCache]);

  const patientNameMap = useMemo(() => new Map(patients.map((p) => [p.id, p.displayName])), [patients]);
  const bodyRegionNameMap = useMemo(() => new Map(bodyRegions.map((br) => [br.id, br.displayName])), [bodyRegions]);
  const caseMap = useMemo(() => new Map(Object.values(caseCache).map((c) => [c.id, c])), [caseCache]);
  const therapistOptions = useMemo(() => therapists.map((t) => ({ value: t.id, label: `${t.therapistName} · ${t.type}` })), [therapists]);
  const dialogCaseOptions = useMemo(() => dialogCases.map((c) => ({
    value: c.id,
    label: `${patientNameMap.get(c.patientId) ?? `Patient #${c.patientId}`} · ${bodyRegionNameMap.get(c.bodyRegionId) ?? `Region #${c.bodyRegionId}`}`,
  })), [dialogCases, patientNameMap, bodyRegionNameMap]);

  async function refreshAppointments() {
    if (!therapist) return;
    const result = await listAppointmentsByTherapist(therapist.id, { date: dateFilter || undefined });
    setAppointments(result.rows);
  }

  async function loadCasesForPatient(patientId: number | null) {
    setDialogPatientId(patientId);
    if (patientId === null) { setDialogCases([]); return; }
    setDialogCaseLoading(true);
    try {
      const cases = await listCasesByPatient(patientId);
      setDialogCases(cases);
      setCaseCache((current) => {
        const next = { ...current };
        cases.forEach((c) => { next[c.id] = c; });
        return next;
      });
    } finally {
      setDialogCaseLoading(false);
    }
  }

  async function prepareEditDialog(record: AppointmentRecord) {
    const caseItem = caseMap.get(record.caseId) ?? await getCaseById(record.caseId);
    setCaseCache((current) => ({ ...current, [caseItem.id]: caseItem }));
    await loadCasesForPatient(caseItem.patientId);
    setDialogState({ mode: "edit", record });
  }

  async function handleDialogSubmit(payload: AppointmentCreateRequest | AppointmentUpdateRequest) {
    setDialogError(null);
    setDialogSubmitting(true);
    try {
      if (dialogState?.mode === "create") {
        await createAppointment(payload as AppointmentCreateRequest);
        setSnackbar({ open: true, severity: "success", message: "Appointment created." });
      } else if (dialogState?.mode === "edit") {
        await updateAppointment(dialogState.record.id, payload as AppointmentUpdateRequest);
        setSnackbar({ open: true, severity: "success", message: "Appointment updated." });
      }
      await refreshAppointments();
      setDialogState(null);
      setDialogCases([]);
      setDialogPatientId(null);
    } catch (e) {
      const message = e instanceof Error ? e.message : "Unable to save appointment.";
      setDialogError(message);
      setSnackbar({ open: true, severity: "error", message });
    } finally {
      setDialogSubmitting(false);
    }
  }

  async function handleDelete() {
    if (!deleteTarget) return;
    setDeleteError(null);
    try {
      await removeAppointment(deleteTarget.id);
      await refreshAppointments();
      setDeleteTarget(null);
      setSnackbar({ open: true, severity: "success", message: "Appointment deleted." });
    } catch (e) {
      const message = e instanceof Error ? e.message : "Unable to delete appointment.";
      setDeleteError(message);
      setSnackbar({ open: true, severity: "error", message });
    }
  }

  function closeDialog() {
    if (!dialogSubmitting) {
      setDialogState(null);
      setDialogPatientId(null);
      setDialogCases([]);
    }
  }

  const columns: GridColDef<AppointmentRecord>[] = [
    {
      field: "scheduledAt", headerName: "Scheduled At", flex: 1.2, minWidth: 200,
      valueFormatter: (value) => formatScheduledAt(String(value)),
    },
    {
      field: "caseId", headerName: "Patient · Region", flex: 1.4, minWidth: 240,
      renderCell: (params: GridRenderCellParams<AppointmentRecord, number>) => {
        const caseItem = caseMap.get(params.value ?? -1);
        if (!caseItem) return `Case #${params.value}`;
        const patient = patientNameMap.get(caseItem.patientId) ?? `Patient #${caseItem.patientId}`;
        const region = bodyRegionNameMap.get(caseItem.bodyRegionId) ?? `Region #${caseItem.bodyRegionId}`;
        return `${patient} · ${region}`;
      },
    },
    {
      field: "type", headerName: "Type", flex: 0.8, minWidth: 140,
      valueFormatter: (value) => formatEnumLabel(String(value)),
    },
    {
      field: "status", headerName: "Status", flex: 0.8, minWidth: 140, sortable: false,
      renderCell: (params: GridRenderCellParams<AppointmentRecord, AppointmentStatusValue>) => (
        <Chip
          label={formatEnumLabel(params.value ?? "SCHEDULED")}
          color={getStatusChipColor(params.value ?? "SCHEDULED")}
          size="small"
          variant={params.value === "SCHEDULED" ? "outlined" : "filled"}
          sx={params.value === "SCHEDULED" ? { color: adminColors.textStrong, borderColor: "rgba(148,163,184,0.4)", backgroundColor: "rgba(148,163,184,0.12)", fontWeight: 600 } : undefined}
        />
      ),
    },
    {
      field: "actions", headerName: "Actions", flex: 0.6, minWidth: 110, sortable: false, filterable: false,
      renderCell: (params: GridRenderCellParams<AppointmentRecord>) => (
        <Stack direction="row" spacing={0.5}>
          <IconButton color="primary" disabled={isDemo} onClick={() => { setDialogError(null); void prepareEditDialog(params.row); }}>
            <EditOutlinedIcon fontSize="small" />
          </IconButton>
          <IconButton color="error" disabled={isDemo} onClick={() => { setDeleteError(null); setDeleteTarget(params.row); }}>
            <DeleteOutlineIcon fontSize="small" />
          </IconButton>
        </Stack>
      ),
    },
  ];

  return (
    <>
      <Stack spacing={2.5} sx={{ height: "100%" }}>
        <Stack direction={{ xs: "column", md: "row" }} spacing={2} alignItems={{ md: "center" }} justifyContent="space-between">
          <Box>
            <Typography variant="h5" sx={{ color: adminColors.textStrong, fontWeight: 700 }}>
              {therapist ? therapist.therapistName : "Select a therapist"}
            </Typography>
            <Typography sx={{ color: adminColors.textSecondary, mt: 0.75 }}>
              {therapist
                ? `${formatEnumLabel(therapist.type)} · ${appointments.length} appointment${appointments.length === 1 ? "" : "s"}${dateFilter ? " on selected date" : ""}`
                : "Choose a therapist from the list to view their appointments."}
            </Typography>
          </Box>
          {therapist && (
            <Stack direction="row" spacing={1.5} alignItems="center">
              <TextField
                label="Date"
                type="date"
                value={dateFilter}
                onChange={(e) => setDateFilter(e.target.value)}
                size="small"
                sx={{ width: 180, ...adminTextFieldSx }}
                InputLabelProps={{ shrink: true }}
              />
              {dateFilter && (
                <Button size="small" variant="outlined" onClick={() => setDateFilter("")}>
                  Clear
                </Button>
              )}
              <Button
                size="small"
                variant="contained"
                disabled={isDemo}
                onClick={() => { setDialogError(null); setDialogState({ mode: "create", record: null }); }}
              >
                Add Appointment
              </Button>
            </Stack>
          )}
        </Stack>

        {appointmentsError && <Alert severity="error">{appointmentsError}</Alert>}

        {!therapist ? (
          <Box sx={{ flexGrow: 1, display: "grid", placeItems: "center", borderRadius: 3, border: `1px dashed ${adminColors.border}`, color: adminColors.textSecondary, px: 3, py: 8, textAlign: "center" }}>
            <Typography>Select a therapist to view their appointments.</Typography>
          </Box>
        ) : (
          <Box sx={{ flex: 1, minHeight: 400 }}>
            <DataGrid
              rows={appointments}
              getRowId={(row) => row.id}
              columns={columns}
              loading={appointmentsLoading}
              pagination
              paginationMode="client"
              rowCount={appointments.length}
              pageSizeOptions={[10, 25, 50]}
              initialState={{ pagination: { paginationModel: { pageSize: 10 } } }}
              disableRowSelectionOnClick
              sx={adminDataGridSx}
            />
          </Box>
        )}
      </Stack>

      {dialogState && therapist && (
        <AppointmentDialog
          key={dialogState.mode === "create" ? `create-${therapist.id}` : `edit-${dialogState.record.id}`}
          open
          mode={dialogState.mode}
          initialValue={dialogState.record}
          initialTherapistId={therapist.id}
          patients={patients}
          initialPatientId={dialogState.mode === "edit" ? (caseMap.get(dialogState.record.caseId)?.patientId ?? dialogPatientId) : dialogPatientId}
          caseOptions={dialogCaseOptions}
          therapistOptions={therapistOptions}
          caseOptionsLoading={dialogCaseLoading}
          caseHelperText={!dialogPatientId ? "Select a patient to load related cases." : undefined}
          submitting={dialogSubmitting}
          error={dialogError}
          onPatientChange={(id) => { void loadCasesForPatient(id); }}
          onClose={closeDialog}
          onSubmit={handleDialogSubmit}
        />
      )}

      <ConfirmDeleteDialog
        open={deleteTarget !== null}
        description={deleteTarget ? `Delete the appointment scheduled for "${formatScheduledAt(deleteTarget.scheduledAt)}"? This action cannot be undone.` : ""}
        error={deleteError}
        onClose={() => setDeleteTarget(null)}
        onConfirm={() => void handleDelete()}
      />

      <Snackbar open={snackbar.open} autoHideDuration={4000} onClose={() => setSnackbar((c) => ({ ...c, open: false }))} anchorOrigin={{ vertical: "bottom", horizontal: "right" }}>
        <Alert onClose={() => setSnackbar((c) => ({ ...c, open: false }))} severity={snackbar.severity} variant="filled">{snackbar.message}</Alert>
      </Snackbar>
    </>
  );
}
