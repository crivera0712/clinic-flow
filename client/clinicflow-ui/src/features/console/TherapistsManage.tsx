import { useState } from "react";
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
import { useCreateTherapist, useRemoveTherapist, useTherapists } from "../../queries/therapists";
import type { Therapist } from "../../types/therapist";

export function TherapistsManage() {
  const therapistsQuery = useTherapists();
  const therapists = therapistsQuery.data ?? [];
  const loading = therapistsQuery.isPending;
  // A failed first load shows only the error, not "No therapists yet".
  const loadFailed = therapistsQuery.isError && !therapistsQuery.data;
  const createTherapist = useCreateTherapist();
  const removeTherapist = useRemoveTherapist();
  const [name, setName] = useState("");
  const [deleteTarget, setDeleteTarget] = useState<Therapist | null>(null);
  const toast = useToast();
  const accents = useTherapistAccents(therapists.map((t) => t.id));

  function handleAdd() {
    if (!name.trim()) return;
    createTherapist.mutate(
      { name: name.trim() },
      {
        onSuccess: () => {
          setName("");
          toast.success("Therapist added.");
        },
        onError: (error) =>
          toast.error(error instanceof Error ? error.message : "Unable to add therapist."),
      },
    );
  }

  function handleDelete() {
    if (!deleteTarget) return;
    removeTherapist.mutate(deleteTarget.id, {
      onSuccess: () => {
        setDeleteTarget(null);
        toast.success("Therapist removed.");
      },
      onError: (error) =>
        toast.error(error instanceof Error ? error.message : "Unable to remove therapist."),
    });
  }

  return (
    <Stack spacing={3.5}>
      <PageHeader
        eyebrow="Care team"
        title="Therapists"
        description="Manage the clinicians available for scheduling and daily patient care."
        onRefresh={() => void therapistsQuery.refetch()}
        refreshing={therapistsQuery.isFetching}
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
            handleAdd();
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
            disabled={createTherapist.isPending}
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
        {therapistsQuery.isError && (
          <Alert severity="error">
            {therapistsQuery.error instanceof Error
              ? therapistsQuery.error.message
              : "Unable to load therapists."}
          </Alert>
        )}
        {loadFailed ? null : loading ? (
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
        loading={removeTherapist.isPending}
        description={
          deleteTarget
            ? `Remove ${deleteTarget.name}? They will no longer be available for scheduling.`
            : ""
        }
        onClose={() => setDeleteTarget(null)}
        onConfirm={handleDelete}
      />
    </Stack>
  );
}
