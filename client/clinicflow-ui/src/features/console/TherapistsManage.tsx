import { useMemo, useState } from "react";
import AddRoundedIcon from "@mui/icons-material/AddRounded";
import DeleteOutlineRoundedIcon from "@mui/icons-material/DeleteOutlineRounded";
import MedicalServicesRoundedIcon from "@mui/icons-material/MedicalServicesRounded";
import MoreVertRoundedIcon from "@mui/icons-material/MoreVertRounded";
import Alert from "@mui/material/Alert";
import Avatar from "@mui/material/Avatar";
import Box from "@mui/material/Box";
import Button from "@mui/material/Button";
import IconButton from "@mui/material/IconButton";
import ListItemIcon from "@mui/material/ListItemIcon";
import ListItemText from "@mui/material/ListItemText";
import Menu from "@mui/material/Menu";
import MenuItem from "@mui/material/MenuItem";
import Paper from "@mui/material/Paper";
import Skeleton from "@mui/material/Skeleton";
import Stack from "@mui/material/Stack";
import TextField from "@mui/material/TextField";
import Typography from "@mui/material/Typography";
import { useIsDemo } from "../../auth/AuthContext";
import { ConfirmDialog } from "../../components/ui/ConfirmDialog";
import { DemoReadOnly } from "../../components/ui/DemoReadOnly";
import { EmptyState } from "../../components/ui/EmptyState";
import { FormDrawer } from "../../components/ui/FormDrawer";
import { PageHeader } from "../../components/ui/PageHeader";
import { useTherapistAccents } from "../../components/ui/therapistAccents";
import { useToast } from "../../components/ui/useToast";
import { useAppointments } from "../../queries/appointments";
import { useCreateTherapist, useRemoveTherapist, useTherapists } from "../../queries/therapists";
import type { Therapist } from "../../types/therapist";
import { getTodayDateString } from "./scheduleUtils";

type DayLoad = { total: number; waiting: number };

const gridSx = {
  display: "grid",
  gridTemplateColumns: "repeat(auto-fill, minmax(260px, 1fr))",
  gap: 2,
};

function todayLabel(load: DayLoad | undefined): string {
  if (!load || load.total === 0) return "No visits today";
  const visits = `${load.total} visit${load.total === 1 ? "" : "s"} today`;
  return load.waiting > 0 ? `${visits} · ${load.waiting} waiting` : visits;
}

function TherapistCard({
  therapist,
  accent,
  load,
  readOnly,
  onRemove,
}: {
  therapist: Therapist;
  accent?: string;
  load?: DayLoad;
  // Demo clinic: removing therapists is turned off.
  readOnly: boolean;
  onRemove: () => void;
}) {
  const [menuAnchor, setMenuAnchor] = useState<HTMLElement | null>(null);

  return (
    <Paper
      component="article"
      aria-label={therapist.name}
      sx={(theme) => ({
        position: "relative",
        overflow: "hidden",
        p: 2,
        pl: 2.75,
        boxShadow: theme.vars.palette.elevation.card,
        // Accent bar on the left edge, the same color the therapist has on the board.
        "&::before": {
          content: '""',
          position: "absolute",
          inset: "0 auto 0 0",
          width: 5,
          bgcolor: accent ?? theme.vars.palette.text.disabled,
        },
      })}
    >
      <Stack direction="row" spacing={1.5} alignItems="center">
        <Avatar
          sx={(theme) => {
            const color = accent ?? theme.vars.palette.primary.main;
            return { bgcolor: theme.alpha(color, 0.14), color, fontWeight: 750 };
          }}
        >
          {therapist.name.charAt(0).toUpperCase()}
        </Avatar>
        <Box sx={{ minWidth: 0, flexGrow: 1 }}>
          <Typography fontWeight={700} noWrap>
            {therapist.name}
          </Typography>
          <Typography variant="body2" color="text.secondary">
            {todayLabel(load)}
          </Typography>
        </Box>
        <IconButton
          aria-label={`More actions for ${therapist.name}`}
          size="small"
          onClick={(event) => setMenuAnchor(event.currentTarget)}
        >
          <MoreVertRoundedIcon fontSize="small" />
        </IconButton>
        <Menu
          anchorEl={menuAnchor}
          open={menuAnchor !== null}
          onClose={() => setMenuAnchor(null)}
          anchorOrigin={{ vertical: "bottom", horizontal: "right" }}
          transformOrigin={{ vertical: "top", horizontal: "right" }}
        >
          <MenuItem
            disabled={readOnly}
            onClick={() => {
              setMenuAnchor(null);
              onRemove();
            }}
            sx={{ color: "error.main" }}
          >
            <ListItemIcon sx={{ color: "inherit" }}>
              <DeleteOutlineRoundedIcon fontSize="small" />
            </ListItemIcon>
            <ListItemText secondary={readOnly ? "Read-only in the demo" : undefined}>
              Remove therapist
            </ListItemText>
          </MenuItem>
        </Menu>
      </Stack>
    </Paper>
  );
}

