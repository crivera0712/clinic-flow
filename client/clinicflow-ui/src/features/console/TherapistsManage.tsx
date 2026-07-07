import { useEffect, useState } from "react";
import AddRoundedIcon from "@mui/icons-material/AddRounded";
import DeleteOutlineRoundedIcon from "@mui/icons-material/DeleteOutlineRounded";
import MedicalServicesRoundedIcon from "@mui/icons-material/MedicalServicesRounded";
import Alert from "@mui/material/Alert";
import Avatar from "@mui/material/Avatar";
import Box from "@mui/material/Box";
import Button from "@mui/material/Button";
import Paper from "@mui/material/Paper";
import Snackbar from "@mui/material/Snackbar";
import Stack from "@mui/material/Stack";
import TextField from "@mui/material/TextField";
import Typography from "@mui/material/Typography";
import IconButton from "@mui/material/IconButton";
import Tooltip from "@mui/material/Tooltip";
import { alpha } from "@mui/material/styles";
import { adminTextFieldSx } from "../../components/admin/adminStyles";
import { ConfirmDeleteDialog } from "../../components/admin/ConfirmDeleteDialog";
import { EmptyState } from "../../components/ui/EmptyState";
import { PageHeader } from "../../components/ui/PageHeader";
import { colors } from "../../theme";
import { createTherapist, listTherapists, removeTherapist } from "../../services/therapistService";
import type { Therapist } from "../../types/therapist";

export function TherapistsManage() {
  const [therapists, setTherapists] = useState<Therapist[]>([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);
  const [name, setName] = useState("");
  const [adding, setAdding] = useState(false);
  const [deleteTarget, setDeleteTarget] = useState<Therapist | null>(null);
  const [snackbar, setSnackbar] = useState<{ open: boolean; message: string }>({
    open: false,
    message: "",
  });

  async function refresh() {
    try {
      setLoading(true);
      setError(null);
      setTherapists(await listTherapists());
    } catch (loadError) {
      setError(loadError instanceof Error ? loadError.message : "Unable to load therapists.");
    } finally {
      setLoading(false);
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
      setSnackbar({
        open: true,
        message: addError instanceof Error ? addError.message : "Unable to add therapist.",
      });
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
      setSnackbar({
        open: true,
        message: deleteError instanceof Error ? deleteError.message : "Unable to remove therapist.",
      });
    }
  }

  return (
    <Stack spacing={3.5}>
      <PageHeader
        eyebrow="Care team"
        title="Therapists"
        description="Manage the clinicians available for scheduling and daily patient care."
      />

      <Paper sx={{ p: { xs: 2, md: 2.5 }, bgcolor: colors.surface }}>
        <Stack direction="row" spacing={1.25} alignItems="center" sx={{ mb: 2 }}>
          <MedicalServicesRoundedIcon color="primary" />
          <Box>
            <Typography variant="h6">Add to the care team</Typography>
            <Typography variant="body2" color="text.secondary">
              New therapists are immediately available in scheduling.
            </Typography>
          </Box>
        </Stack>
        <Stack
          direction={{ xs: "column", sm: "row" }}
          spacing={2}
          component="form"
          onSubmit={(e) => {
            e.preventDefault();
            void handleAdd();
          }}
        >
          <TextField
            label="Therapist name"
            value={name}
            onChange={(e) => setName(e.target.value)}
            required
            sx={{ flex: 1, ...adminTextFieldSx }}
          />
          <Button
            type="submit"
            variant="contained"
            startIcon={<AddRoundedIcon />}
            disabled={adding}
          >
            Add therapist
          </Button>
        </Stack>
      </Paper>

      <Paper sx={{ overflow: "hidden", bgcolor: colors.surface }}>
        <Box sx={{ px: { xs: 2, md: 2.5 }, py: 2, borderBottom: `1px solid ${colors.borderSoft}` }}>
          <Typography variant="h6">Active therapists</Typography>
          <Typography variant="body2" color="text.secondary">
            {loading
              ? "Loading care team…"
              : `${therapists.length} therapist${therapists.length === 1 ? "" : "s"}`}
          </Typography>
        </Box>
        {error && (
          <Alert severity="error" sx={{ mb: 2 }}>
            {error}
          </Alert>
        )}
        {!loading && therapists.length === 0 ? (
          <EmptyState
            icon={<MedicalServicesRoundedIcon sx={{ fontSize: 42 }} />}
            title="No therapists yet"
            description="Add the first member of your care team above."
          />
        ) : (
          <Stack divider={<Box sx={{ borderBottom: `1px solid ${colors.borderSoft}` }} />}>
            {therapists.map((t) => (
              <Stack
                key={t.id}
                direction="row"
                spacing={2}
                alignItems="center"
                sx={{
                  px: { xs: 2, md: 2.5 },
                  py: 1.75,
                  "&:hover": { bgcolor: alpha(colors.surfaceRaised, 0.45) },
                }}
              >
                <Avatar
                  sx={{
                    bgcolor: alpha(colors.success, 0.13),
                    color: "success.main",
                    fontWeight: 750,
                  }}
                >
                  {t.name.charAt(0).toUpperCase()}
                </Avatar>
                <Box sx={{ flexGrow: 1 }}>
                  <Typography fontWeight={700}>{t.name}</Typography>
                  <Typography variant="body2" color="text.secondary">
                    Available for appointments
                  </Typography>
                </Box>
                <Tooltip title={`Remove ${t.name}`}>
                  <IconButton
                    aria-label={`Remove ${t.name}`}
                    color="error"
                    onClick={() => setDeleteTarget(t)}
                  >
                    <DeleteOutlineRoundedIcon />
                  </IconButton>
                </Tooltip>
              </Stack>
            ))}
          </Stack>
        )}
      </Paper>

      <ConfirmDeleteDialog
        open={deleteTarget !== null}
        description={deleteTarget ? `Remove ${deleteTarget.name}?` : ""}
        onClose={() => setDeleteTarget(null)}
        onConfirm={() => void handleDelete()}
      />

      <Snackbar
        open={snackbar.open}
        autoHideDuration={4000}
        onClose={() => setSnackbar({ open: false, message: "" })}
        anchorOrigin={{ vertical: "bottom", horizontal: "right" }}
      >
        <Alert
          onClose={() => setSnackbar({ open: false, message: "" })}
          severity="success"
          variant="filled"
        >
          {snackbar.message}
        </Alert>
      </Snackbar>
    </Stack>
  );
}
