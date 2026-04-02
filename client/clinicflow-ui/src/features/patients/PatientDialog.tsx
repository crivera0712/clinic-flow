import { useState } from "react";
import Alert from "@mui/material/Alert";
import Button from "@mui/material/Button";
import Dialog from "@mui/material/Dialog";
import DialogActions from "@mui/material/DialogActions";
import DialogContent from "@mui/material/DialogContent";
import DialogTitle from "@mui/material/DialogTitle";
import Stack from "@mui/material/Stack";
import TextField from "@mui/material/TextField";
import { adminColors, adminTextFieldSx } from "../../components/admin/adminStyles";
import type { Patient, PatientCreateRequest, PatientUpdateRequest } from "../../types/admin";

type PatientDialogProps = {
  open: boolean;
  mode: "create" | "edit";
  initialValue?: Patient | null;
  submitting?: boolean;
  error?: string | null;
  onClose: () => void;
  onSubmit: (payload: PatientCreateRequest | PatientUpdateRequest) => Promise<void>;
};

export function PatientDialog({ open, mode, initialValue, submitting = false, error, onClose, onSubmit }: PatientDialogProps) {
  const [firstName, setFirstName] = useState(initialValue?.firstName ?? "");
  const [lastName, setLastName] = useState(initialValue?.lastName ?? "");

  async function handleSubmit(event: React.FormEvent<HTMLFormElement>) {
    event.preventDefault();
    await onSubmit({ firstName: firstName.trim(), lastName: lastName.trim() });
  }

  return (
    <Dialog
      open={open}
      onClose={submitting ? undefined : onClose}
      fullWidth
      maxWidth="sm"
      PaperProps={{ sx: { backgroundColor: adminColors.panelElevated, color: adminColors.textStrong, border: `1px solid ${adminColors.border}`, backgroundImage: "none" } }}
    >
      <DialogTitle sx={{ color: adminColors.textStrong }}>{mode === "create" ? "Add Patient" : "Edit Patient"}</DialogTitle>
      <DialogContent>
        <Stack component="form" spacing={2.5} sx={{ pt: 1 }} onSubmit={handleSubmit}>
          {error && <Alert severity="error">{error}</Alert>}
          <TextField label="First Name" value={firstName} onChange={(e) => setFirstName(e.target.value)} required autoFocus fullWidth sx={adminTextFieldSx} />
          <TextField label="Last Name" value={lastName} onChange={(e) => setLastName(e.target.value)} required fullWidth sx={adminTextFieldSx} />
          <DialogActions sx={{ px: 0, pb: 0 }}>
            <Button onClick={onClose} disabled={submitting}>Cancel</Button>
            <Button type="submit" variant="contained" disabled={submitting}>{mode === "create" ? "Create" : "Save Changes"}</Button>
          </DialogActions>
        </Stack>
      </DialogContent>
    </Dialog>
  );
}
