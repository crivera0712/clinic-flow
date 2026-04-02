import EditOutlinedIcon from "@mui/icons-material/EditOutlined";
import SearchIcon from "@mui/icons-material/Search";
import Alert from "@mui/material/Alert";
import Box from "@mui/material/Box";
import Button from "@mui/material/Button";
import Chip from "@mui/material/Chip";
import IconButton from "@mui/material/IconButton";
import InputAdornment from "@mui/material/InputAdornment";
import Paper from "@mui/material/Paper";
import Snackbar from "@mui/material/Snackbar";
import Stack from "@mui/material/Stack";
import TextField from "@mui/material/TextField";
import Typography from "@mui/material/Typography";
import { DataGrid, type GridColDef, type GridPaginationModel, type GridRenderCellParams } from "@mui/x-data-grid";
import { useEffect, useMemo, useState } from "react";
import { adminColors, adminDataGridSx, adminTextFieldSx } from "../../components/admin/adminStyles";
import { createBodyRegion, listBodyRegions, updateBodyRegion } from "../../services/bodyRegionService";
import type { BodyRegion, BodyRegionCreateRequest, BodyRegionUpdateRequest } from "../../types/admin";
import { BodyRegionDialog } from "./BodyRegionDialog";

type DialogState = { mode: "create"; record: null } | { mode: "edit"; record: BodyRegion };
type SnackbarState = { open: boolean; severity: "success" | "error"; message: string };

export function BodyRegionsPage() {
  const [rows, setRows] = useState<BodyRegion[]>([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);
  const [searchInput, setSearchInput] = useState("");
  const [search, setSearch] = useState("");
  const [paginationModel, setPaginationModel] = useState<GridPaginationModel>({ page: 0, pageSize: 10 });
  const [dialogState, setDialogState] = useState<DialogState | null>(null);
  const [dialogError, setDialogError] = useState<string | null>(null);
  const [submitting, setSubmitting] = useState(false);
  const [snackbar, setSnackbar] = useState<SnackbarState>({ open: false, severity: "success", message: "" });

  useEffect(() => {
    const timeoutId = window.setTimeout(() => setSearch(searchInput.trim().toLowerCase()), 300);
    return () => window.clearTimeout(timeoutId);
  }, [searchInput]);

  async function refresh() {
    try {
      setLoading(true);
      setError(null);
      const result = await listBodyRegions();
      setRows(result.rows);
    } catch (loadError) {
      setError(loadError instanceof Error ? loadError.message : "Unable to load body regions.");
    } finally {
      setLoading(false);
    }
  }

  useEffect(() => { void refresh(); }, []);

  const filteredRows = useMemo(() => {
    if (!search) return rows;
    return rows.filter((row) => [row.code, row.displayName].some((value) => value.toLowerCase().includes(search)));
  }, [rows, search]);

  async function handleSubmit(payload: BodyRegionCreateRequest | BodyRegionUpdateRequest) {
    setDialogError(null);
    setSubmitting(true);
    try {
      if (dialogState?.mode === "create") {
        await createBodyRegion(payload as BodyRegionCreateRequest);
        setSnackbar({ open: true, severity: "success", message: "Body region created." });
      } else if (dialogState?.mode === "edit") {
        await updateBodyRegion(dialogState.record.id, payload as BodyRegionUpdateRequest);
        setSnackbar({ open: true, severity: "success", message: "Body region updated." });
      }
      await refresh();
      setDialogState(null);
    } catch (submitError) {
      const message = submitError instanceof Error ? submitError.message : "Unable to save body region.";
      setDialogError(message);
      setSnackbar({ open: true, severity: "error", message });
    } finally {
      setSubmitting(false);
    }
  }

  const columns: GridColDef<BodyRegion>[] = [
    { field: "code", headerName: "Code", flex: 1, minWidth: 140 },
    { field: "displayName", headerName: "Display Name", flex: 1.5, minWidth: 200 },
    {
      field: "isActive", headerName: "Status", flex: 0.8, minWidth: 140, renderCell: (params: GridRenderCellParams<BodyRegion, boolean>) => (
        <Chip size="small" label={params.value ? "Active" : "Inactive"} color={params.value ? "success" : "default"} variant={params.value ? "filled" : "outlined"} />
      ),
    },
    {
      field: "actions", headerName: "Actions", flex: 0.6, minWidth: 100, sortable: false, filterable: false, renderCell: (params: GridRenderCellParams<BodyRegion>) => (
        <IconButton color="primary" onClick={() => { setDialogError(null); setDialogState({ mode: "edit", record: params.row }); }}>
          <EditOutlinedIcon fontSize="small" />
        </IconButton>
      ),
    },
  ];

  return (
    <>
      <Stack spacing={3}>
        <Stack direction={{ xs: "column", md: "row" }} spacing={2} alignItems={{ md: "center" }} justifyContent="space-between">
          <Box>
            <Typography variant="h4" sx={{ color: "#f8fafc", fontWeight: 700 }}>Body Regions</Typography>
            <Typography sx={{ color: adminColors.textSecondary, mt: 1 }}>Manage body region codes and activation state.</Typography>
          </Box>
          <Button variant="contained" onClick={() => { setDialogError(null); setDialogState({ mode: "create", record: null }); }}>Add Body Region</Button>
        </Stack>
        <Paper elevation={0} sx={{ p: 2, borderRadius: 4, bgcolor: adminColors.panelBg, border: `1px solid ${adminColors.border}` }}>
          <Stack spacing={2}>
            <TextField value={searchInput} onChange={(e) => setSearchInput(e.target.value)} placeholder="Search by code or display name" fullWidth sx={{ maxWidth: 420, ...adminTextFieldSx }} InputProps={{ startAdornment: <InputAdornment position="start"><SearchIcon fontSize="small" /></InputAdornment> }} />
            {error && <Alert severity="error">{error}</Alert>}
            <Box sx={{ height: 620 }}>
              <DataGrid rows={filteredRows} columns={columns} loading={loading} pagination paginationMode="client" rowCount={filteredRows.length} paginationModel={paginationModel} onPaginationModelChange={setPaginationModel} pageSizeOptions={[5, 10, 20, 50]} disableRowSelectionOnClick sx={adminDataGridSx} />
            </Box>
          </Stack>
        </Paper>
      </Stack>
      {dialogState && <BodyRegionDialog key={dialogState.mode === "create" ? "create" : `edit-${dialogState.record.id}`} open mode={dialogState.mode} initialValue={dialogState.record} submitting={submitting} error={dialogError} onClose={() => !submitting && setDialogState(null)} onSubmit={handleSubmit} />}
      <Snackbar open={snackbar.open} autoHideDuration={4000} onClose={() => setSnackbar((c) => ({ ...c, open: false }))} anchorOrigin={{ vertical: "bottom", horizontal: "right" }}>
        <Alert onClose={() => setSnackbar((c) => ({ ...c, open: false }))} severity={snackbar.severity} variant="filled">{snackbar.message}</Alert>
      </Snackbar>
    </>
  );
}
