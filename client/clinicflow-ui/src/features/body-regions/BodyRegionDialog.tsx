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
import type { BodyRegion, BodyRegionCreateRequest, BodyRegionUpdateRequest } from "../../types/admin";

type Props = {
  open: boolean;
  mode: "create" | "edit";
  initialValue?: BodyRegion | null;
  submitting?: boolean;
  error?: string | null;
  onClose: () => void;
  onSubmit: (payload: BodyRegionCreateRequest | BodyRegionUpdateRequest) => Promise<void>;
};

export function BodyRegionDialog({ open, mode, initialValue, submitting = false, error, onClose, onSubmit }: Props) {
  const [code, setCode] = useState(initialValue?.code ?? "");
  const [displayName, setDisplayName] = useState(initialValue?.displayName ?? "");

  async function handleSubmit(event: React.FormEvent<HTMLFormElement>) {
    event.preventDefault();
    if (mode === "create") {
      await onSubmit({ code: code.trim(), displayName: displayName.trim() });
      return;
    }
    await onSubmit({ code: code.trim() });
  }

  return (
    <Dialog open={open} onClose={submitting ? undefined : onClose} fullWidth maxWidth="sm" PaperProps={{ sx: { backgroundColor: adminColors.panelElevated, color: adminColors.textStrong, border: `1px solid ${adminColors.border}`, backgroundImage: "none" } }}>
      <DialogTitle sx={{ color: adminColors.textStrong }}>{mode === "create" ? "Add Body Region" : "Edit Body Region"}</DialogTitle>
      <DialogContent>
        <Stack component="form" spacing={2.5} sx={{ pt: 1 }} onSubmit={handleSubmit}>
          {error && <Alert severity="error">{error}</Alert>}
          <TextField label="Code" value={code} onChange={(e) => setCode(e.target.value)} required fullWidth autoFocus sx={adminTextFieldSx} />
          <TextField label="Display Name" value={displayName} onChange={(e) => setDisplayName(e.target.value)} required={mode === "create"} disabled={mode === "edit"} helperText={mode === "edit" ? "Display name cannot be changed with the current backend patch contract." : undefined} fullWidth sx={adminTextFieldSx} />
          <DialogActions sx={{ px: 0, pb: 0 }}>
            <Button onClick={onClose} disabled={submitting}>Cancel</Button>
            <Button type="submit" variant="contained" disabled={submitting}>{mode === "create" ? "Create" : "Save Changes"}</Button>
          </DialogActions>
        </Stack>
      </DialogContent>
    </Dialog>
  );
}
