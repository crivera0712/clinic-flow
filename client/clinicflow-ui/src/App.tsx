import Box from "@mui/material/Box";
import CircularProgress from "@mui/material/CircularProgress";
import Stack from "@mui/material/Stack";
import Typography from "@mui/material/Typography";
import { useEffect } from "react";
import { useQueryClient } from "@tanstack/react-query";
import { Navigate, Route, Routes, useLocation } from "react-router-dom";
import LoginPage from "./LoginPage";
import LandingPage from "./features/landing/LandingPage";
import RegisterPage from "./RegisterPage";
import ScheduleDisplayPage from "./ScheduleDisplayPage";
import { ConsoleLayout } from "./features/console/ConsoleLayout";
import { PatientsDirectory } from "./features/console/PatientsDirectory";
import { SchedulePage } from "./features/console/SchedulePage";
import { TherapistsManage } from "./features/console/TherapistsManage";
import { useAuth } from "./auth/AuthContext";
import { BrandMark } from "./components/ui/BrandMark";

// ADMIN (front desk): console is home, clinic display is available at /board.
function AdminApp() {
  return (
    <Routes>
      <Route path="/" element={<ConsoleLayout />}>
        <Route index element={<SchedulePage />} />
        <Route path="patients" element={<PatientsDirectory />} />
        <Route path="therapists" element={<TherapistsManage />} />
      </Route>
      <Route path="/board" element={<ScheduleDisplayPage />} />
      <Route path="*" element={<Navigate to="/" replace />} />
    </Routes>
  );
}

// DISPLAY: the clinic display is the only screen.
function DisplayApp() {
  return (
    <Routes>
      <Route path="/" element={<ScheduleDisplayPage />} />
      <Route path="*" element={<Navigate to="/" replace />} />
    </Routes>
  );
}

function App() {
  const { status, currentUser } = useAuth();
  const { pathname } = useLocation();
  const queryClient = useQueryClient();

  // Cached data belongs to the clinic that was signed in. Drop all of it on logout or session
  // expiry, so the next login (possibly another clinic) never sees it.
  useEffect(() => {
    if (status === "anonymous") queryClient.clear();
  }, [status, queryClient]);

  if (pathname.startsWith("/register")) {
    return (
      <Routes>
        <Route path="/register/:clinicSlug" element={<RegisterPage />} />
      </Routes>
    );
  }

  if (status === "bootstrapping") {
    return (
      <Box sx={{ minHeight: "100vh", display: "grid", placeItems: "center" }}>
        <Stack spacing={2} alignItems="center">
          <BrandMark size={48} />
          <CircularProgress />
          <Typography color="text.secondary">Restoring your clinic workspace…</Typography>
        </Stack>
      </Box>
    );
  }

  // Signed out: the landing page at /, sign-in at /login; anything else goes to the landing
  // page (so logging out lands there too).
  if (status === "anonymous") {
    return (
      <Routes>
        <Route path="/" element={<LandingPage />} />
        <Route path="/login" element={<LoginPage />} />
        <Route path="*" element={<Navigate to="/" replace />} />
      </Routes>
    );
  }

  return currentUser?.roleName === "ADMIN" ? <AdminApp /> : <DisplayApp />;
}

export default App;
