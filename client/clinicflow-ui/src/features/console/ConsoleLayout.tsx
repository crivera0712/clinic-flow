import CalendarMonthRoundedIcon from "@mui/icons-material/CalendarMonthRounded";
import GroupsRoundedIcon from "@mui/icons-material/GroupsRounded";
import InfoOutlinedIcon from "@mui/icons-material/InfoOutlined";
import LogoutRoundedIcon from "@mui/icons-material/LogoutRounded";
import MenuRoundedIcon from "@mui/icons-material/MenuRounded";
import MonitorRoundedIcon from "@mui/icons-material/MonitorRounded";
import PersonRoundedIcon from "@mui/icons-material/PersonRounded";
import AppBar from "@mui/material/AppBar";
import Avatar from "@mui/material/Avatar";
import Box from "@mui/material/Box";
import Chip from "@mui/material/Chip";
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
import { useTheme } from "@mui/material/styles";
import { useEffect, useState } from "react";
import { Link as RouterLink, Outlet, useLocation } from "react-router-dom";
import { useAuth } from "../../auth/AuthContext";
import { BrandMark } from "../../components/ui/BrandMark";
import { ColorModeToggle } from "../../components/ui/ColorModeToggle";
import type { RoleName } from "../../types/auth";

const drawerWidth = 232;
const topBarHeight = 64;

const navItems = [
  { label: "Schedule", to: "/", icon: <CalendarMonthRoundedIcon /> },
  { label: "Patients", to: "/patients", icon: <GroupsRoundedIcon /> },
  { label: "Therapists", to: "/therapists", icon: <PersonRoundedIcon /> },
  { label: "Clinic Display", to: "/board", icon: <MonitorRoundedIcon /> },
];

const roleLabels: Record<RoleName, string> = { ADMIN: "Admin", DISPLAY: "Display" };

// Today's date, kept current if the console stays open past midnight.
function useToday(): Date {
  const [today, setToday] = useState(() => new Date());
  useEffect(() => {
    const id = setInterval(() => setToday(new Date()), 60_000);
    return () => clearInterval(id);
  }, []);
  return today;
}

// Local calendar date ("2026-10-14"); toISOString() would give the UTC date.
function toLocalISODate(d: Date): string {
  return `${d.getFullYear()}-${String(d.getMonth() + 1).padStart(2, "0")}-${String(d.getDate()).padStart(2, "0")}`;
}

const longDate = new Intl.DateTimeFormat("en-US", {
  weekday: "long",
  month: "long",
  day: "numeric",
});
const shortDate = new Intl.DateTimeFormat("en-US", {
  weekday: "short",
  month: "short",
  day: "numeric",
});

