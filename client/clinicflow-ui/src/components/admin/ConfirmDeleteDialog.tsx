import Alert from "@mui/material/Alert";
import Button from "@mui/material/Button";
import Dialog from "@mui/material/Dialog";
import DialogActions from "@mui/material/DialogActions";
import DialogContent from "@mui/material/DialogContent";
import DialogTitle from "@mui/material/DialogTitle";
import Typography from "@mui/material/Typography";
import { adminColors } from "./adminStyles";

type ConfirmDeleteDialogProps = {
  open: boolean;
  description: string;
  error?: string | null;
  loading?: boolean;
  onClose: () => void;
  onConfirm: () => void;
};

export function ConfirmDeleteDialog({
  open,
  description,
  error,
  loading = false,
  onClose,
  onConfirm,
}: ConfirmDeleteDialogProps) {
  return (
    <Dialog
      open={open}
      onClose={loading ? undefined : onClose}
      fullWidth
      maxWidth="xs"
      PaperProps={{
        sx: {
          backgroundColor: adminColors.panelElevated,
          color: adminColors.textStrong,
          border: `1px solid ${adminColors.border}`,
          backgroundImage: "none",
        },
      }}
    >
      <DialogTitle sx={{ color: adminColors.textStrong }}>Confirm Delete</DialogTitle>
      <DialogContent>
        {error && <Alert severity="error" sx={{ mb: 2 }}>{error}</Alert>}
        <Typography sx={{ color: adminColors.textSecondary }}>{description}</Typography>
      </DialogContent>
      <DialogActions sx={{ px: 3, pb: 2.5 }}>
        <Button onClick={onClose} disabled={loading}>
          Cancel
        </Button>
        <Button color="error" variant="contained" onClick={onConfirm} disabled={loading}>
          Delete
        </Button>
      </DialogActions>
    </Dialog>
  );
}