export function TherapistsManage() {
  const therapistsQuery = useTherapists();
  const therapists = useMemo(() => therapistsQuery.data ?? [], [therapistsQuery.data]);
  const today = useAppointments(getTodayDateString());
  const removeTherapist = useRemoveTherapist();
  const [adding, setAdding] = useState(false);
  const [removeTarget, setRemoveTarget] = useState<Therapist | null>(null);
  const toast = useToast();
  const isDemo = useIsDemo();
  const accents = useTherapistAccents(therapists.map((t) => t.id));

  // Today's visits per therapist, from the same cached schedule the Schedule page uses.
  const loads = useMemo(() => {
    const byTherapist = new Map<number, DayLoad>();
    for (const row of today.data ?? []) {
      const load = byTherapist.get(row.therapistId) ?? { total: 0, waiting: 0 };
      load.total += 1;
      if (row.status === "WAITING") load.waiting += 1;
      byTherapist.set(row.therapistId, load);
    }
    return byTherapist;
  }, [today.data]);

  // A failed first load shows only the error, not "No therapists yet".
  const loadFailed = therapistsQuery.isError && !therapistsQuery.data;

  function handleRemove() {
    if (!removeTarget) return;
    removeTherapist.mutate(removeTarget.id, {
      onSuccess: () => {
        setRemoveTarget(null);
        toast.success("Therapist removed.");
      },
      onError: (error) => toast.error(error.message || "Unable to remove therapist."),
    });
  }

  const addTherapistButton = (
    <DemoReadOnly active={isDemo}>
      <Button
        variant="contained"
        startIcon={<AddRoundedIcon />}
        disabled={isDemo}
        onClick={() => setAdding(true)}
      >
        Add therapist
      </Button>
    </DemoReadOnly>
  );

  function renderGrid() {
    if (loadFailed) return null;
    if (therapistsQuery.isPending) {
      return (
        <Box sx={gridSx} aria-hidden="true">
          {Array.from({ length: 4 }, (_, index) => (
            <Skeleton key={index} variant="rounded" height={80} sx={{ borderRadius: 1 }} />
          ))}
        </Box>
      );
    }
    if (therapists.length === 0) {
      return (
        <Paper>
          <EmptyState
            icon={<MedicalServicesRoundedIcon sx={{ fontSize: 42 }} />}
            title="No therapists yet"
            description="Add the first member of your care team."
            action={addTherapistButton}
          />
        </Paper>
      );
    }
    return (
      <Box sx={gridSx}>
        {therapists.map((therapist) => (
          <TherapistCard
            key={therapist.id}
            therapist={therapist}
            accent={accents.get(therapist.id)}
            load={loads.get(therapist.id)}
            readOnly={isDemo}
            onRemove={() => setRemoveTarget(therapist)}
          />
        ))}
      </Box>
    );
  }

  const visitsToday = today.data?.length ?? 0;

  return (
    <Stack spacing={3}>
      <PageHeader
        eyebrow="Care team"
        title="Therapists"
        description="Manage the clinicians available for scheduling and daily patient care."
        onRefresh={() => void therapistsQuery.refetch()}
        refreshing={therapistsQuery.isFetching}
        action={addTherapistButton}
      />

      <Stack component="section" aria-label="Care team" spacing={1.5}>
        <Box>
          <Typography variant="h6" component="h2">
            Care team
          </Typography>
          <Typography variant="body2" color="text.secondary">
            {therapistsQuery.isPending
              ? "Loading care team…"
              : `${therapists.length} therapist${therapists.length === 1 ? "" : "s"} · ${visitsToday} visit${visitsToday === 1 ? "" : "s"} today`}
          </Typography>
        </Box>
        {therapistsQuery.isError && (
          <Alert severity="error">
            {therapistsQuery.error instanceof Error
              ? therapistsQuery.error.message
              : "Unable to load therapists."}
          </Alert>
        )}
        {renderGrid()}
      </Stack>

      {adding && <AddTherapistDrawer onClose={() => setAdding(false)} />}

      <ConfirmDialog
        open={removeTarget !== null}
        title="Remove therapist"
        confirmLabel="Remove"
        tone="danger"
        loading={removeTherapist.isPending}
        // The backend deletes a removed therapist's appointments along with them (past and
        // future), so say so before the user confirms.
        description={
          removeTarget
            ? `Remove ${removeTarget.name}? This also permanently deletes all of their appointments, including past visits.`
            : ""
        }
        onClose={() => setRemoveTarget(null)}
        onConfirm={handleRemove}
      />
    </Stack>
  );
}

function AddTherapistDrawer({ onClose }: { onClose: () => void }) {
  const [name, setName] = useState("");
  const createTherapist = useCreateTherapist();
  const toast = useToast();

  function handleSubmit() {
    if (!name.trim()) return;
    createTherapist.mutate(
      { name: name.trim() },
      {
        onSuccess: () => {
          onClose();
          toast.success("Therapist added.");
        },
        onError: (error) => toast.error(error.message || "Unable to add therapist."),
      },
    );
  }

  return (
    <FormDrawer
      open
      title="Add therapist"
      subtitle="New therapists are immediately available in scheduling."
      submitLabel="Add therapist"
      saving={createTherapist.isPending}
      onClose={onClose}
      onSubmit={handleSubmit}
    >
      <TextField
        label="Therapist name"
        value={name}
        onChange={(e) => setName(e.target.value)}
        required
        autoFocus
      />
    </FormDrawer>
  );
}
