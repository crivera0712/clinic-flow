import Box from "@mui/material/Box";
import CircularProgress from "@mui/material/CircularProgress";
import Stack from "@mui/material/Stack";
import Typography from "@mui/material/Typography";
import { Navigate, Route, Routes, useLocation } from "react-router-dom";
import LoginPage from "./LoginPage";
import RegisterPage from "./RegisterPage";
import ScheduleDisplayPage from "./ScheduleDisplayPage";
import { ConsoleLayout } from "./features/console/ConsoleLayout";
import { PatientsDirectory } from "./features/console/PatientsDirectory";
import { SchedulePage } from "./features/console/SchedulePage";
import { TherapistsManage } from "./features/console/TherapistsManage";
import { useAuth } from "./auth/AuthContext";

// ADMIN (front desk): console is home, gym board is a preview at /board.
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

// DISPLAY (gym wall): the board is the only screen.
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

  if (pathname.startsWith("/register")) {
    return (
      <Routes>
        <Route path="/register/:clinicSlug" element={<RegisterPage />} />
      </Routes>
    );
  }

  if (status === "bootstrapping") {
    return (
      <Box sx={{ minHeight: "100vh", display: "grid", placeItems: "center", bgcolor: "#020617", color: "#e2e8f0" }}>
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

  return currentUser?.roleName === "ADMIN" ? <AdminApp /> : <DisplayApp />;
}

export default App;
