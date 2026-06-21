import type { ReactNode } from "react";
import Box from "@mui/material/Box";
import Typography from "@mui/material/Typography";

export function EmptyState({ icon, title, description }: { icon?: ReactNode; title: string; description?: string }) {
  return (
    <Box sx={{ py: 7, px: 3, textAlign: "center", color: "text.secondary" }}>
      {icon && <Box sx={{ color: "text.disabled", mb: 1 }}>{icon}</Box>}
      <Typography variant="h6" color="text.primary">{title}</Typography>
      {description && <Typography sx={{ mt: 0.75 }}>{description}</Typography>}
    </Box>
  );
}
