import { useEffect, useState } from "react";
import AddRoundedIcon from "@mui/icons-material/AddRounded";
import DeleteOutlineRoundedIcon from "@mui/icons-material/DeleteOutlineRounded";
import MedicalServicesRoundedIcon from "@mui/icons-material/MedicalServicesRounded";
import Alert from "@mui/material/Alert";
import Avatar from "@mui/material/Avatar";
import Box from "@mui/material/Box";
import Button from "@mui/material/Button";
import Stack from "@mui/material/Stack";
import TextField from "@mui/material/TextField";
import Typography from "@mui/material/Typography";
import IconButton from "@mui/material/IconButton";
import Tooltip from "@mui/material/Tooltip";
import { ConfirmDialog } from "../../components/ui/ConfirmDialog";
import { EmptyState } from "../../components/ui/EmptyState";
import { PageHeader } from "../../components/ui/PageHeader";
import { SectionCard } from "../../components/ui/SectionCard";
import { ListSkeleton } from "../../components/ui/Skeletons";
import { useTherapistAccents } from "../../components/ui/therapistAccents";
import { useToast } from "../../components/ui/useToast";
import { createTherapist, listTherapists, removeTherapist } from "../../services/therapistService";
import type { Therapist } from "../../types/therapist";

export function TherapistsManage() {
  const [therapists, setTherapists] = useState<Therapist[]>([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);
  const [name, setName] = useState("");
  const [adding, setAdding] = useState(false);
  const [deleteTarget, setDeleteTarget] = useState<Therapist | null>(null);
  const [deleting, setDeleting] = useState(false);
  const toast = useToast();
  const accents = useTherapistAccents(therapists.map((t) => t.id));

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
      toast.success("Therapist added.");
    } catch (addError) {
      toast.error(addError instanceof Error ? addError.message : "Unable to add therapist.");
    } finally {
      setAdding(false);
    }
  }

  async function handleDelete() {
    if (!deleteTarget) return;
    setDeleting(true);
    try {
      await removeTherapist(deleteTarget.id);
      setDeleteTarget(null);
      await refresh();
      toast.success("Therapist removed.");
    } catch (deleteError) {
      toast.error(
        deleteError instanceof Error ? deleteError.message : "Unable to remove therapist.",
      );
    } finally {
      setDeleting(false);
    }
  }

  return (
    <Stack spacing={3.5}>
      <PageHeader
        eyebrow="Care team"
        title="Therapists"
        description="Manage the clinicians available for scheduling and daily patient care."
        onRefresh={() => void refresh()}
        refreshing={loading}
      />

      <SectionCard
        icon={<MedicalServicesRoundedIcon />}
        title="Add to the care team"
        subtitle="New therapists are immediately available in scheduling."
      >
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
            sx={{ flex: 1 }}
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
      </SectionCard>

      <SectionCard
        title="Active therapists"
        subtitle={
          loading
            ? "Loading care team…"
            : `${therapists.length} therapist${therapists.length === 1 ? "" : "s"}`
        }
        disablePadding
      >
        {error && <Alert severity="error">{error}</Alert>}
        {loading && therapists.length === 0 ? (
          <ListSkeleton avatar />
        ) : therapists.length === 0 ? (
          <EmptyState
            icon={<MedicalServicesRoundedIcon sx={{ fontSize: 42 }} />}
            title="No therapists yet"
            description="Add the first member of your care team above."
          />
        ) : (
          <Stack divider={<Box sx={{ borderBottom: 1, borderColor: "divider" }} />}>
            {therapists.map((t) => (
              <Stack
                key={t.id}
                direction="row"
                spacing={2}
                alignItems="center"
                sx={{
                  px: { xs: 2, md: 2.5 },
                  py: 1.75,
                  "&:hover": { bgcolor: "action.hover" },
                }}
              >
                <Avatar
                  sx={(theme) => {
                    const accent = accents.get(t.id) ?? theme.vars.palette.primary.main;
                    return { bgcolor: theme.alpha(accent, 0.14), color: accent, fontWeight: 750 };
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
      </SectionCard>

      <ConfirmDialog
        open={deleteTarget !== null}
        title="Remove therapist"
        confirmLabel="Remove"
        tone="danger"
        loading={deleting}
        description={
          deleteTarget
            ? `Remove ${deleteTarget.name}? They will no longer be available for scheduling.`
            : ""
        }
        onClose={() => setDeleteTarget(null)}
        onConfirm={() => void handleDelete()}
      />
    </Stack>
  );
}
