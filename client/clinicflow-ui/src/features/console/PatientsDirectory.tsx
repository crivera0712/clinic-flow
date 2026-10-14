import { useEffect, useState } from "react";
import PersonAddAltRoundedIcon from "@mui/icons-material/PersonAddAltRounded";
import PeopleAltRoundedIcon from "@mui/icons-material/PeopleAltRounded";
import Alert from "@mui/material/Alert";
import Box from "@mui/material/Box";
import Button from "@mui/material/Button";
import Dialog from "@mui/material/Dialog";
import DialogActions from "@mui/material/DialogActions";
import DialogContent from "@mui/material/DialogContent";
import DialogTitle from "@mui/material/DialogTitle";
import Stack from "@mui/material/Stack";
import TextField from "@mui/material/TextField";
import Typography from "@mui/material/Typography";
import { DataGrid, type GridColDef } from "@mui/x-data-grid";
import { PageHeader } from "../../components/ui/PageHeader";
import { SectionCard } from "../../components/ui/SectionCard";
import { useToast } from "../../components/ui/useToast";
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
  const toast = useToast();

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
      toast.success("Patient added.");
    } catch (createError) {
      toast.error(createError instanceof Error ? createError.message : "Unable to add patient.");
    } finally {
      setSaving(false);
    }
  }

  const columns: GridColDef<Patient>[] = [
    { field: "firstName", headerName: "First Name", flex: 1, minWidth: 160 },
    { field: "lastName", headerName: "Last Name", flex: 1, minWidth: 160 },
  ];

  return (
    <Stack spacing={3.5}>
      <PageHeader
        eyebrow="Clinic directory"
        title="Patients"
        description="A focused directory for everyone currently receiving care at the clinic."
        onRefresh={() => void refresh()}
        refreshing={loading}
        action={
          <Button
            variant="contained"
            startIcon={<PersonAddAltRoundedIcon />}
            onClick={() => setOpen(true)}
          >
            Add patient
          </Button>
        }
      />
      <SectionCard
        icon={<PeopleAltRoundedIcon />}
        title="Patient directory"
        subtitle={
          loading ? "Loading patients…" : `${rows.length} patient${rows.length === 1 ? "" : "s"}`
        }
        disablePadding
      >
        {error && <Alert severity="error">{error}</Alert>}
        <Box sx={{ height: { xs: 460, md: 580 } }}>
          <DataGrid
            rows={rows}
            columns={columns}
            loading={loading}
            disableRowSelectionOnClick
            rowHeight={58}
            columnHeaderHeight={50}
            pageSizeOptions={[10, 25, 50]}
            initialState={{ pagination: { paginationModel: { page: 0, pageSize: 10 } } }}
            slotProps={{ loadingOverlay: { variant: "skeleton", noRowsVariant: "skeleton" } }}
          />
        </Box>
      </SectionCard>

      <Dialog
        open={open}
        onClose={saving ? undefined : () => setOpen(false)}
        fullWidth
        maxWidth="xs"
      >
        <DialogTitle>Add patient</DialogTitle>
        <DialogContent>
          <Typography color="text.secondary" sx={{ mb: 2.5 }}>
            Add a patient to the clinic directory.
          </Typography>
          <Stack
            component="form"
            spacing={2.5}
            sx={{ pt: 1 }}
            onSubmit={(e) => {
              e.preventDefault();
              void handleCreate();
            }}
          >
            <TextField
              label="First Name"
              value={firstName}
              onChange={(e) => setFirstName(e.target.value)}
              required
              autoFocus
              fullWidth
            />
            <TextField
              label="Last Name"
              value={lastName}
              onChange={(e) => setLastName(e.target.value)}
              required
              fullWidth
            />
            <DialogActions sx={{ px: 0, pb: 0 }}>
              <Button onClick={() => setOpen(false)} disabled={saving}>
                Cancel
              </Button>
              <Button type="submit" variant="contained" disabled={saving}>
                Add patient
              </Button>
            </DialogActions>
          </Stack>
        </DialogContent>
      </Dialog>
    </Stack>
  );
}
