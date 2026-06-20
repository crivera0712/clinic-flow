import MenuIcon from "@mui/icons-material/Menu";
import AppBar from "@mui/material/AppBar";
import Box from "@mui/material/Box";
import Button from "@mui/material/Button";
import Chip from "@mui/material/Chip";
import Divider from "@mui/material/Divider";
import Drawer from "@mui/material/Drawer";
import IconButton from "@mui/material/IconButton";
import List from "@mui/material/List";
import ListItemButton from "@mui/material/ListItemButton";
import ListItemText from "@mui/material/ListItemText";
import Stack from "@mui/material/Stack";
import Toolbar from "@mui/material/Toolbar";
import Typography from "@mui/material/Typography";
import useMediaQuery from "@mui/material/useMediaQuery";
import { useTheme } from "@mui/material/styles";
import { useState } from "react";
import { Link as RouterLink, Outlet, useLocation } from "react-router-dom";
import { useAuth } from "../../auth/AuthContext";
import { adminColors } from "../../components/admin/adminStyles";

const drawerWidth = 260;

const navItems = [
  { label: "Schedule", to: "/" },
  { label: "Patients", to: "/patients" },
  { label: "Therapists", to: "/therapists" },
  { label: "Gym Board", to: "/board" },
];

export function ConsoleLayout() {
  const { currentUser, logout } = useAuth();
  const location = useLocation();
  const theme = useTheme();
  const isDesktop = useMediaQuery(theme.breakpoints.up("lg"));
  const [mobileOpen, setMobileOpen] = useState(false);

  const drawer = (
    <Box sx={{ height: "100%", display: "flex", flexDirection: "column", bgcolor: "#0f172a", color: adminColors.textStrong }}>
      <Box sx={{ px: 3, py: 3 }}>
        <Typography variant="overline" sx={{ color: adminColors.accent, letterSpacing: "0.24em" }}>
          Clinic Flow
        </Typography>
        <Typography variant="h5" sx={{ fontWeight: 700 }}>
          Front Desk
        </Typography>
      </Box>
      <Divider sx={{ borderColor: adminColors.border }} />
      <List sx={{ px: 2, py: 2, flexGrow: 1 }}>
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
                mb: 1,
                borderRadius: 2,
                borderLeft: selected ? `3px solid ${adminColors.accent}` : "3px solid transparent",
                pl: selected ? 1.625 : 2,
                color: selected ? adminColors.textStrong : adminColors.textSecondary,
                bgcolor: selected ? adminColors.panelSelected : "transparent",
                "&:hover": { bgcolor: selected ? adminColors.panelSelected : adminColors.accentSoft, color: adminColors.textStrong },
              }}
            >
              <ListItemText primary={item.label} />
            </ListItemButton>
          );
        })}
      </List>
      <Divider sx={{ borderColor: adminColors.border }} />
      <Stack spacing={1.5} sx={{ p: 3 }}>
        {currentUser && (
          <>
            <Chip label={currentUser.roleName} color="secondary" sx={{ width: "fit-content" }} />
            <Typography sx={{ fontWeight: 600 }}>{currentUser.username}</Typography>
          </>
        )}
      </Stack>
    </Box>
  );

  return (
    <Box sx={{ display: "flex", minHeight: "100vh", bgcolor: adminColors.pageBg }}>
      <AppBar
        position="fixed"
        elevation={0}
        sx={{
          width: { lg: `calc(100% - ${drawerWidth}px)` },
          ml: { lg: `${drawerWidth}px` },
          bgcolor: "rgba(2, 6, 23, 0.84)",
          backdropFilter: "blur(16px)",
          borderBottom: `1px solid ${adminColors.border}`,
        }}
      >
        <Toolbar sx={{ minHeight: 72, gap: 2 }}>
          {!isDesktop && (
            <IconButton color="inherit" edge="start" onClick={() => setMobileOpen(true)}>
              <MenuIcon />
            </IconButton>
          )}
          <Typography variant="h6" sx={{ fontWeight: 700, flexGrow: 1 }}>
            Clinic Data Manager
          </Typography>
          <Button color="inherit" onClick={() => void logout()}>Logout</Button>
        </Toolbar>
      </AppBar>

      <Box component="nav" sx={{ width: { lg: drawerWidth }, flexShrink: { lg: 0 } }}>
        <Drawer
          variant={isDesktop ? "permanent" : "temporary"}
          open={isDesktop ? true : mobileOpen}
          onClose={() => setMobileOpen(false)}
          ModalProps={{ keepMounted: true }}
          sx={{ "& .MuiDrawer-paper": { width: drawerWidth, boxSizing: "border-box", borderRight: `1px solid ${adminColors.border}` } }}
        >
          {drawer}
        </Drawer>
      </Box>

      <Box component="main" sx={{ flexGrow: 1, px: { xs: 2, md: 4 }, py: 4 }}>
        <Toolbar sx={{ minHeight: 72 }} />
        <Outlet />
      </Box>
    </Box>
  );
}
