import ExpandMoreIcon from "@mui/icons-material/ExpandMore";
import AddIcon from "@mui/icons-material/Add";
import EventAvailableOutlinedIcon from "@mui/icons-material/EventAvailableOutlined";
import DeleteOutlineIcon from "@mui/icons-material/DeleteOutline";
import EditOutlinedIcon from "@mui/icons-material/EditOutlined";
import SearchIcon from "@mui/icons-material/Search";
import Alert from "@mui/material/Alert";
import Accordion from "@mui/material/Accordion";
import AccordionDetails from "@mui/material/AccordionDetails";
import AccordionSummary from "@mui/material/AccordionSummary";
import Box from "@mui/material/Box";
import Button from "@mui/material/Button";
import Divider from "@mui/material/Divider";
import IconButton from "@mui/material/IconButton";
import InputAdornment from "@mui/material/InputAdornment";
import Paper from "@mui/material/Paper";
import Snackbar from "@mui/material/Snackbar";
import Stack from "@mui/material/Stack";
import TextField from "@mui/material/TextField";
import Typography from "@mui/material/Typography";
import { DataGrid, type GridColDef, type GridPaginationModel, type GridRenderCellParams } from "@mui/x-data-grid";
import { useEffect, useMemo, useState } from "react";
import { ConfirmDeleteDialog } from "../../components/admin/ConfirmDeleteDialog";
import { adminColors, adminDataGridSx, adminTextFieldSx } from "../../components/admin/adminStyles";
import { useEntityCrud } from "../../hooks/useEntityCrud";
import { createAppointment } from "../../services/appointmentService";
import { listBodyRegions } from "../../services/bodyRegionService";
import { createCase, listCasesByPatient, removeCase, updateCase } from "../../services/caseService";
import { createPatient, listPatients, removePatient, updatePatient } from "../../services/patientService";
import { listTherapists } from "../../services/therapistService";
import type { AppointmentCreateRequest, BodyRegion, CaseCreateRequest, CaseSummary, CaseUpdateRequest, Patient, PatientCreateRequest, PatientUpdateRequest, Therapist } from "../../types/admin";
import { AppointmentDialog } from "../appointments/AppointmentDialog";
import { CaseDialog } from "../cases/CaseDialog";
import { PatientDialog } from "./PatientDialog";

type PatientDialogState = { mode: "create"; record: null } | { mode: "edit"; record: Patient };
type CaseDialogState = { mode: "create"; record: null } | { mode: "edit"; record: CaseSummary };
type AppointmentDialogState = { caseRecord: CaseSummary } | null;
type DeleteState = { kind: "patient"; record: Patient } | { kind: "case"; record: CaseSummary } | null;
type SnackbarState = { open: boolean; severity: "success" | "error"; message: string };

const patientCrudService = { list: listPatients, create: createPatient, update: updatePatient, remove: removePatient };

function formatCreatedAt(value: string) {
  const parsed = new Date(value);
  if (Number.isNaN(parsed.getTime())) return value;
  return new Intl.DateTimeFormat(undefined, { year: "numeric", month: "short", day: "numeric" }).format(parsed);
}

