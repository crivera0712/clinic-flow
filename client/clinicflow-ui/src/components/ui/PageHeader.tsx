import type { ReactNode } from "react";
import RefreshRoundedIcon from "@mui/icons-material/RefreshRounded";
import Box from "@mui/material/Box";
import IconButton from "@mui/material/IconButton";
import Stack from "@mui/material/Stack";
import Tooltip from "@mui/material/Tooltip";
import Typography from "@mui/material/Typography";
import { keyframes } from "@mui/material/styles";

const spin = keyframes`
  to { transform: rotate(360deg); }
`;

export function PageHeader({
  eyebrow,
  title,
  description,
  action,
  onRefresh,
  refreshing = false,
}: {
  eyebrow?: string;
  title: string;
  description: string;
  action?: ReactNode;
  // Shows a refresh button; the icon spins while `refreshing` is true.
  onRefresh?: () => void;
  refreshing?: boolean;
}) {
  return (
    <Stack
      direction={{ xs: "column", sm: "row" }}
      spacing={2}
      alignItems={{ sm: "flex-end" }}
      justifyContent="space-between"
    >
      <Box>
        {eyebrow && (
          <Typography variant="overline" color="primary.main">
            {eyebrow}
          </Typography>
        )}
        <Typography variant="h4" component="h1" sx={{ mt: eyebrow ? 0.25 : 0 }}>
          {title}
        </Typography>
        <Typography color="text.secondary" sx={{ mt: 0.75, maxWidth: 640 }}>
          {description}
        </Typography>
      </Box>
      {(onRefresh || action) && (
        <Stack direction="row" spacing={1} alignItems="center">
          {onRefresh && (
            <Tooltip title="Refresh">
              <span>
                <IconButton aria-label="Refresh" onClick={onRefresh} disabled={refreshing}>
                  <RefreshRoundedIcon
                    sx={{ animation: refreshing ? `${spin} 0.9s linear infinite` : "none" }}
                  />
                </IconButton>
              </span>
            </Tooltip>
          )}
          {action}
        </Stack>
      )}
    </Stack>
  );
}
