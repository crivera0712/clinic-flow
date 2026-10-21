import type { ReactNode } from "react";
import ArrowBackRoundedIcon from "@mui/icons-material/ArrowBackRounded";
import Box from "@mui/material/Box";
import Link from "@mui/material/Link";
import Paper from "@mui/material/Paper";
import Stack from "@mui/material/Stack";
import Typography from "@mui/material/Typography";
import { Link as RouterLink } from "react-router-dom";
import { BrandMark } from "./BrandMark";

// Centered card for sign-in and registration: brand on top (links home), the form, and a way
// back to the landing page.
export function AuthShell({ children }: { children: ReactNode }) {
  return (
    <Box
      sx={(theme) => ({
        minHeight: "100vh",
        display: "grid",
        placeItems: "center",
        p: { xs: 2, sm: 4 },
        background: `radial-gradient(circle at 15% 10%, ${theme.alpha(theme.vars.palette.primary.main, 0.1)}, transparent 35%), ${theme.vars.palette.background.default}`,
      })}
    >
      <Stack spacing={3} sx={{ width: "100%", maxWidth: 420 }}>
        <Stack
          component={RouterLink}
          to="/"
          direction="row"
          spacing={1.25}
          alignItems="center"
          justifyContent="center"
          sx={{ color: "text.primary", textDecoration: "none" }}
        >
          <BrandMark size={36} />
          <Typography variant="h6" component="span">
            ClinicFlow
          </Typography>
        </Stack>
        <Paper
          sx={(theme) => ({
            p: { xs: 3, sm: 4 },
            boxShadow: theme.vars.palette.elevation.raised,
          })}
        >
          {children}
        </Paper>
        <Link
          component={RouterLink}
          to="/"
          underline="hover"
          sx={{
            alignSelf: "center",
            display: "inline-flex",
            alignItems: "center",
            gap: 0.75,
            color: "text.secondary",
            fontWeight: 600,
          }}
        >
          <ArrowBackRoundedIcon fontSize="small" />
          Back to home
        </Link>
      </Stack>
    </Box>
  );
}
