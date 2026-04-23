import DeleteOutlineIcon from "@mui/icons-material/DeleteOutline";
import EditOutlinedIcon from "@mui/icons-material/EditOutlined";
import SearchIcon from "@mui/icons-material/Search";
import Alert from "@mui/material/Alert";
import Box from "@mui/material/Box";
import Button from "@mui/material/Button";
import IconButton from "@mui/material/IconButton";
import InputAdornment from "@mui/material/InputAdornment";
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
import { createTherapist, listTherapists, removeTherapist, updateTherapist } from "../../services/therapistService";
import type { Therapist, TherapistCreateRequest, TherapistUpdateRequest } from "../../types/admin";
import { TherapistAppointmentsPanel } from "./TherapistAppointmentsPanel";
import { TherapistDialog } from "./TherapistDialog";

type DialogState = { mode: "create"; record: null } | { mode: "edit"; record: Therapist };
type SnackbarState = { open: boolean; severity: "success" | "error"; message: string };

export function TherapistsPage() {
  const { currentUser } = useAuth();
  const isDemo = currentUser?.isDemo === true;
  const [rows, setRows] = useState<Therapist[]>([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);
  const [searchInput, setSearchInput] = useState("");
  const [search, setSearch] = useState("");
  const [paginationModel, setPaginationModel] = useState<GridPaginationModel>({ page: 0, pageSize: 10 });
  const [selectedTherapistId, setSelectedTherapistId] = useState<number | null>(null);
  const [dialogState, setDialogState] = useState<DialogState | null>(null);
  const [deleteTarget, setDeleteTarget] = useState<Therapist | null>(null);
  const [dialogError, setDialogError] = useState<string | null>(null);
  const [deleteError, setDeleteError] = useState<string | null>(null);
  const [submitting, setSubmitting] = useState(false);
  const [snackbar, setSnackbar] = useState<SnackbarState>({ open: false, severity: "success", message: "" });

  useEffect(() => {
    const id = window.setTimeout(() => setSearch(searchInput.trim().toLowerCase()), 300);
    return () => window.clearTimeout(id);
  }, [searchInput]);

  async function refresh() {
    try {
      setLoading(true);
      setError(null);
      const result = await listTherapists();
      setRows(result.rows);
    } catch (loadError) {
      setError(loadError instanceof Error ? loadError.message : "Unable to load therapists.");
    } finally {
      setLoading(false);
    }
  }

  useEffect(() => { void refresh(); }, []);

  useEffect(() => {
    if (rows.length === 0) { setSelectedTherapistId(null); return; }
    const stillPresent = selectedTherapistId !== null && rows.some((r) => r.id === selectedTherapistId);
    if (!stillPresent) setSelectedTherapistId(rows[0].id);
  }, [rows, selectedTherapistId]);

  const filteredRows = useMemo(() => {
    if (!search) return rows;
    return rows.filter((r) =>
      [r.therapistName, r.type].some((v) => String(v ?? "").toLowerCase().includes(search)),
    );
  }, [rows, search]);

  const selectedTherapist = useMemo(() => rows.find((r) => r.id === selectedTherapistId) ?? null, [rows, selectedTherapistId]);

  async function handleSubmit(payload: TherapistCreateRequest | TherapistUpdateRequest) {
    setDialogError(null);
    setSubmitting(true);
    try {
      if (dialogState?.mode === "create") {
        await createTherapist(payload as TherapistCreateRequest);
        setSnackbar({ open: true, severity: "success", message: "Therapist created." });
      } else if (dialogState?.mode === "edit") {
        await updateTherapist(dialogState.record.id, payload as TherapistUpdateRequest);
        setSnackbar({ open: true, severity: "success", message: "Therapist updated." });
      }
      await refresh();
      setDialogState(null);
    } catch (submitError) {
      const message = submitError instanceof Error ? submitError.message : "Unable to save therapist.";
      setDialogError(message);
      setSnackbar({ open: true, severity: "error", message });
    } finally {
      setSubmitting(false);
    }
  }

  async function handleDelete() {
    if (!deleteTarget) return;
    setDeleteError(null);
    setSubmitting(true);
    try {
      await removeTherapist(deleteTarget.id);
      await refresh();
      setDeleteTarget(null);
      setSnackbar({ open: true, severity: "success", message: "Therapist deleted." });
    } catch (deleteRequestError) {
      const message = deleteRequestError instanceof Error ? deleteRequestError.message : "Unable to delete therapist.";
      setDeleteError(message);
      setSnackbar({ open: true, severity: "error", message });
    } finally {
      setSubmitting(false);
    }
  }

  const columns: GridColDef<Therapist>[] = [
    { field: "therapistName", headerName: "Therapist Name", flex: 1.3, minWidth: 200 },
    { field: "type", headerName: "Type", flex: 1, minWidth: 180 },
    {
      field: "actions", headerName: "Actions", flex: 0.6, minWidth: 120, sortable: false, filterable: false,
      renderCell: (params: GridRenderCellParams<Therapist>) => (
        <Stack direction="row" spacing={0.5}>
          <IconButton color="primary" disabled={isDemo} onClick={(e) => { e.stopPropagation(); setDialogError(null); setDialogState({ mode: "edit", record: params.row }); }}><EditOutlinedIcon fontSize="small" /></IconButton>
          <IconButton color="error" disabled={isDemo} onClick={(e) => { e.stopPropagation(); setDeleteError(null); setDeleteTarget(params.row); }}><DeleteOutlineIcon fontSize="small" /></IconButton>
        </Stack>
      ),
    },
  ];

  return (
    <>
      <Stack spacing={3}>
        <Stack direction={{ xs: "column", md: "row" }} spacing={2} alignItems={{ md: "center" }} justifyContent="space-between">
          <Box>
            <Typography variant="h4" sx={{ color: "#f8fafc", fontWeight: 700 }}>Therapists</Typography>
            <Typography sx={{ color: adminColors.textSecondary, mt: 1 }}>Manage therapist names, role types, and view their appointment schedules.</Typography>
          </Box>
          <Button variant="contained" disabled={isDemo} onClick={() => { setDialogError(null); setDialogState({ mode: "create", record: null }); }}>Add Therapist</Button>
        </Stack>

        <Stack direction={{ xs: "column", lg: "row" }} spacing={3} alignItems="stretch">
          <Paper elevation={0} sx={{ p: 2, borderRadius: 4, bgcolor: adminColors.panelBg, border: `1px solid ${adminColors.border}`, flex: { lg: "0 0 42%" }, minWidth: 0 }}>
            <Stack spacing={2}>
              <TextField
                value={searchInput}
                onChange={(e) => setSearchInput(e.target.value)}
                placeholder="Search by therapist name or type"
                fullWidth
                sx={adminTextFieldSx}
                InputProps={{ startAdornment: <InputAdornment position="start"><SearchIcon fontSize="small" /></InputAdornment> }}
              />
              {error && <Alert severity="error">{error}</Alert>}
              <Box sx={{ height: 560 }}>
                <DataGrid
                  rows={filteredRows}
                  getRowId={(row) => row.id}
                  columns={columns}
                  loading={loading}
                  pagination
                  paginationMode="client"
                  rowCount={filteredRows.length}
                  paginationModel={paginationModel}
                  onPaginationModelChange={setPaginationModel}
                  pageSizeOptions={[5, 10, 20, 50]}
                  disableRowSelectionOnClick
                  onRowClick={(params) => setSelectedTherapistId(params.row.id)}
                  getRowClassName={(params) => params.row.id === selectedTherapistId ? "Mui-selected" : ""}
                  sx={adminDataGridSx}
                />
              </Box>
            </Stack>
          </Paper>

          <Paper elevation={0} sx={{ p: 2.5, borderRadius: 4, bgcolor: adminColors.panelBg, border: `1px solid ${adminColors.border}`, flex: 1, minWidth: 0 }}>
            <TherapistAppointmentsPanel therapist={selectedTherapist} />
          </Paper>
        </Stack>
      </Stack>

      {dialogState && (
        <TherapistDialog
          key={dialogState.mode === "create" ? "create" : `edit-${dialogState.record.id}`}
          open
          mode={dialogState.mode}
          initialValue={dialogState.record}
          submitting={submitting}
          error={dialogError}
          onClose={() => !submitting && setDialogState(null)}
          onSubmit={handleSubmit}
        />
      )}
      <ConfirmDeleteDialog
        open={deleteTarget !== null}
        description={deleteTarget ? `Delete therapist "${deleteTarget.therapistName}"? This action cannot be undone.` : ""}
        error={deleteError}
        loading={submitting}
        onClose={() => !submitting && setDeleteTarget(null)}
        onConfirm={() => void handleDelete()}
      />
      <Snackbar open={snackbar.open} autoHideDuration={4000} onClose={() => setSnackbar((c) => ({ ...c, open: false }))} anchorOrigin={{ vertical: "bottom", horizontal: "right" }}>
        <Alert onClose={() => setSnackbar((c) => ({ ...c, open: false }))} severity={snackbar.severity} variant="filled">{snackbar.message}</Alert>
      </Snackbar>
    </>
  );
}
