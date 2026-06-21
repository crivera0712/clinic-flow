import type { ReactNode } from "react";
import Box from "@mui/material/Box";
import Stack from "@mui/material/Stack";
import Typography from "@mui/material/Typography";

export function PageHeader({
  eyebrow,
  title,
  description,
  action,
}: {
  eyebrow?: string;
  title: string;
  description: string;
  action?: ReactNode;
}) {
  return (
    <Stack direction={{ xs: "column", sm: "row" }} spacing={2} alignItems={{ sm: "flex-end" }} justifyContent="space-between">
      <Box>
        {eyebrow && <Typography variant="overline" color="primary.main">{eyebrow}</Typography>}
        <Typography variant="h4" sx={{ mt: eyebrow ? 0.25 : 0 }}>{title}</Typography>
        <Typography color="text.secondary" sx={{ mt: 0.75, maxWidth: 640 }}>{description}</Typography>
      </Box>
      {action}
    </Stack>
  );
}
