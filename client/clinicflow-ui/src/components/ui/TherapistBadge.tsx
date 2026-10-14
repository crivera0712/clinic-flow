import Box from "@mui/material/Box";
import Stack from "@mui/material/Stack";
import Typography, { type TypographyProps } from "@mui/material/Typography";

// Therapist name with their accent dot. Get `accent` from useTherapistAccents().
export function TherapistBadge({
  name,
  accent,
  ...typographyProps
}: { name: string; accent?: string } & Omit<TypographyProps, "children">) {
  return (
    <Stack direction="row" spacing={1} alignItems="center" component="span" sx={{ minWidth: 0 }}>
      <Box
        component="span"
        aria-hidden="true"
        sx={{
          width: 10,
          height: 10,
          flexShrink: 0,
          borderRadius: "50%",
          bgcolor: accent ?? "text.disabled",
        }}
      />
      <Typography component="span" variant="body2" noWrap {...typographyProps}>
        {name}
      </Typography>
    </Stack>
  );
}
