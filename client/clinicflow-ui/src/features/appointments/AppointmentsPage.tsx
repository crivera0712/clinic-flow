import DeleteOutlineIcon from "@mui/icons-material/DeleteOutline";
import EditOutlinedIcon from "@mui/icons-material/EditOutlined";
import Alert from "@mui/material/Alert";
import Box from "@mui/material/Box";
import Button from "@mui/material/Button";
import Chip from "@mui/material/Chip";
import IconButton from "@mui/material/IconButton";
import Paper from "@mui/material/Paper";
import Snackbar from "@mui/material/Snackbar";
import Stack from "@mui/material/Stack";
import TextField from "@mui/material/TextField";
import Typography from "@mui/material/Typography";
import { DataGrid, type GridColDef, type GridPaginationModel, type GridRenderCellParams } from "@mui/x-data-grid";
import { useEffect, useMemo, useState } from "react";
import { useAuth } from "../../auth/AuthContext";
import { ConfirmDeleteDialog } from "../../components/admin/ConfirmDeleteDialog";
import { adminColors, adminDataGridSx, adminTextFieldSx } from "../../components/admin/adminStyles";
import { AppointmentDialog } from "./AppointmentDialog";
import { createAppointment, listAppointments, removeAppointment, updateAppointment } from "../../services/appointmentService";
import { getCaseById, listCasesByPatient } from "../../services/caseService";
import { listPatients } from "../../services/patientService";
import { listTherapists } from "../../services/therapistService";
import { listBodyRegions } from "../../services/bodyRegionService";
import type { AppointmentCreateRequest, AppointmentRecord, AppointmentStatusValue, AppointmentUpdateRequest, BodyRegion, CaseSummary, Patient, Therapist } from "../../types/admin";

type DialogState = { mode: "create"; record: null } | { mode: "edit"; record: AppointmentRecord };
type SnackbarState = { open: boolean; severity: "success" | "error"; message: string };

function getTodayDateString() {
  const now = new Date();
  return `${now.getFullYear()}-${String(now.getMonth() + 1).padStart(2, "0")}-${String(now.getDate()).padStart(2, "0")}`;
}

function formatEnumLabel(value: string) {
  return value.toLowerCase().split("_").map((part) => part.charAt(0).toUpperCase() + part.slice(1)).join(" ");
}

function formatScheduledAt(value: string) {
  const parsed = new Date(value.includes("T") ? value : value.replace(" ", "T"));
  if (Number.isNaN(parsed.getTime())) return value;
  return new Intl.DateTimeFormat(undefined, { year: "numeric", month: "short", day: "numeric", hour: "numeric", minute: "2-digit" }).format(parsed);
}

function getStatusChipColor(status: AppointmentStatusValue): "default" | "info" | "warning" | "success" {
  switch (status) {
    case "CHECKED_IN": return "info";
    case "IN_SESSION": return "warning";
    case "FINISHED": return "success";
    default: return "default";
  }
}

