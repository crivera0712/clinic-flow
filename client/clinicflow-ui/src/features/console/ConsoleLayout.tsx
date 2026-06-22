import CalendarMonthRoundedIcon from "@mui/icons-material/CalendarMonthRounded";
import ChevronRightRoundedIcon from "@mui/icons-material/ChevronRightRounded";
import GroupsRoundedIcon from "@mui/icons-material/GroupsRounded";
import LogoutRoundedIcon from "@mui/icons-material/LogoutRounded";
import MenuRoundedIcon from "@mui/icons-material/MenuRounded";
import MonitorRoundedIcon from "@mui/icons-material/MonitorRounded";
import PersonRoundedIcon from "@mui/icons-material/PersonRounded";
import AppBar from "@mui/material/AppBar";
import Avatar from "@mui/material/Avatar";
import Box from "@mui/material/Box";
import Button from "@mui/material/Button";
import Divider from "@mui/material/Divider";
import Drawer from "@mui/material/Drawer";
import IconButton from "@mui/material/IconButton";
import List from "@mui/material/List";
import ListItemButton from "@mui/material/ListItemButton";
import ListItemIcon from "@mui/material/ListItemIcon";
import ListItemText from "@mui/material/ListItemText";
import Stack from "@mui/material/Stack";
import Toolbar from "@mui/material/Toolbar";
import Tooltip from "@mui/material/Tooltip";
import Typography from "@mui/material/Typography";
import useMediaQuery from "@mui/material/useMediaQuery";
import { alpha, useTheme } from "@mui/material/styles";
import { useState } from "react";
import { Link as RouterLink, Outlet, useLocation } from "react-router-dom";
import { useAuth } from "../../auth/AuthContext";
import { BrandMark } from "../../components/ui/BrandMark";
import { colors } from "../../theme";

const drawerWidth = 264;

const navItems = [
  { label: "Schedule", to: "/", icon: <CalendarMonthRoundedIcon /> },
  { label: "Patients", to: "/patients", icon: <GroupsRoundedIcon /> },
  { label: "Therapists", to: "/therapists", icon: <PersonRoundedIcon /> },
  { label: "Clinic Display", to: "/board", icon: <MonitorRoundedIcon /> },
];

const pageNames: Record<string, string> = {
  "/": "Schedule",
  "/patients": "Patients",
  "/therapists": "Therapists",
};

