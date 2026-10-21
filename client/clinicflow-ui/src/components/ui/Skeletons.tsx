import Box from "@mui/material/Box";
import Skeleton from "@mui/material/Skeleton";
import Stack from "@mui/material/Stack";

// Placeholder rows shown on first load, before there is any data to render.

export function ListSkeleton({ rows = 4, avatar = false }: { rows?: number; avatar?: boolean }) {
  return (
    <Stack aria-hidden="true" divider={<Box sx={{ borderBottom: 1, borderColor: "divider" }} />}>
      {Array.from({ length: rows }, (_, row) => (
        <Stack
          key={row}
          direction="row"
          spacing={2}
          alignItems="center"
          sx={{ px: { xs: 2, md: 2.5 }, py: 1.75 }}
        >
          {avatar && <Skeleton variant="circular" width={40} height={40} />}
          <Box sx={{ flexGrow: 1 }}>
            <Skeleton variant="text" width={`${40 + (row % 3) * 15}%`} />
            <Skeleton variant="text" width="30%" />
          </Box>
        </Stack>
      ))}
    </Stack>
  );
}