export function AppointmentsPage() {
  const { currentUser } = useAuth();
  const isDemo = currentUser?.isDemo === true;
  const [selectedDate, setSelectedDate] = useState(getTodayDateString);
  const [rows, setRows] = useState<AppointmentRecord[]>([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);
  const [dialogState, setDialogState] = useState<DialogState | null>(null);
  const [deleteTarget, setDeleteTarget] = useState<AppointmentRecord | null>(null);
  const [dialogError, setDialogError] = useState<string | null>(null);
  const [deleteError, setDeleteError] = useState<string | null>(null);
  const [snackbar, setSnackbar] = useState<SnackbarState>({ open: false, severity: "success", message: "" });
  const [patients, setPatients] = useState<Patient[]>([]);
  const [therapists, setTherapists] = useState<Therapist[]>([]);
  const [bodyRegions, setBodyRegions] = useState<BodyRegion[]>([]);
  const [caseCache, setCaseCache] = useState<Record<number, CaseSummary>>({});
  const [dialogPatientId, setDialogPatientId] = useState<number | null>(null);
  const [dialogCases, setDialogCases] = useState<CaseSummary[]>([]);
  const [dialogCaseLoading, setDialogCaseLoading] = useState(false);
  const [dialogSubmitting, setDialogSubmitting] = useState(false);
  const [paginationModel, setPaginationModel] = useState<GridPaginationModel>({ page: 0, pageSize: 10 });

  async function refresh() {
    try {
      setLoading(true);
      setError(null);
      const result = await listAppointments({ date: selectedDate });
      setRows(result.rows);
    } catch (loadError) {
      setError(loadError instanceof Error ? loadError.message : "Unable to load appointments.");
    } finally {
      setLoading(false);
    }
  }

  useEffect(() => { void refresh(); }, [selectedDate]);

  useEffect(() => {
    let active = true;
    async function loadLookups() {
      try {
        const [patientResult, therapistResult, bodyRegionResult] = await Promise.all([
          listPatients({ page: 0, size: 1000, search: "" }),
          listTherapists(),
          listBodyRegions(),
        ]);
        if (!active) return;
        setPatients(patientResult.rows);
        setTherapists(therapistResult.rows);
        setBodyRegions(bodyRegionResult.rows);
      } catch {
        // Lookup failures are surfaced later through missing option labels.
      }
    }
    void loadLookups();
    return () => { active = false; };
  }, []);

  useEffect(() => {
    let active = true;
    async function hydrateCases() {
      const missing = Array.from(new Set(rows.map((row) => row.caseId))).filter((id) => !(id in caseCache));
      if (missing.length === 0) return;
      try {
        const fetched = await Promise.all(missing.map((id) => getCaseById(id)));
        if (!active) return;
        setCaseCache((current) => {
          const next = { ...current };
          fetched.forEach((caseItem) => { next[caseItem.id] = caseItem; });
          return next;
        });
      } catch {
        // Row rendering falls back to ids when case hydration fails.
      }
    }
    void hydrateCases();
    return () => { active = false; };
  }, [rows, caseCache]);

  const patientNameMap = useMemo(() => new Map(patients.map((patient) => [patient.id, patient.displayName])), [patients]);
  const therapistMap = useMemo(() => new Map(therapists.map((therapist) => [therapist.id, therapist])), [therapists]);
  const bodyRegionNameMap = useMemo(() => new Map(bodyRegions.map((bodyRegion) => [bodyRegion.id, bodyRegion.displayName])), [bodyRegions]);
  const caseMap = useMemo(() => new Map(Object.values(caseCache).map((caseItem) => [caseItem.id, caseItem])), [caseCache]);
  const caseOptions = useMemo(() => dialogCases.map((caseItem) => ({ value: caseItem.id, label: `Case #${caseItem.id} · ${patientNameMap.get(caseItem.patientId) ?? `Patient #${caseItem.patientId}`} · ${bodyRegionNameMap.get(caseItem.bodyRegionId) ?? `Region #${caseItem.bodyRegionId}`}` })), [dialogCases, patientNameMap, bodyRegionNameMap]);
  const therapistOptions = useMemo(() => therapists.map((therapist) => ({ value: therapist.id, label: `${therapist.therapistName} · ${therapist.type}` })), [therapists]);

  async function loadCasesForPatient(patientId: number | null) {
    setDialogPatientId(patientId);
    if (patientId === null) {
      setDialogCases([]);
      return;
    }
    setDialogCaseLoading(true);
    try {
      const cases = await listCasesByPatient(patientId);
      setDialogCases(cases);
      setCaseCache((current) => {
        const next = { ...current };
        cases.forEach((caseItem) => { next[caseItem.id] = caseItem; });
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
      await refresh();
      setDialogState(null);
      setDialogCases([]);
      setDialogPatientId(null);
    } catch (submitError) {
      const message = submitError instanceof Error ? submitError.message : "Unable to save appointment.";
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
      await refresh();
      setDeleteTarget(null);
      setSnackbar({ open: true, severity: "success", message: "Appointment deleted." });
    } catch (deleteRequestError) {
      const message = deleteRequestError instanceof Error ? deleteRequestError.message : "Unable to delete appointment.";
      setDeleteError(message);
      setSnackbar({ open: true, severity: "error", message });
    }
  }

  const columns: GridColDef<AppointmentRecord>[] = [
    { field: "scheduledAt", headerName: "Scheduled At", flex: 1.1, minWidth: 220, valueFormatter: (value) => formatScheduledAt(String(value)) },
    {
      field: "caseId", headerName: "Case", flex: 1.4, minWidth: 260, renderCell: (params: GridRenderCellParams<AppointmentRecord, number>) => {
        const caseItem = caseMap.get(params.value ?? -1);
        if (!caseItem) return `Case #${params.value}`;
        return `Case #${caseItem.id} · ${patientNameMap.get(caseItem.patientId) ?? `Patient #${caseItem.patientId}`} · ${bodyRegionNameMap.get(caseItem.bodyRegionId) ?? `Region #${caseItem.bodyRegionId}`}`;
      },
    },
    { field: "therapistId", headerName: "Therapist", flex: 1.1, minWidth: 220, renderCell: (params: GridRenderCellParams<AppointmentRecord, number>) => therapistMap.get(params.value ?? -1)?.therapistName ?? `Therapist #${params.value}` },
    { field: "type", headerName: "Type", flex: 0.8, minWidth: 160, valueFormatter: (value) => formatEnumLabel(String(value)) },
    {
      field: "status", headerName: "Status", flex: 0.8, minWidth: 160, renderCell: (params: GridRenderCellParams<AppointmentRecord, AppointmentStatusValue>) => (
        <Chip label={formatEnumLabel(params.value ?? "SCHEDULED")} color={getStatusChipColor(params.value ?? "SCHEDULED")} size="small" variant={params.value === "SCHEDULED" ? "outlined" : "filled"} />
      ),
    },
    {
      field: "actions", headerName: "Actions", flex: 0.8, minWidth: 140, sortable: false, filterable: false, renderCell: (params: GridRenderCellParams<AppointmentRecord>) => (
        <Stack direction="row" spacing={0.5}>
          <IconButton color="primary" disabled={isDemo} onClick={() => { void prepareEditDialog(params.row); }}><EditOutlinedIcon fontSize="small" /></IconButton>
          <IconButton color="error" disabled={isDemo} onClick={() => setDeleteTarget(params.row)}><DeleteOutlineIcon fontSize="small" /></IconButton>
        </Stack>
      ),
    },
  ];

  return (
    <>
      <Stack spacing={3}>
        <Stack direction={{ xs: "column", md: "row" }} spacing={2} alignItems={{ md: "center" }} justifyContent="space-between">
          <Box>
            <Typography variant="h4" sx={{ color: "#f8fafc", fontWeight: 700 }}>Appointments</Typography>
            <Typography sx={{ color: adminColors.textSecondary, mt: 1 }}>Manage daily appointments, therapist assignments, and status updates.</Typography>
          </Box>
          <Button variant="contained" disabled={isDemo} onClick={() => { setDialogError(null); setDialogState({ mode: "create", record: null }); }}>Add Appointment</Button>
        </Stack>
        <Paper elevation={0} sx={{ p: 2, borderRadius: 4, bgcolor: adminColors.panelBg, border: `1px solid ${adminColors.border}` }}>
          <Stack spacing={2}>
            <TextField label="Date" type="date" value={selectedDate} onChange={(e) => setSelectedDate(e.target.value)} sx={{ maxWidth: 240, ...adminTextFieldSx }} InputLabelProps={{ shrink: true }} />
            {error && <Alert severity="error">{error}</Alert>}
            <Box sx={{ height: 620 }}>
              <DataGrid rows={rows} columns={columns} loading={loading} pagination paginationMode="client" rowCount={rows.length} paginationModel={paginationModel} onPaginationModelChange={setPaginationModel} pageSizeOptions={[5, 10, 20, 50]} disableRowSelectionOnClick sx={adminDataGridSx} />
            </Box>
          </Stack>
        </Paper>
      </Stack>
      {dialogState && (
        <AppointmentDialog
          key={dialogState.mode === "create" ? "create" : `edit-${dialogState.record.id}`}
          open
          mode={dialogState.mode}
          initialValue={dialogState.record}
          patients={patients}
          initialPatientId={dialogState.mode === "edit" ? (caseMap.get(dialogState.record.caseId)?.patientId ?? dialogPatientId) : dialogPatientId}
          caseOptions={caseOptions}
          therapistOptions={therapistOptions}
          caseOptionsLoading={dialogCaseLoading}
          caseHelperText={!dialogPatientId ? "Select a patient to load related cases." : undefined}
          submitting={dialogSubmitting}
          error={dialogError}
          onPatientChange={(patientId) => { void loadCasesForPatient(patientId); }}
          onClose={() => { if (!dialogSubmitting) { setDialogState(null); setDialogPatientId(null); setDialogCases([]); } }}
          onSubmit={handleDialogSubmit}
        />
      )}
      <ConfirmDeleteDialog open={deleteTarget !== null} description={deleteTarget ? `Delete the appointment scheduled for "${formatScheduledAt(deleteTarget.scheduledAt)}"? This action cannot be undone.` : ""} error={deleteError} onClose={() => setDeleteTarget(null)} onConfirm={() => void handleDelete()} />
      <Snackbar open={snackbar.open} autoHideDuration={4000} onClose={() => setSnackbar((c) => ({ ...c, open: false }))} anchorOrigin={{ vertical: "bottom", horizontal: "right" }}>
        <Alert onClose={() => setSnackbar((c) => ({ ...c, open: false }))} severity={snackbar.severity} variant="filled">{snackbar.message}</Alert>
      </Snackbar>
    </>
  );
}
