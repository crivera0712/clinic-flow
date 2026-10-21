import { useId, type ReactNode } from "react";
import CloseRoundedIcon from "@mui/icons-material/CloseRounded";
import Box from "@mui/material/Box";
import Button from "@mui/material/Button";
import Drawer from "@mui/material/Drawer";
import IconButton from "@mui/material/IconButton";
import Stack from "@mui/material/Stack";
import Typography from "@mui/material/Typography";

// Side drawer holding a form, used to add and edit records (full width on phones).
// Header with title and close button, scrolling fields, and Cancel / submit pinned at the bottom.
// Can't be dismissed while `saving`.
export function FormDrawer({
  open,
  title,
  subtitle,
  submitLabel = "Save",
  saving = false,
  onClose,
  onSubmit,
  children,
}: {
  open: boolean;
  title: string;
  subtitle?: string;
  submitLabel?: string;
  saving?: boolean;
  onClose: () => void;
  onSubmit: () => void;
  children: ReactNode;
}) {
  const titleId = useId();

  return (
    <Drawer
      anchor="right"
      open={open}
      onClose={saving ? undefined : onClose}
      PaperProps={{
        role: "dialog",
        "aria-labelledby": titleId,
        sx: { width: { xs: "100%", sm: 400 }, borderLeft: 1, borderColor: "divider" },
      }}
    >
      <Stack
        component="form"
        sx={{ height: "100%" }}
        onSubmit={(event) => {
          event.preventDefault();
          onSubmit();
        }}
      >
        <Stack
          direction="row"
          alignItems="flex-start"
          justifyContent="space-between"
          sx={{ px: 3, py: 2.5, borderBottom: 1, borderColor: "divider" }}
        >
          <Box>
            <Typography id={titleId} variant="h6" component="h2">
              {title}
            </Typography>
            {subtitle && (
              <Typography variant="body2" color="text.secondary">
                {subtitle}
              </Typography>
            )}
          </Box>
          <IconButton aria-label="Close" onClick={onClose} disabled={saving} edge="end">
            <CloseRoundedIcon />
          </IconButton>
        </Stack>
        <Stack spacing={2.5} sx={{ p: 3, flexGrow: 1, overflowY: "auto" }}>
          {children}
        </Stack>
        <Stack
          direction="row"
          spacing={1}
          justifyContent="flex-end"
          sx={{ px: 3, py: 2, borderTop: 1, borderColor: "divider" }}
        >
          <Button onClick={onClose} disabled={saving}>
            Cancel
          </Button>
          <Button type="submit" variant="contained" disabled={saving}>
            {submitLabel}
          </Button>
        </Stack>
      </Stack>
    </Drawer>
  );
}
