import { useState } from "react";
import Alert from "@mui/material/Alert";
import Button from "@mui/material/Button";
import Dialog from "@mui/material/Dialog";
import DialogActions from "@mui/material/DialogActions";
import DialogContent from "@mui/material/DialogContent";
import DialogTitle from "@mui/material/DialogTitle";
import MenuItem from "@mui/material/MenuItem";
import Stack from "@mui/material/Stack";
import TextField from "@mui/material/TextField";
import { adminColors, adminTextFieldSx } from "../../components/admin/adminStyles";
import type { Therapist, TherapistCreateRequest, TherapistTypeValue, TherapistUpdateRequest } from "../../types/admin";
import { therapistTypeOptions } from "../../types/admin";

function getTypeValue(label?: string | null): TherapistTypeValue {
  return therapistTypeOptions.find((option) => option.label === label)?.value ?? "PHYSICAL_THERAPIST";
}

type Props = {
  open: boolean;
  mode: "create" | "edit";
  initialValue?: Therapist | null;
  submitting?: boolean;
  error?: string | null;
  onClose: () => void;
  onSubmit: (payload: TherapistCreateRequest | TherapistUpdateRequest) => Promise<void>;
};

export function TherapistDialog({ open, mode, initialValue, submitting = false, error, onClose, onSubmit }: Props) {
  const [therapistName, setTherapistName] = useState(initialValue?.therapistName ?? "");
  const [type, setType] = useState<TherapistTypeValue>(getTypeValue(initialValue?.type));

  async function handleSubmit(event: React.FormEvent<HTMLFormElement>) {
    event.preventDefault();
    if (mode === "create") {
      await onSubmit({ therapistName: therapistName.trim(), type });
      return;
    }
    await onSubmit({ therapistName: therapistName.trim(), therapistType: type });
  }

  return (
    <Dialog open={open} onClose={submitting ? undefined : onClose} fullWidth maxWidth="sm" PaperProps={{ sx: { backgroundColor: adminColors.panelElevated, color: adminColors.textStrong, border: `1px solid ${adminColors.border}`, backgroundImage: "none" } }}>
      <DialogTitle sx={{ color: adminColors.textStrong }}>{mode === "create" ? "Add Therapist" : "Edit Therapist"}</DialogTitle>
      <DialogContent>
        <Stack component="form" spacing={2.5} sx={{ pt: 1 }} onSubmit={handleSubmit}>
          {error && <Alert severity="error">{error}</Alert>}
          <TextField label="Therapist Name" value={therapistName} onChange={(e) => setTherapistName(e.target.value)} required fullWidth autoFocus sx={adminTextFieldSx} />
          <TextField select label="Therapist Type" value={type} onChange={(e) => setType(e.target.value as TherapistTypeValue)} required fullWidth sx={adminTextFieldSx}>
            {therapistTypeOptions.map((option) => <MenuItem key={option.value} value={option.value}>{option.label}</MenuItem>)}
          </TextField>
          <DialogActions sx={{ px: 0, pb: 0 }}>
            <Button onClick={onClose} disabled={submitting}>Cancel</Button>
            <Button type="submit" variant="contained" disabled={submitting}>{mode === "create" ? "Create" : "Save Changes"}</Button>
          </DialogActions>
        </Stack>
      </DialogContent>
    </Dialog>
  );
}
