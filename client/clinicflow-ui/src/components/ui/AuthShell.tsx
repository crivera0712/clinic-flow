import type { ReactNode } from "react";
import Box from "@mui/material/Box";
import Paper from "@mui/material/Paper";
import Stack from "@mui/material/Stack";
import Typography from "@mui/material/Typography";
import CalendarMonthRoundedIcon from "@mui/icons-material/CalendarMonthRounded";
import GroupsRoundedIcon from "@mui/icons-material/GroupsRounded";
import MonitorHeartRoundedIcon from "@mui/icons-material/MonitorHeartRounded";
import { BrandMark } from "./BrandMark";
import { colors } from "../../theme";

const benefits = [
  { icon: <CalendarMonthRoundedIcon />, label: "Daily appointment board" },
  { icon: <GroupsRoundedIcon />, label: "Patient and therapist coordination" },
  { icon: <MonitorHeartRoundedIcon />, label: "Treatment-floor display view" },
];

export function AuthShell({ children }: { children: ReactNode }) {
  return (
    <Box
      sx={{
        minHeight: "100vh",
        display: "grid",
        placeItems: "center",
        p: { xs: 2, sm: 4 },
        background:
          "radial-gradient(circle at 15% 10%, rgba(56,189,248,.12), transparent 30%), radial-gradient(circle at 90% 90%, rgba(45,212,191,.08), transparent 30%), #050b18",
      }}
    >
      <Paper
        sx={{
          width: "100%",
          maxWidth: 1040,
          overflow: "hidden",
          display: "grid",
          gridTemplateColumns: { xs: "1fr", md: "1.08fr .92fr" },
          bgcolor: colors.surface,
          boxShadow: "0 36px 100px rgba(0,0,0,.48)",
        }}
      >
        <Stack
          spacing={5}
          sx={{
            display: { xs: "none", md: "flex" },
            p: { md: 5, lg: 7 },
            justifyContent: "space-between",
            background: "linear-gradient(145deg, rgba(19,34,56,.96), rgba(8,18,34,.98))",
            borderRight: `1px solid ${colors.borderSoft}`,
          }}
        >
          <Stack direction="row" spacing={1.5} alignItems="center">
            <BrandMark />
            <Typography variant="h6">ClinicFlow</Typography>
          </Stack>
          <Box>
            <Typography variant="h3" sx={{ maxWidth: 500 }}>A clearer view of today&apos;s clinic schedule.</Typography>
            <Typography color="text.secondary" sx={{ mt: 2, maxWidth: 480, fontSize: "1.05rem", lineHeight: 1.75 }}>
              Manage appointments, patient check-ins, and therapist schedules in one clinic-focused workspace.
            </Typography>
          </Box>
          <Stack spacing={2.25}>
            {benefits.map((benefit) => (
              <Stack key={benefit.label} direction="row" spacing={1.5} alignItems="center" color="text.secondary">
                <Box sx={{ color: "primary.main", display: "flex" }}>{benefit.icon}</Box>
                <Typography>{benefit.label}</Typography>
              </Stack>
            ))}
          </Stack>
        </Stack>
        <Box sx={{ p: { xs: 3, sm: 5, md: 6 }, alignSelf: "center" }}>{children}</Box>
      </Paper>
    </Box>
  );
}