export function ConsoleLayout() {
  const { currentUser, logout } = useAuth();
  const location = useLocation();
  const theme = useTheme();
  const isDesktop = useMediaQuery(theme.breakpoints.up("lg"));
  const [mobileOpen, setMobileOpen] = useState(false);

  const drawer = (
    <Stack sx={{ height: "100%", bgcolor: colors.canvasRaised }}>
      <Stack direction="row" spacing={1.5} alignItems="center" sx={{ px: 3, py: 3.25 }}>
        <BrandMark size={40} />
        <Box>
          <Typography variant="h6" lineHeight={1.1}>ClinicFlow</Typography>
          <Typography variant="caption" color="text.secondary">Front desk workspace</Typography>
        </Box>
      </Stack>
      <Divider />
      <Box sx={{ px: 2, pt: 2.5 }}>
        <Typography variant="overline" color="text.disabled" sx={{ px: 1.5 }}>Workspace</Typography>
      </Box>
      <List sx={{ px: 2, py: 1, flexGrow: 1 }}>
        {navItems.map((item) => {
          const selected = location.pathname === item.to;
          return (
            <ListItemButton
              key={item.to}
              component={RouterLink}
              to={item.to}
              selected={selected}
              onClick={() => setMobileOpen(false)}
              sx={{
                minHeight: 50,
                mb: 0.75,
                px: 1.5,
                borderRadius: 2.5,
                color: selected ? "text.primary" : "text.secondary",
                bgcolor: selected ? alpha(colors.primary, 0.11) : "transparent",
                border: `1px solid ${selected ? alpha(colors.primary, 0.22) : "transparent"}`,
                "&.Mui-selected": { bgcolor: alpha(colors.primary, 0.11) },
                "&.Mui-selected:hover": { bgcolor: alpha(colors.primary, 0.16) },
                "&:hover": { bgcolor: alpha(colors.primary, 0.07), color: "text.primary" },
              }}
            >
              <ListItemIcon sx={{ minWidth: 40, color: selected ? "primary.main" : "text.disabled" }}>{item.icon}</ListItemIcon>
              <ListItemText primary={item.label} slotProps={{ primary: { fontWeight: selected ? 700 : 600 } }} />
              {selected && <ChevronRightRoundedIcon fontSize="small" sx={{ color: "primary.main" }} />}
            </ListItemButton>
          );
        })}
      </List>
      <Box sx={{ p: 2 }}>
        <Stack direction="row" spacing={1.5} alignItems="center" sx={{ p: 1.5, borderRadius: 2.5, bgcolor: alpha(colors.surfaceRaised, 0.65), border: `1px solid ${colors.borderSoft}` }}>
          <Avatar sx={{ width: 38, height: 38, bgcolor: alpha(colors.primary, 0.15), color: "primary.main", fontWeight: 750 }}>
            {currentUser?.username?.charAt(0).toUpperCase() ?? "U"}
          </Avatar>
          <Box sx={{ minWidth: 0, flexGrow: 1 }}>
            <Typography variant="body2" fontWeight={700} noWrap>{currentUser?.username}</Typography>
            <Typography variant="caption" color="text.secondary">{currentUser?.roleName}</Typography>
          </Box>
          <Tooltip title="Log out">
            <IconButton aria-label="Log out" size="small" onClick={() => void logout()} sx={{ color: "text.secondary" }}>
              <LogoutRoundedIcon fontSize="small" />
            </IconButton>
          </Tooltip>
        </Stack>
      </Box>
    </Stack>
  );

  return (
    <Box sx={{ display: "flex", minHeight: "100vh", bgcolor: "background.default" }}>
      <AppBar
        position="fixed"
        color="transparent"
        elevation={0}
        sx={{
          width: { lg: `calc(100% - ${drawerWidth}px)` },
          ml: { lg: `${drawerWidth}px` },
          bgcolor: alpha(colors.canvas, 0.82),
          backdropFilter: "blur(18px)",
          border: 0,
          borderBottom: `1px solid ${colors.borderSoft}`,
          boxShadow: "none",
        }}
      >
        <Toolbar sx={{ minHeight: { xs: 64, sm: 72 }, px: { xs: 2, md: 4 } }}>
          {!isDesktop && (
            <IconButton aria-label="Open navigation" color="inherit" edge="start" onClick={() => setMobileOpen(true)} sx={{ mr: 1 }}>
              <MenuRoundedIcon />
            </IconButton>
          )}
          <Box sx={{ flexGrow: 1 }}>
            <Typography variant="body2" color="text.secondary">Today’s workspace</Typography>
            <Typography variant="h6" lineHeight={1.2}>{pageNames[location.pathname] ?? "ClinicFlow"}</Typography>
          </Box>
          <Button component={RouterLink} to="/board" variant="outlined" startIcon={<MonitorRoundedIcon />} sx={{ display: { xs: "none", sm: "inline-flex" } }}>
            Open display
          </Button>
        </Toolbar>
      </AppBar>

      <Box component="nav" sx={{ width: { lg: drawerWidth }, flexShrink: { lg: 0 } }} aria-label="Primary navigation">
        <Drawer
          variant={isDesktop ? "permanent" : "temporary"}
          open={isDesktop || mobileOpen}
          onClose={() => setMobileOpen(false)}
          ModalProps={{ keepMounted: true }}
          sx={{ "& .MuiDrawer-paper": { width: drawerWidth, boxSizing: "border-box", border: 0, borderRight: `1px solid ${colors.borderSoft}` } }}
        >
          {drawer}
        </Drawer>
      </Box>

      <Box component="main" sx={{ flexGrow: 1, minWidth: 0, px: { xs: 2, sm: 3, md: 4.5 }, pb: 5 }}>
        <Toolbar sx={{ minHeight: { xs: 88, sm: 96 } }} />
        <Box sx={{ width: "100%", maxWidth: 1440, mx: "auto" }}><Outlet /></Box>
      </Box>
    </Box>
  );
}
