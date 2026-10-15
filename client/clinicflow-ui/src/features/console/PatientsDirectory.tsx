import { useMemo, useState } from "react";
import CloseRoundedIcon from "@mui/icons-material/CloseRounded";
import EditRoundedIcon from "@mui/icons-material/EditRounded";
import PersonAddAltRoundedIcon from "@mui/icons-material/PersonAddAltRounded";
import SearchRoundedIcon from "@mui/icons-material/SearchRounded";
import Alert from "@mui/material/Alert";
import Avatar from "@mui/material/Avatar";
import Box from "@mui/material/Box";
import Button from "@mui/material/Button";
import IconButton from "@mui/material/IconButton";
import InputAdornment from "@mui/material/InputAdornment";
import Paper from "@mui/material/Paper";
import Stack from "@mui/material/Stack";
import TextField from "@mui/material/TextField";
import Tooltip from "@mui/material/Tooltip";
import Typography from "@mui/material/Typography";
import { DataGrid, type GridColDef } from "@mui/x-data-grid";
import { FormDrawer } from "../../components/ui/FormDrawer";
import { PageHeader } from "../../components/ui/PageHeader";
import { useToast } from "../../components/ui/useToast";
import { useCreatePatient, usePatients, useUpdatePatient } from "../../queries/patients";
import { displayName, type Patient } from "../../types/patient";

// Case-insensitive: every word typed must appear somewhere in the patient's full name.
function matchesSearch(patient: Patient, query: string): boolean {
  const name = displayName(patient).toLowerCase();
  return query
    .toLowerCase()
    .split(/\s+/)
    .filter(Boolean)
    .every((word) => name.includes(word));
}

function initials(patient: Patient): string {
  return `${patient.firstName.charAt(0)}${patient.lastName.charAt(0)}`.toUpperCase();
}

// null = closed, "new" = adding, Patient = editing that patient.
type DrawerTarget = null | "new" | Patient;