export function ConsoleLayout() {
  const { currentUser, logout } = useAuth();
  const location = useLocation();
  const theme = useTheme();
  // The sidebar stays open from tablets in landscape and laptops up; narrower screens get a drawer.
  const isDesktop = useMediaQuery(theme.breakpoints.up("md"));
  const isSmUp = useMediaQuery(theme.breakpoints.up("sm"));
  const [mobileOpen, setMobileOpen] = useState(false);
  const today = useToday();

  const drawer = (
    <Stack sx={{ height: "100%", bgcolor: "background.paper" }}>
      <Stack
        direction="row"
        spacing={1.25}
        alignItems="center"
        sx={{ px: 2.5, minHeight: topBarHeight, borderBottom: 1, borderColor: "divider" }}
      >
        <BrandMark size={34} />
        <Box sx={{ minWidth: 0 }}>
          <Typography fontWeight={720} lineHeight={1.15}>
            ClinicFlow
          </Typography>
          <Typography variant="caption" color="text.secondary" noWrap component="p">
            Front desk workspace
          </Typography>
        </Box>
      </Stack>
      <List sx={{ px: 1.5, py: 2, flexGrow: 1 }}>
        {navItems.map((item) => {
          const selected = location.pathname === item.to;
          return (
            <ListItemButton
              key={item.to}
              component={RouterLink}
              to={item.to}
              selected={selected}
              aria-current={selected ? "page" : undefined}
              onClick={() => setMobileOpen(false)}
              sx={(theme) => {
                const primary = theme.vars.palette.primary.main;
                return {
                  position: "relative",
                  minHeight: 44,
                  mb: 0.5,
                  px: 1.5,
                  borderRadius: 2,
                  color: "text.secondary",
                  "&:hover": { bgcolor: theme.alpha(primary, 0.07), color: "text.primary" },
                  // Active item: tinted background, primary text and a bar on the left edge.
                  "&.Mui-selected": {
                    bgcolor: theme.alpha(primary, 0.12),
                    color: "primary.main",
                    "&:hover": { bgcolor: theme.alpha(primary, 0.16) },
                    "&::before": {
                      content: '""',
                      position: "absolute",
                      left: 0,
                      top: 10,
                      bottom: 10,
                      width: 3,
                      borderRadius: 3,
                      bgcolor: "primary.main",
                    },
                  },
                };
              }}
            >
              <ListItemIcon
                sx={{ minWidth: 36, color: selected ? "primary.main" : "text.disabled" }}
              >
                {item.icon}
              </ListItemIcon>
              <ListItemText
                primary={item.label}
                slotProps={{ primary: { fontWeight: selected ? 700 : 600, fontSize: "0.95rem" } }}
              />
            </ListItemButton>
          );
        })}
      </List>
      <Box sx={{ p: 1.5 }}>
        <Box sx={{ mb: 1 }}>
          <ColorModeToggle />
        </Box>
        <Stack
          direction="row"
          spacing={1.25}
          alignItems="center"
          sx={{
            p: 1.25,
            borderRadius: 2.5,
            bgcolor: "background.subtle",
            border: 1,
            borderColor: "divider",
          }}
        >
          <Avatar
            sx={(theme) => ({
              width: 34,
              height: 34,
              fontSize: "0.95rem",
              bgcolor: theme.alpha(theme.vars.palette.primary.main, 0.15),
              color: "primary.main",
              fontWeight: 750,
            })}
          >
            {currentUser?.username?.charAt(0).toUpperCase() ?? "U"}
          </Avatar>
          <Box sx={{ minWidth: 0, flexGrow: 1 }}>
            <Typography variant="body2" fontWeight={700} noWrap>
              {currentUser?.username}
            </Typography>
            <Typography variant="caption" color="text.secondary" component="p">
              {currentUser ? roleLabels[currentUser.roleName] : ""}
            </Typography>
          </Box>
          <Tooltip title="Log out">
            <IconButton
              aria-label="Log out"
              size="small"
              onClick={() => void logout()}
              sx={{ color: "text.secondary" }}
            >
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
        sx={(theme) => ({
          width: { md: `calc(100% - ${drawerWidth}px)` },
          ml: { md: `${drawerWidth}px` },
          color: "text.primary",
          bgcolor: theme.alpha(theme.vars.palette.background.default, 0.82),
          backdropFilter: "blur(18px)",
          border: 0,
          borderBottom: `1px solid ${theme.vars.palette.divider}`,
          boxShadow: "none",
        })}
      >
        <Toolbar sx={{ minHeight: { xs: topBarHeight }, px: { xs: 2, md: 4 }, gap: 1.5 }}>
          {!isDesktop && (
            <IconButton
              aria-label="Open navigation"
              color="inherit"
              edge="start"
              onClick={() => setMobileOpen(true)}
            >
              <MenuRoundedIcon />
            </IconButton>
          )}
          <Typography
            component="time"
            dateTime={toLocalISODate(today)}
            fontWeight={650}
            sx={{ flexGrow: 1, minWidth: 0 }}
            noWrap
          >
            {(isSmUp ? longDate : shortDate).format(today)}
          </Typography>
          {currentUser?.isDemo && (
            <Tooltip title="Shared demo clinic. You can check patients in; other changes are turned off.">
              <Chip
                size="small"
                icon={<InfoOutlinedIcon />}
                label="Demo clinic"
                sx={(theme) => ({
                  color: "info.main",
                  bgcolor: theme.alpha(theme.vars.palette.info.main, 0.1),
                  border: `1px solid ${theme.alpha(theme.vars.palette.info.main, 0.3)}`,
                  "& .MuiChip-icon": { color: "inherit" },
                })}
              />
            </Tooltip>
          )}
        </Toolbar>
      </AppBar>

      <Box
        component="nav"
        sx={{ width: { md: drawerWidth }, flexShrink: { md: 0 } }}
        aria-label="Primary navigation"
      >
        <Drawer
          variant={isDesktop ? "permanent" : "temporary"}
          open={isDesktop || mobileOpen}
          onClose={() => setMobileOpen(false)}
          ModalProps={{ keepMounted: true }}
          sx={{ "& .MuiDrawer-paper": { width: drawerWidth, boxSizing: "border-box" } }}
        >
          {drawer}
        </Drawer>
      </Box>

      <Box
        component="main"
        sx={{
          flexGrow: 1,
          minWidth: 0,
          px: { xs: 2, sm: 3, md: 4.5 },
          pt: { xs: 3, md: 4 },
          pb: 5,
        }}
      >
        <Toolbar sx={{ minHeight: { xs: topBarHeight } }} />
        <Box sx={{ width: "100%", maxWidth: 1440, mx: "auto" }}>
          <Outlet />
        </Box>
      </Box>
    </Box>
  );
}
