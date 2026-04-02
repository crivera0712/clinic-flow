import AppBar from "@mui/material/AppBar";
import Box from "@mui/material/Box";
import Button from "@mui/material/Button";
import Chip from "@mui/material/Chip";
import CircularProgress from "@mui/material/CircularProgress";
import Container from "@mui/material/Container";
import Stack from "@mui/material/Stack";
import Toolbar from "@mui/material/Toolbar";
import Typography from "@mui/material/Typography";
import type { ReactNode } from "react";
import { Link as RouterLink, Navigate, Route, Routes } from "react-router-dom";
import LoginPage from "./LoginPage";
import ScheduleDisplayPage from "./ScheduleDisplayPage";
import { AdminLayout } from "./components/admin/AdminLayout";
import { AppointmentsPage } from "./features/appointments/AppointmentsPage";
import { BodyRegionsPage } from "./features/body-regions/BodyRegionsPage";
import { PatientsPage } from "./features/patients/PatientsPage";
import { TherapistsPage } from "./features/therapists/TherapistsPage";
import { useAuth } from "./auth/AuthContext";

function ScheduleShell() {
  const { currentUser, logout } = useAuth();

  return (
    <Box sx={{ minHeight: "100vh", bgcolor: "#020617" }}>
      <AppBar position="sticky" elevation={0} sx={{ bgcolor: "rgba(2, 6, 23, 0.82)", backdropFilter: "blur(16px)" }}>
        <Toolbar sx={{ gap: 2, minHeight: 80 }}>
          <Box sx={{ flexGrow: 1 }}>
            <Typography variant="overline" sx={{ color: "#38bdf8", letterSpacing: "0.22em" }}>
              Clinic Flow
            </Typography>
            <Typography variant="h6" sx={{ fontWeight: 700 }}>
              Schedule Board
            </Typography>
          </Box>

          {currentUser && (
            <Stack direction="row" spacing={1} alignItems="center">
              <Chip
                label={currentUser.roleName}
                color={currentUser.roleName === "ADMIN" ? "secondary" : "default"}
                sx={{
                  color: "#e2e8f0",
                  bgcolor: currentUser.roleName === "ADMIN" ? undefined : "rgba(148, 163, 184, 0.14)",
                }}
              />
              <Chip
                label={currentUser.username}
                variant="outlined"
                sx={{
                  color: "#e2e8f0",
                  borderColor: "rgba(148, 163, 184, 0.35)",
                  bgcolor: "rgba(15, 23, 42, 0.72)",
                  "& .MuiChip-label": {
                    color: "inherit",
                  },
                }}
              />
            </Stack>
          )}

          {currentUser?.roleName === "ADMIN" && (
            <Button component={RouterLink} to="/admin/appointments" color="inherit" variant="outlined">
              Admin Panel
            </Button>
          )}

          <Button color="inherit" onClick={() => void logout()}>
            Logout
          </Button>
        </Toolbar>
      </AppBar>

      <Container maxWidth={false} disableGutters>
        {currentUser?.roleName === "ADMIN" && (
          <Box
            sx={{
              mx: 2,
              mt: 2,
              px: 2,
              py: 1.5,
              borderRadius: 3,
              color: "#cbd5e1",
              bgcolor: "rgba(30, 41, 59, 0.92)",
              border: "1px solid rgba(148, 163, 184, 0.14)",
            }}
          >
            <Typography sx={{ fontWeight: 600 }}>Admin access is active.</Typography>
            <Typography sx={{ color: "#94a3b8" }}>
              Use the admin panel to manage appointments, body regions, patients, and therapists.
            </Typography>
          </Box>
        )}

        <ScheduleDisplayPage />
      </Container>
    </Box>
  );
}

function RequireAdmin({ children }: { children: ReactNode }) {
  const { currentUser } = useAuth();
  if (currentUser?.roleName !== "ADMIN") {
    return <Navigate to="/" replace />;
  }
  return <>{children}</>;
}

function AuthenticatedApp() {
  return (
    <Routes>
      <Route path="/" element={<ScheduleShell />} />
      <Route
        path="/admin"
        element={
          <RequireAdmin>
            <AdminLayout />
          </RequireAdmin>
        }
      >
        <Route index element={<Navigate to="/admin/appointments" replace />} />
        <Route path="appointments" element={<AppointmentsPage />} />
        <Route path="body-regions" element={<BodyRegionsPage />} />
        <Route path="patients" element={<PatientsPage />} />
        <Route path="therapists" element={<TherapistsPage />} />
      </Route>
      <Route path="*" element={<Navigate to="/" replace />} />
    </Routes>
  );
}

function App() {
  const { status } = useAuth();

  if (status === "bootstrapping") {
    return (
      <Box
        sx={{
          minHeight: "100vh",
          display: "grid",
          placeItems: "center",
          bgcolor: "#020617",
          color: "#e2e8f0",
        }}
      >
        <Stack spacing={2} alignItems="center">
          <CircularProgress />
          <Typography>Restoring session...</Typography>
        </Stack>
      </Box>
    );
  }

  if (status === "anonymous") {
    return <LoginPage />;
  }

  return <AuthenticatedApp />;
}

export default App;
