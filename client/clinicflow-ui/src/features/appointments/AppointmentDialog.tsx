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
import type {
  AppointmentCreateRequest,
  AppointmentRecord,
  AppointmentStatusValue,
  AppointmentTypeValue,
  AppointmentUpdateRequest,
  CaseSummary,
  Patient,
} from "../../types/admin";
import { appointmentStatusOptions, appointmentTypeOptions } from "../../types/admin";

type SelectOption = { value: number; label: string };

type AppointmentDialogProps = {
  open: boolean;
  mode: "create" | "edit";
  initialValue?: AppointmentRecord | null;
  patients: Patient[];
  fixedPatient?: Patient | null;
  fixedCase?: CaseSummary | null;
  initialPatientId?: number | null;
  caseOptions: SelectOption[];
  therapistOptions: SelectOption[];
  caseOptionsLoading?: boolean;
  submitting?: boolean;
  error?: string | null;
  caseHelperText?: string;
  onClose: () => void;
  onPatientChange: (patientId: number | null) => void;
  onSubmit: (payload: AppointmentCreateRequest | AppointmentUpdateRequest) => Promise<void>;
};

function toDateTimeInputValue(value?: string | null) {
  if (!value) return "";
  return value.replace(" ", "T").slice(0, 16);
}

export function AppointmentDialog({
  open,
  mode,
  initialValue,
  patients,
  fixedPatient = null,
  fixedCase = null,
  initialPatientId = null,
  caseOptions,
  therapistOptions,
  caseOptionsLoading = false,
  submitting = false,
  error,
  caseHelperText,
  onClose,
  onPatientChange,
  onSubmit,
}: AppointmentDialogProps) {
  const [scheduledAt, setScheduledAt] = useState(toDateTimeInputValue(initialValue?.scheduledAt));
  const [patientId, setPatientId] = useState(String(fixedPatient?.id ?? initialPatientId ?? ""));
  const [caseId, setCaseId] = useState(String(fixedCase?.id ?? initialValue?.caseId ?? ""));
  const [therapistId, setTherapistId] = useState(String(initialValue?.therapistId ?? ""));
  const [status, setStatus] = useState<AppointmentStatusValue>(initialValue?.status ?? "SCHEDULED");
  const [type, setType] = useState<AppointmentTypeValue>(initialValue?.type ?? "EVALUATION");

  function handlePatientSelection(nextValue: string) {
    setPatientId(nextValue);
    setCaseId("");
    onPatientChange(nextValue ? Number(nextValue) : null);
  }

  async function handleSubmit(event: React.FormEvent<HTMLFormElement>) {
    event.preventDefault();
    const payload = { scheduledAt, caseId: Number(caseId), therapistId: Number(therapistId), type };
    if (mode === "create") {
      await onSubmit({ ...payload, status: "SCHEDULED" });
      return;
    }
    await onSubmit({ ...payload, status });
  }

  return (
    <Dialog open={open} onClose={submitting ? undefined : onClose} fullWidth maxWidth="sm" PaperProps={{ sx: { backgroundColor: adminColors.panelElevated, color: adminColors.textStrong, border: `1px solid ${adminColors.border}`, backgroundImage: "none" } }}>
      <DialogTitle sx={{ color: adminColors.textStrong }}>{mode === "create" ? "Add Appointment" : "Edit Appointment"}</DialogTitle>
      <DialogContent>
        <Stack component="form" spacing={2.5} sx={{ pt: 1 }} onSubmit={handleSubmit}>
          {error && <Alert severity="error">{error}</Alert>}
          <TextField label="Scheduled At" type="datetime-local" value={scheduledAt} onChange={(e) => setScheduledAt(e.target.value)} required autoFocus fullWidth sx={adminTextFieldSx} InputLabelProps={{ shrink: true }} />
          {fixedPatient ? (
            <TextField label="Patient" value={fixedPatient.displayName} fullWidth disabled helperText="Patient is locked to the selected case." sx={adminTextFieldSx} />
          ) : (
            <TextField select label="Patient" value={patientId} onChange={(e) => handlePatientSelection(e.target.value)} required fullWidth sx={adminTextFieldSx} SelectProps={adminSelectProps}>
              {patients.map((patient) => <MenuItem key={patient.id} value={String(patient.id)}>{patient.displayName}</MenuItem>)}
            </TextField>
          )}
          {fixedCase ? (
            <TextField label="Case" value={caseOptions.find((o) => o.value === fixedCase.id)?.label ?? ""} fullWidth disabled helperText={caseHelperText ?? "Case is locked to the selected case."} sx={adminTextFieldSx} />
          ) : (
            <TextField select label="Case" value={caseId} onChange={(e) => setCaseId(e.target.value)} required fullWidth disabled={!patientId || caseOptionsLoading} helperText={caseHelperText} sx={adminTextFieldSx} SelectProps={adminSelectProps}>
              {caseOptions.map((option) => <MenuItem key={option.value} value={String(option.value)}>{option.label}</MenuItem>)}
            </TextField>
          )}
          <TextField select label="Therapist" value={therapistId} onChange={(e) => setTherapistId(e.target.value)} required fullWidth sx={adminTextFieldSx} SelectProps={adminSelectProps}>
            {therapistOptions.map((option) => <MenuItem key={option.value} value={String(option.value)}>{option.label}</MenuItem>)}
          </TextField>
          <TextField select label="Type" value={type} onChange={(e) => setType(e.target.value as AppointmentTypeValue)} required fullWidth sx={adminTextFieldSx} SelectProps={adminSelectProps}>
            {appointmentTypeOptions.map((option) => <MenuItem key={option.value} value={option.value}>{option.label}</MenuItem>)}
          </TextField>
          <TextField select label="Status" value={status} onChange={(e) => setStatus(e.target.value as AppointmentStatusValue)} required fullWidth disabled={mode === "create"} helperText={mode === "create" ? "New appointments are created as Scheduled." : undefined} sx={adminTextFieldSx} SelectProps={adminSelectProps}>
            {appointmentStatusOptions.map((option) => <MenuItem key={option.value} value={option.value}>{option.label}</MenuItem>)}
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