export function PatientsDirectory() {
  const patients = usePatients();
  const allRows = useMemo(() => patients.data?.rows ?? [], [patients.data]);
  const totalOnServer = patients.data?.rowCount ?? 0;
  const [search, setSearch] = useState("");
  const [drawer, setDrawer] = useState<DrawerTarget>(null);

  const rows = useMemo(
    () => (search.trim() ? allRows.filter((patient) => matchesSearch(patient, search)) : allRows),
    [allRows, search],
  );

  const columns: GridColDef<Patient>[] = [
    {
      field: "name",
      headerName: "Name",
      flex: 1,
      minWidth: 220,
      // Sort by last name, then first; display "First Last".
      valueGetter: (_value, row) => `${row.lastName} ${row.firstName}`,
      renderCell: ({ row }) => (
        <Stack direction="row" spacing={1.5} alignItems="center" sx={{ height: "100%" }}>
          <Avatar
            sx={(theme) => ({
              width: 34,
              height: 34,
              fontSize: "0.85rem",
              fontWeight: 750,
              color: "primary.main",
              bgcolor: theme.alpha(theme.vars.palette.primary.main, 0.12),
            })}
          >
            {initials(row)}
          </Avatar>
          <Typography fontWeight={650}>{displayName(row)}</Typography>
        </Stack>
      ),
    },
    {
      field: "actions",
      headerName: "",
      width: 72,
      sortable: false,
      filterable: false,
      disableColumnMenu: true,
      align: "right",
      renderCell: ({ row }) => (
        <Tooltip title="Edit patient">
          <IconButton aria-label={`Edit ${displayName(row)}`} onClick={() => setDrawer(row)}>
            <EditRoundedIcon fontSize="small" />
          </IconButton>
        </Tooltip>
      ),
    },
  ];

  function countLabel(): string {
    if (patients.isPending) return "Loading patients…";
    const total = `${allRows.length} patient${allRows.length === 1 ? "" : "s"}`;
    return search.trim() ? `${rows.length} of ${total}` : total;
  }

  return (
    <Stack spacing={3}>
      <PageHeader
        eyebrow="Clinic directory"
        title="Patients"
        description="A focused directory for everyone currently receiving care at the clinic."
        onRefresh={() => void patients.refetch()}
        refreshing={patients.isFetching}
        action={
          <Button
            variant="contained"
            startIcon={<PersonAddAltRoundedIcon />}
            onClick={() => setDrawer("new")}
          >
            Add patient
          </Button>
        }
      />

      <Paper
        component="section"
        aria-label="Patient directory"
        sx={(theme) => ({ overflow: "hidden", boxShadow: theme.vars.palette.elevation.card })}
      >
        <Stack
          direction={{ xs: "column", sm: "row" }}
          spacing={1.5}
          alignItems={{ sm: "center" }}
          justifyContent="space-between"
          sx={{ px: { xs: 2, md: 2.5 }, py: 2, borderBottom: 1, borderColor: "divider" }}
        >
          <Box>
            <Typography variant="h6" component="h2">
              Patient directory
            </Typography>
            <Typography variant="body2" color="text.secondary">
              {countLabel()}
            </Typography>
          </Box>
          <TextField
            value={search}
            onChange={(event) => setSearch(event.target.value)}
            placeholder="Search by name"
            sx={{ width: { xs: "100%", sm: 300 } }}
            slotProps={{
              htmlInput: { "aria-label": "Search patients" },
              input: {
                startAdornment: (
                  <InputAdornment position="start">
                    <SearchRoundedIcon sx={{ color: "text.disabled" }} />
                  </InputAdornment>
                ),
                endAdornment: search ? (
                  <InputAdornment position="end">
                    <IconButton
                      aria-label="Clear search"
                      size="small"
                      onClick={() => setSearch("")}
                    >
                      <CloseRoundedIcon fontSize="small" />
                    </IconButton>
                  </InputAdornment>
                ) : undefined,
              },
            }}
          />
        </Stack>
        {patients.isError && (
          <Alert severity="error" sx={{ borderRadius: 0, border: 0 }}>
            {patients.error instanceof Error ? patients.error.message : "Unable to load patients."}
          </Alert>
        )}
        {totalOnServer > allRows.length && (
          <Alert severity="info" sx={{ borderRadius: 0, border: 0 }}>
            Showing the first {allRows.length} of {totalOnServer} patients.
          </Alert>
        )}
        <Box sx={{ height: { xs: 480, md: 600 } }}>
          <DataGrid
            rows={rows}
            columns={columns}
            loading={patients.isPending}
            disableRowSelectionOnClick
            rowHeight={60}
            columnHeaderHeight={48}
            pageSizeOptions={[10, 25, 50]}
            initialState={{
              pagination: { paginationModel: { page: 0, pageSize: 10 } },
              sorting: { sortModel: [{ field: "name", sort: "asc" }] },
            }}
            localeText={{
              noRowsLabel: search.trim()
                ? `No patients match "${search.trim()}"`
                : "No patients yet",
            }}
            slotProps={{ loadingOverlay: { variant: "skeleton", noRowsVariant: "skeleton" } }}
          />
        </Box>
      </Paper>

      {drawer !== null && (
        <PatientDrawer
          // Remount per patient so the form starts from that patient's values.
          key={drawer === "new" ? "new" : drawer.id}
          patient={drawer === "new" ? null : drawer}
          onClose={() => setDrawer(null)}
        />
      )}
    </Stack>
  );
}

function PatientDrawer({ patient, onClose }: { patient: Patient | null; onClose: () => void }) {
  const [firstName, setFirstName] = useState(patient?.firstName ?? "");
  const [lastName, setLastName] = useState(patient?.lastName ?? "");
  const createPatient = useCreatePatient();
  const updatePatient = useUpdatePatient();
  const saving = createPatient.isPending || updatePatient.isPending;
  const toast = useToast();

  function handleSubmit() {
    const names = { firstName: firstName.trim(), lastName: lastName.trim() };
    const callbacks = {
      onSuccess: () => {
        onClose();
        toast.success(patient ? "Patient updated." : "Patient added.");
      },
      onError: (error: Error) =>
        toast.error(
          error.message || (patient ? "Unable to update patient." : "Unable to add patient."),
        ),
    };
    if (patient) updatePatient.mutate({ id: patient.id, patch: names }, callbacks);
    else createPatient.mutate(names, callbacks);
  }

  return (
    <FormDrawer
      open
      title={patient ? "Edit patient" : "Add patient"}
      subtitle={patient ? displayName(patient) : "Add a patient to the clinic directory."}
      submitLabel={patient ? "Save" : "Add patient"}
      saving={saving}
      onClose={onClose}
      onSubmit={handleSubmit}
    >
      <TextField
        label="First name"
        value={firstName}
        onChange={(e) => setFirstName(e.target.value)}
        required
        autoFocus
      />
      <TextField
        label="Last name"
        value={lastName}
        onChange={(e) => setLastName(e.target.value)}
        required
      />
    </FormDrawer>
  );
}
