import type { ReactNode } from "react";
import Box from "@mui/material/Box";
import Paper from "@mui/material/Paper";
import Stack from "@mui/material/Stack";
import Typography from "@mui/material/Typography";

// A titled card: header (icon, title, subtitle, optional action) above a divider, then the body.
// Use `disablePadding` when the body is edge-to-edge (tables, lists, data grids).
export function SectionCard({
  icon,
  title,
  subtitle,
  action,
  disablePadding = false,
  children,
}: {
  icon?: ReactNode;
  title: string;
  subtitle?: ReactNode;
  action?: ReactNode;
  disablePadding?: boolean;
  children: ReactNode;
}) {
  return (
    <Paper
      component="section"
      aria-label={title}
      sx={(theme) => ({ overflow: "hidden", boxShadow: theme.vars.palette.elevation.card })}
    >
      <Stack
        direction={{ xs: "column", sm: "row" }}
        spacing={2}
        alignItems={{ sm: "center" }}
        justifyContent="space-between"
        sx={{ px: { xs: 2, md: 2.5 }, py: 2, borderBottom: 1, borderColor: "divider" }}
      >
        <Stack direction="row" spacing={1.25} alignItems="center" sx={{ minWidth: 0 }}>
          {icon && (
            <Box
              sx={(theme) => ({
                width: 36,
                height: 36,
                flexShrink: 0,
                borderRadius: 2,
                display: "grid",
                placeItems: "center",
                color: "primary.main",
                bgcolor: theme.alpha(theme.vars.palette.primary.main, 0.11),
              })}
            >
              {icon}
            </Box>
          )}
          <Box sx={{ minWidth: 0 }}>
            <Typography variant="h6" component="h2">
              {title}
            </Typography>
            {subtitle && (
              <Typography variant="body2" color="text.secondary">
                {subtitle}
              </Typography>
            )}
          </Box>
        </Stack>
        {action}
      </Stack>
      <Box sx={disablePadding ? undefined : { p: { xs: 2, md: 2.5 } }}>{children}</Box>
    </Paper>
  );
}