export function PatientsPage() {
  const [searchInput, setSearchInput] = useState("");
  const [search, setSearch] = useState("");
  const [paginationModel, setPaginationModel] = useState<GridPaginationModel>({ page: 0, pageSize: 10 });
  const [patientDialogState, setPatientDialogState] = useState<PatientDialogState | null>(null);
  const [caseDialogState, setCaseDialogState] = useState<CaseDialogState | null>(null);
  const [appointmentDialogState, setAppointmentDialogState] = useState<AppointmentDialogState>(null);
  const [deleteState, setDeleteState] = useState<DeleteState>(null);
  const [patientDialogError, setPatientDialogError] = useState<string | null>(null);
  const [caseDialogError, setCaseDialogError] = useState<string | null>(null);
  const [appointmentDialogError, setAppointmentDialogError] = useState<string | null>(null);
  const [deleteError, setDeleteError] = useState<string | null>(null);
  const [snackbar, setSnackbar] = useState<SnackbarState>({ open: false, severity: "success", message: "" });
  const [selectedPatientId, setSelectedPatientId] = useState<number | null>(null);
  const [selectedPatientCases, setSelectedPatientCases] = useState<CaseSummary[]>([]);
  const [casesLoading, setCasesLoading] = useState(false);
  const [casesError, setCasesError] = useState<string | null>(null);
  const [bodyRegions, setBodyRegions] = useState<BodyRegion[]>([]);
  const [bodyRegionsError, setBodyRegionsError] = useState<string | null>(null);
  const [therapists, setTherapists] = useState<Therapist[]>([]);
  const [therapistsError, setTherapistsError] = useState<string | null>(null);
  const [caseMutationKind, setCaseMutationKind] = useState<"create" | "update" | "delete" | null>(null);
  const [appointmentSubmitting, setAppointmentSubmitting] = useState(false);

  useEffect(() => {
    const timeoutId = window.setTimeout(() => {
      setSearch(searchInput.trim());
      setPaginationModel((current) => ({ ...current, page: 0 }));
    }, 300);
    return () => window.clearTimeout(timeoutId);
  }, [searchInput]);

  const listParams = useMemo(() => ({ page: paginationModel.page, size: paginationModel.pageSize, search }), [paginationModel.page, paginationModel.pageSize, search]);
  const isSearchMode = search.length > 0;
  const { rows, rowCount, loading, error, createEntity, updateEntity, deleteEntity, mutationKind } = useEntityCrud(patientCrudService, listParams, isSearchMode ? `search-${search}` : `${paginationModel.page}-${paginationModel.pageSize}`);

  useEffect(() => {
    let active = true;
    async function loadLookups() {
      try {
        const [bodyRegionResult, therapistResult] = await Promise.all([listBodyRegions(), listTherapists()]);
        if (!active) return;
        setBodyRegions(bodyRegionResult.rows);
        setTherapists(therapistResult.rows);
      } catch (loadError) {
        if (!active) return;
        const message = loadError instanceof Error ? loadError.message : "Unable to load lookup data.";
        setBodyRegionsError(message);
        setTherapistsError(message);
      }
    }
    void loadLookups();
    return () => { active = false; };
  }, []);

  useEffect(() => {
    if (rows.length === 0) {
      setSelectedPatientId(null);
      setSelectedPatientCases([]);
      setCasesError(null);
      return;
    }
    const hasSelectedPatient = selectedPatientId !== null && rows.some((patient) => patient.id === selectedPatientId);
    if (!hasSelectedPatient) setSelectedPatientId(rows[0].id);
  }, [rows, selectedPatientId]);

  const selectedPatient = useMemo(() => rows.find((patient) => patient.id === selectedPatientId) ?? null, [rows, selectedPatientId]);

  async function refreshSelectedPatientCases() {
    if (selectedPatientId === null) {
      setSelectedPatientCases([]);
      return;
    }
    setCasesLoading(true);
    setCasesError(null);
    try {
      const cases = await listCasesByPatient(selectedPatientId);
      setSelectedPatientCases(cases);
    } catch (loadError) {
      setSelectedPatientCases([]);
      setCasesError(loadError instanceof Error ? loadError.message : "Unable to load cases.");
    } finally {
      setCasesLoading(false);
    }
  }

  useEffect(() => { void refreshSelectedPatientCases(); }, [selectedPatientId]);

  const bodyRegionNameMap = useMemo(() => new Map(bodyRegions.map((bodyRegion) => [bodyRegion.id, bodyRegion.displayName])), [bodyRegions]);
  const therapistOptions = useMemo(() => therapists.map((therapist) => ({ value: therapist.id, label: `${therapist.therapistName} · ${therapist.type}` })), [therapists]);

  async function handlePatientDialogSubmit(payload: PatientCreateRequest | PatientUpdateRequest) {
    setPatientDialogError(null);
    try {
      if (patientDialogState?.mode === "create") {
        await createEntity(payload as PatientCreateRequest);
        setSnackbar({ open: true, severity: "success", message: "Patient created." });
      } else if (patientDialogState?.mode === "edit") {
        await updateEntity(patientDialogState.record.id, payload as PatientUpdateRequest);
        setSnackbar({ open: true, severity: "success", message: "Patient updated." });
      }
      setPatientDialogState(null);
    } catch (submitError) {
      const message = submitError instanceof Error ? submitError.message : "Unable to save patient.";
      setPatientDialogError(message);
      setSnackbar({ open: true, severity: "error", message });
    }
  }

  async function handleCaseDialogSubmit(payload: CaseCreateRequest | CaseUpdateRequest) {
    if (!selectedPatient) return;
    setCaseDialogError(null);
    setCaseMutationKind(caseDialogState?.mode === "create" ? "create" : "update");
    try {
      if (caseDialogState?.mode === "create") {
        await createCase(payload as CaseCreateRequest);
        setSnackbar({ open: true, severity: "success", message: "Case created." });
      } else if (caseDialogState?.mode === "edit") {
        await updateCase(caseDialogState.record.id, payload as CaseUpdateRequest);
        setSnackbar({ open: true, severity: "success", message: "Case updated." });
      }
      await refreshSelectedPatientCases();
      setCaseDialogState(null);
    } catch (submitError) {
      const message = submitError instanceof Error ? submitError.message : "Unable to save case.";
      setCaseDialogError(message);
      setSnackbar({ open: true, severity: "error", message });
    } finally {
      setCaseMutationKind(null);
    }
  }

  async function handleAppointmentDialogSubmit(payload: AppointmentCreateRequest) {
    setAppointmentDialogError(null);
    setAppointmentSubmitting(true);
    try {
      await createAppointment(payload);
      setSnackbar({ open: true, severity: "success", message: "Appointment created." });
      setAppointmentDialogState(null);
    } catch (submitError) {
      const message = submitError instanceof Error ? submitError.message : "Unable to create appointment.";
      setAppointmentDialogError(message);
      setSnackbar({ open: true, severity: "error", message });
    } finally {
      setAppointmentSubmitting(false);
    }
  }

  async function handleDeleteConfirm() {
    if (!deleteState) return;
    setDeleteError(null);
    try {
      if (deleteState.kind === "patient") {
        await deleteEntity(deleteState.record.id);
        setSnackbar({ open: true, severity: "success", message: "Patient deleted." });
      } else {
        setCaseMutationKind("delete");
        await removeCase(deleteState.record.id);
        await refreshSelectedPatientCases();
        setSnackbar({ open: true, severity: "success", message: "Case deleted." });
      }
      setDeleteState(null);
    } catch (deleteRequestError) {
      const message = deleteRequestError instanceof Error ? deleteRequestError.message : `Unable to delete ${deleteState.kind}.`;
      setDeleteError(message);
      setSnackbar({ open: true, severity: "error", message });
    } finally {
      setCaseMutationKind(null);
    }
  }

  const columns: GridColDef<Patient>[] = [
    { field: "firstName", headerName: "First Name", flex: 1, minWidth: 140 },
    { field: "lastName", headerName: "Last Name", flex: 1, minWidth: 140 },
    { field: "displayName", headerName: "Display Name", flex: 1.1, minWidth: 170 },
    {
      field: "actions", headerName: "Actions", flex: 0.7, minWidth: 130, sortable: false, filterable: false, renderCell: (params: GridRenderCellParams<Patient>) => (
        <Stack direction="row" spacing={0.5}>
          <IconButton color="primary" onClick={(event) => { event.stopPropagation(); setPatientDialogError(null); setPatientDialogState({ mode: "edit", record: params.row }); }}><EditOutlinedIcon fontSize="small" /></IconButton>
          <IconButton color="error" onClick={(event) => { event.stopPropagation(); setDeleteError(null); setDeleteState({ kind: "patient", record: params.row }); }}><DeleteOutlineIcon fontSize="small" /></IconButton>
        </Stack>
      ),
    },
  ];

  return (
    <>
      <Stack spacing={3}>
        <Stack direction={{ xs: "column", md: "row" }} spacing={2} alignItems={{ md: "center" }} justifyContent="space-between">
          <Box>
            <Typography variant="h4" sx={{ color: "#f8fafc", fontWeight: 700 }}>Patients</Typography>
            <Typography sx={{ color: adminColors.textSecondary, mt: 1 }}>Manage patient records and the cases attached to each patient.</Typography>
          </Box>
          <Button variant="contained" onClick={() => { setPatientDialogError(null); setPatientDialogState({ mode: "create", record: null }); }}>Add Patient</Button>
        </Stack>
        <Stack direction={{ xs: "column", lg: "row" }} spacing={3} alignItems="stretch">
          <Paper elevation={0} sx={{ p: 2, borderRadius: 4, bgcolor: adminColors.panelBg, border: `1px solid ${adminColors.border}`, flex: { lg: "0 0 48%" }, minWidth: 0 }}>
            <Stack spacing={2}>
              <TextField value={searchInput} onChange={(e) => setSearchInput(e.target.value)} placeholder="Search by first or last name" fullWidth sx={{ ...adminTextFieldSx }} InputProps={{ startAdornment: <InputAdornment position="start"><SearchIcon fontSize="small" /></InputAdornment> }} />
              {error && <Alert severity="error">{error}</Alert>}
              <Box sx={{ height: 620 }}>
                <DataGrid rows={rows} columns={columns} loading={loading} pagination paginationMode={isSearchMode ? "client" : "server"} rowCount={rowCount} paginationModel={paginationModel} onPaginationModelChange={setPaginationModel} onRowClick={(params) => setSelectedPatientId(params.row.id)} pageSizeOptions={[5, 10, 20, 50]} disableRowSelectionOnClick getRowClassName={(params) => (params.row.id === selectedPatientId ? "Mui-selected" : "")} sx={adminDataGridSx} />
              </Box>
            </Stack>
          </Paper>
          <Paper elevation={0} sx={{ p: 2.5, borderRadius: 4, bgcolor: adminColors.panelBg, border: `1px solid ${adminColors.border}`, flex: 1, minWidth: 0 }}>
            <Stack spacing={2.5} sx={{ height: "100%" }}>
              <Stack direction={{ xs: "column", md: "row" }} spacing={2} alignItems={{ md: "center" }} justifyContent="space-between">
                <Box>
                  <Typography variant="h5" sx={{ color: adminColors.textStrong, fontWeight: 700 }}>{selectedPatient ? selectedPatient.displayName : "Select a patient"}</Typography>
                  <Typography sx={{ color: adminColors.textSecondary, mt: 0.75 }}>
                    {selectedPatient ? `${selectedPatient.firstName} ${selectedPatient.lastName} · ${selectedPatientCases.length} case${selectedPatientCases.length === 1 ? "" : "s"}` : "Choose a patient from the list to manage their cases."}
                  </Typography>
                </Box>
                <Button variant="contained" startIcon={<AddIcon />} disabled={!selectedPatient} onClick={() => { if (selectedPatient) { setCaseDialogError(null); setCaseDialogState({ mode: "create", record: null }); } }}>Add Case</Button>
              </Stack>
              {bodyRegionsError && <Alert severity="error">{bodyRegionsError}</Alert>}
              {therapistsError && <Alert severity="error">{therapistsError}</Alert>}
              {casesError && <Alert severity="error">{casesError}</Alert>}
              {!selectedPatient ? (
                <Box sx={{ flexGrow: 1, display: "grid", placeItems: "center", borderRadius: 3, border: `1px dashed ${adminColors.border}`, color: adminColors.textSecondary, px: 3, py: 8, textAlign: "center" }}>
                  <Typography>Select a patient to view and manage their cases.</Typography>
                </Box>
              ) : casesLoading ? (
                <Alert severity="info">Loading cases for {selectedPatient.displayName}...</Alert>
              ) : selectedPatientCases.length === 0 ? (
                <Box sx={{ flexGrow: 1, display: "grid", placeItems: "center", borderRadius: 3, border: `1px dashed ${adminColors.border}`, color: adminColors.textSecondary, px: 3, py: 8, textAlign: "center" }}>
                  <Typography>No cases found for this patient.</Typography>
                </Box>
              ) : (
                <Stack spacing={1.5}>
                  {selectedPatientCases.map((caseItem) => (
                    <Accordion key={caseItem.id} disableGutters elevation={0} sx={{ bgcolor: adminColors.panelElevated, color: adminColors.textStrong, border: `1px solid ${adminColors.border}`, borderRadius: "16px !important", "&:before": { display: "none" } }}>
                      <AccordionSummary expandIcon={<ExpandMoreIcon sx={{ color: adminColors.textStrong }} />}>
                        <Stack direction={{ xs: "column", md: "row" }} spacing={1.5} sx={{ width: "100%" }} alignItems={{ md: "center" }}>
                          <Typography sx={{ fontWeight: 700, minWidth: 100 }}>Case #{caseItem.id}</Typography>
                          <Typography sx={{ color: adminColors.textSecondary, flexGrow: 1 }}>{bodyRegionNameMap.get(caseItem.bodyRegionId) ?? `Body Region #${caseItem.bodyRegionId}`}</Typography>
                          <Typography sx={{ color: adminColors.textMuted }}>Created {formatCreatedAt(caseItem.createdAt)}</Typography>
                        </Stack>
                      </AccordionSummary>
                      <AccordionDetails>
                        <Stack spacing={2}>
                          <Stack direction={{ xs: "column", sm: "row" }} spacing={2}>
                            <Box sx={{ flex: 1 }}>
                              <Typography variant="overline" sx={{ color: adminColors.textMuted }}>Patient</Typography>
                              <Typography>{selectedPatient.displayName}</Typography>
                            </Box>
                            <Box sx={{ flex: 1 }}>
                              <Typography variant="overline" sx={{ color: adminColors.textMuted }}>Body Region</Typography>
                              <Typography>{bodyRegionNameMap.get(caseItem.bodyRegionId) ?? `Body Region #${caseItem.bodyRegionId}`}</Typography>
                            </Box>
                            <Box sx={{ flex: 1 }}>
                              <Typography variant="overline" sx={{ color: adminColors.textMuted }}>Created</Typography>
                              <Typography>{formatCreatedAt(caseItem.createdAt)}</Typography>
                            </Box>
                          </Stack>
                          <Divider sx={{ borderColor: adminColors.borderMuted }} />
                          <Stack direction="row" spacing={1} flexWrap="wrap">
                            <Button variant="outlined" startIcon={<EditOutlinedIcon />} onClick={() => { setCaseDialogError(null); setCaseDialogState({ mode: "edit", record: caseItem }); }}>Edit Case</Button>
                            <Button variant="outlined" startIcon={<EventAvailableOutlinedIcon />} disabled={therapists.length === 0 || Boolean(therapistsError)} onClick={() => { setAppointmentDialogError(null); setAppointmentDialogState({ caseRecord: caseItem }); }}>Create Appointment</Button>
                            <Button variant="outlined" color="error" startIcon={<DeleteOutlineIcon />} onClick={() => { setDeleteError(null); setDeleteState({ kind: "case", record: caseItem }); }}>Delete Case</Button>
                          </Stack>
                        </Stack>
                      </AccordionDetails>
                    </Accordion>
                  ))}
                </Stack>
              )}
            </Stack>
          </Paper>
        </Stack>
      </Stack>
      {patientDialogState && <PatientDialog key={patientDialogState.mode === "create" ? "create" : `edit-${patientDialogState.record.id}`} open mode={patientDialogState.mode} initialValue={patientDialogState.record} submitting={mutationKind === "create" || mutationKind === "update"} error={patientDialogError} onClose={() => { if (!(mutationKind === "create" || mutationKind === "update")) setPatientDialogState(null); }} onSubmit={handlePatientDialogSubmit} />}
      {caseDialogState && selectedPatient && <CaseDialog key={caseDialogState.mode === "create" ? `create-${selectedPatient.id}` : `edit-${caseDialogState.record.id}`} open mode={caseDialogState.mode} initialValue={caseDialogState.record} patients={[selectedPatient]} fixedPatient={selectedPatient} bodyRegions={bodyRegions} submitting={caseMutationKind === "create" || caseMutationKind === "update"} error={caseDialogError} onClose={() => { if (!(caseMutationKind === "create" || caseMutationKind === "update")) setCaseDialogState(null); }} onSubmit={handleCaseDialogSubmit} />}
      {appointmentDialogState && selectedPatient && <AppointmentDialog key={`create-appointment-${appointmentDialogState.caseRecord.id}`} open mode="create" patients={[selectedPatient]} fixedPatient={selectedPatient} fixedCase={appointmentDialogState.caseRecord} initialPatientId={selectedPatient.id} caseOptions={[{ value: appointmentDialogState.caseRecord.id, label: `Case #${appointmentDialogState.caseRecord.id}` }]} therapistOptions={therapistOptions} caseHelperText={`Case #${appointmentDialogState.caseRecord.id} is selected from this patient.`} submitting={appointmentSubmitting} error={appointmentDialogError} onPatientChange={() => {}} onClose={() => { if (!appointmentSubmitting) setAppointmentDialogState(null); }} onSubmit={async (payload) => { await handleAppointmentDialogSubmit(payload as AppointmentCreateRequest); }} />}
      <ConfirmDeleteDialog open={deleteState !== null} description={deleteState?.kind === "patient" ? `Delete patient "${deleteState.record.displayName}"? This action cannot be undone.` : deleteState?.kind === "case" ? `Delete case #${deleteState.record.id}? This action cannot be undone.` : ""} error={deleteError} loading={mutationKind === "delete" || caseMutationKind === "delete"} onClose={() => { if (!(mutationKind === "delete" || caseMutationKind === "delete")) setDeleteState(null); }} onConfirm={() => void handleDeleteConfirm()} />
      <Snackbar open={snackbar.open} autoHideDuration={4000} onClose={() => setSnackbar((c) => ({ ...c, open: false }))} anchorOrigin={{ vertical: "bottom", horizontal: "right" }}>
        <Alert onClose={() => setSnackbar((c) => ({ ...c, open: false }))} severity={snackbar.severity} variant="filled">{snackbar.message}</Alert>
      </Snackbar>
    </>
  );
}
