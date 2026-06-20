import { useEffect, useState } from "react";
import Alert from "@mui/material/Alert";
import Box from "@mui/material/Box";
import Button from "@mui/material/Button";
import Dialog from "@mui/material/Dialog";
import DialogActions from "@mui/material/DialogActions";
import DialogContent from "@mui/material/DialogContent";
import DialogTitle from "@mui/material/DialogTitle";
import Paper from "@mui/material/Paper";
import Snackbar from "@mui/material/Snackbar";
import Stack from "@mui/material/Stack";
import TextField from "@mui/material/TextField";
import Typography from "@mui/material/Typography";
import { DataGrid, type GridColDef } from "@mui/x-data-grid";
import { adminColors, adminDataGridSx, adminTextFieldSx } from "../../components/admin/adminStyles";
import { createPatient, listPatients } from "../../services/patientService";
import type { Patient } from "../../types/patient";

export function PatientsDirectory() {
  const [rows, setRows] = useState<Patient[]>([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);
  const [open, setOpen] = useState(false);
  const [firstName, setFirstName] = useState("");
  const [lastName, setLastName] = useState("");
  const [saving, setSaving] = useState(false);
  const [snackbar, setSnackbar] = useState<{ open: boolean; message: string }>({ open: false, message: "" });

  async function refresh() {
    try {
      setLoading(true);
      setError(null);
      const result = await listPatients({ page: 0, size: 200 });
      setRows(result.rows);
    } catch (loadError) {
      setError(loadError instanceof Error ? loadError.message : "Unable to load patients.");
    } finally {
      setLoading(false);
    }
  }

  useEffect(() => {
    void refresh();
  }, []);

  async function handleCreate() {
    setSaving(true);
    try {
      await createPatient({ firstName: firstName.trim(), lastName: lastName.trim() });
      setOpen(false);
      setFirstName("");
      setLastName("");
      await refresh();
      setSnackbar({ open: true, message: "Patient added." });
    } catch (createError) {
      setSnackbar({ open: true, message: createError instanceof Error ? createError.message : "Unable to add patient." });
    } finally {
      setSaving(false);
    }
  }

  const columns: GridColDef<Patient>[] = [
    { field: "firstName", headerName: "First Name", flex: 1, minWidth: 160 },
    { field: "lastName", headerName: "Last Name", flex: 1, minWidth: 160 },
  ];

  return (
    <Stack spacing={3}>
      <Stack direction={{ xs: "column", md: "row" }} spacing={2} justifyContent="space-between" alignItems={{ md: "center" }}>
        <Box>
          <Typography variant="h4" sx={{ color: "#f8fafc", fontWeight: 700 }}>Patients</Typography>
          <Typography sx={{ color: adminColors.textSecondary, mt: 1 }}>The clinic's patient directory.</Typography>
        </Box>
        <Button variant="contained" onClick={() => setOpen(true)}>Add Patient</Button>
      </Stack>
      <Paper elevation={0} sx={{ p: 2, borderRadius: 4, bgcolor: adminColors.panelBg, border: `1px solid ${adminColors.border}` }}>
        {error && <Alert severity="error" sx={{ mb: 2 }}>{error}</Alert>}
        <Box sx={{ height: 560 }}>
          <DataGrid rows={rows} columns={columns} loading={loading} disableRowSelectionOnClick sx={adminDataGridSx} />
        </Box>
      </Paper>

      <Dialog open={open} onClose={saving ? undefined : () => setOpen(false)} fullWidth maxWidth="xs" PaperProps={{ sx: { bgcolor: adminColors.panelElevated, color: adminColors.textStrong, border: `1px solid ${adminColors.border}`, backgroundImage: "none" } }}>
        <DialogTitle sx={{ color: adminColors.textStrong }}>Add Patient</DialogTitle>
        <DialogContent>
          <Stack component="form" spacing={2.5} sx={{ pt: 1 }} onSubmit={(e) => { e.preventDefault(); void handleCreate(); }}>
            <TextField label="First Name" value={firstName} onChange={(e) => setFirstName(e.target.value)} required autoFocus fullWidth sx={adminTextFieldSx} />
            <TextField label="Last Name" value={lastName} onChange={(e) => setLastName(e.target.value)} required fullWidth sx={adminTextFieldSx} />
            <DialogActions sx={{ px: 0, pb: 0 }}>
              <Button onClick={() => setOpen(false)} disabled={saving}>Cancel</Button>
              <Button type="submit" variant="contained" disabled={saving}>Create</Button>
            </DialogActions>
          </Stack>
        </DialogContent>
      </Dialog>

      <Snackbar open={snackbar.open} autoHideDuration={4000} onClose={() => setSnackbar({ open: false, message: "" })} anchorOrigin={{ vertical: "bottom", horizontal: "right" }}>
        <Alert onClose={() => setSnackbar({ open: false, message: "" })} severity="success" variant="filled">{snackbar.message}</Alert>
      </Snackbar>
    </Stack>
  );
}
