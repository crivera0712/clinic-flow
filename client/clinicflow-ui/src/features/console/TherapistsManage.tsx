import { useEffect, useState } from "react";
import Alert from "@mui/material/Alert";
import Box from "@mui/material/Box";
import Button from "@mui/material/Button";
import Paper from "@mui/material/Paper";
import Snackbar from "@mui/material/Snackbar";
import Stack from "@mui/material/Stack";
import TextField from "@mui/material/TextField";
import Typography from "@mui/material/Typography";
import { adminColors, adminTextFieldSx } from "../../components/admin/adminStyles";
import { ConfirmDeleteDialog } from "../../components/admin/ConfirmDeleteDialog";
import { createTherapist, listTherapists, removeTherapist } from "../../services/therapistService";
import type { Therapist } from "../../types/therapist";

export function TherapistsManage() {
  const [therapists, setTherapists] = useState<Therapist[]>([]);
  const [error, setError] = useState<string | null>(null);
  const [name, setName] = useState("");
  const [adding, setAdding] = useState(false);
  const [deleteTarget, setDeleteTarget] = useState<Therapist | null>(null);
  const [snackbar, setSnackbar] = useState<{ open: boolean; message: string }>({ open: false, message: "" });

  async function refresh() {
    try {
      setError(null);
      setTherapists(await listTherapists());
    } catch (loadError) {
      setError(loadError instanceof Error ? loadError.message : "Unable to load therapists.");
    }
  }

  useEffect(() => {
    void refresh();
  }, []);

  async function handleAdd() {
    if (!name.trim()) return;
    setAdding(true);
    try {
      await createTherapist({ name: name.trim() });
      setName("");
      await refresh();
      setSnackbar({ open: true, message: "Therapist added." });
    } catch (addError) {
      setSnackbar({ open: true, message: addError instanceof Error ? addError.message : "Unable to add therapist." });
    } finally {
      setAdding(false);
    }
  }

  async function handleDelete() {
    if (!deleteTarget) return;
    try {
      await removeTherapist(deleteTarget.id);
      setDeleteTarget(null);
      await refresh();
      setSnackbar({ open: true, message: "Therapist removed." });
    } catch (deleteError) {
      setSnackbar({ open: true, message: deleteError instanceof Error ? deleteError.message : "Unable to remove therapist." });
    }
  }

  return (
    <Stack spacing={3}>
      <Box>
        <Typography variant="h4" sx={{ color: "#f8fafc", fontWeight: 700 }}>Therapists</Typography>
        <Typography sx={{ color: adminColors.textSecondary, mt: 1 }}>Manage the clinic's therapists.</Typography>
      </Box>

      <Paper elevation={0} sx={{ p: 2.5, borderRadius: 4, bgcolor: adminColors.panelBg, border: `1px solid ${adminColors.border}` }}>
        <Stack direction="row" spacing={2} component="form" onSubmit={(e) => { e.preventDefault(); void handleAdd(); }}>
          <TextField label="Therapist name" value={name} onChange={(e) => setName(e.target.value)} sx={{ flex: 1, ...adminTextFieldSx }} />
          <Button type="submit" variant="contained" disabled={adding}>Add</Button>
        </Stack>
      </Paper>

      <Paper elevation={0} sx={{ p: 2.5, borderRadius: 4, bgcolor: adminColors.panelBg, border: `1px solid ${adminColors.border}` }}>
        {error && <Alert severity="error" sx={{ mb: 2 }}>{error}</Alert>}
        <Stack divider={<Box sx={{ borderBottom: `1px solid ${adminColors.borderMuted}` }} />}>
          {therapists.length === 0 && (
            <Typography sx={{ color: adminColors.textSecondary, py: 2 }}>No therapists yet.</Typography>
          )}
          {therapists.map((t) => (
            <Stack key={t.id} direction="row" alignItems="center" justifyContent="space-between" sx={{ py: 1.5 }}>
              <Typography sx={{ color: adminColors.textStrong, fontWeight: 600 }}>{t.name}</Typography>
              <Button size="small" color="error" onClick={() => setDeleteTarget(t)}>Remove</Button>
            </Stack>
          ))}
        </Stack>
      </Paper>

      <ConfirmDeleteDialog
        open={deleteTarget !== null}
        description={deleteTarget ? `Remove ${deleteTarget.name}?` : ""}
        onClose={() => setDeleteTarget(null)}
        onConfirm={() => void handleDelete()}
      />

      <Snackbar open={snackbar.open} autoHideDuration={4000} onClose={() => setSnackbar({ open: false, message: "" })} anchorOrigin={{ vertical: "bottom", horizontal: "right" }}>
        <Alert onClose={() => setSnackbar({ open: false, message: "" })} severity="success" variant="filled">{snackbar.message}</Alert>
      </Snackbar>
    </Stack>
  );
}
