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
import { adminColors, adminSelectProps, adminTextFieldSx } from "../../components/admin/adminStyles";
import type { BodyRegion, CaseCreateRequest, CaseSummary, CaseUpdateRequest, Patient } from "../../types/admin";

type CaseDialogProps = {
  open: boolean;
  mode: "create" | "edit";
  initialValue?: CaseSummary | null;
  patients: Patient[];
  bodyRegions: BodyRegion[];
  fixedPatient?: Patient | null;
  submitting?: boolean;
  error?: string | null;
  onClose: () => void;
  onSubmit: (payload: CaseCreateRequest | CaseUpdateRequest) => Promise<void>;
};

export function CaseDialog({ open, mode, initialValue, patients, bodyRegions, fixedPatient = null, submitting = false, error, onClose, onSubmit }: CaseDialogProps) {
  const [patientId, setPatientId] = useState(String(fixedPatient?.id ?? initialValue?.patientId ?? ""));
  const [bodyRegionId, setBodyRegionId] = useState(String(initialValue?.bodyRegionId ?? ""));

  async function handleSubmit(event: React.FormEvent<HTMLFormElement>) {
    event.preventDefault();
    if (mode === "create") {
      await onSubmit({ patientId: Number(patientId), bodyRegionId: Number(bodyRegionId) });
      return;
    }
    await onSubmit({ bodyRegionId: Number(bodyRegionId) });
  }

  return (
    <Dialog
      open={open}
      onClose={submitting ? undefined : onClose}
      fullWidth
      maxWidth="sm"
      PaperProps={{ sx: { backgroundColor: adminColors.panelElevated, color: adminColors.textStrong, border: `1px solid ${adminColors.border}`, backgroundImage: "none" } }}
    >
      <DialogTitle sx={{ color: adminColors.textStrong }}>{mode === "create" ? "Add Case" : "Edit Case"}</DialogTitle>
      <DialogContent>
        <Stack component="form" spacing={2.5} sx={{ pt: 1 }} onSubmit={handleSubmit}>
          {error && <Alert severity="error">{error}</Alert>}
          {fixedPatient ? (
            <TextField label="Patient" value={fixedPatient.displayName} fullWidth disabled helperText="Cases are created from the selected patient." sx={adminTextFieldSx} />
          ) : (
            <TextField select label="Patient" value={patientId} onChange={(e) => setPatientId(e.target.value)} required fullWidth disabled={mode === "edit"} helperText={mode === "edit" ? "Patient cannot be changed after the case is created." : undefined} sx={adminTextFieldSx} SelectProps={adminSelectProps}>
              {patients.map((patient) => <MenuItem key={patient.id} value={String(patient.id)}>{patient.displayName}</MenuItem>)}
            </TextField>
          )}
          <TextField select label="Body Region" value={bodyRegionId} onChange={(e) => setBodyRegionId(e.target.value)} required fullWidth sx={adminTextFieldSx} SelectProps={adminSelectProps}>
            {bodyRegions.map((bodyRegion) => <MenuItem key={bodyRegion.id} value={String(bodyRegion.id)}>{bodyRegion.displayName}</MenuItem>)}
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
