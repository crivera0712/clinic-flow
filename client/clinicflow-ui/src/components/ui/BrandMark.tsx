import Box from "@mui/material/Box";

export function BrandMark({ size = 42 }: { size?: number }) {
  return (
    <Box
      aria-hidden="true"
      sx={(theme) => ({
        width: size,
        height: size,
        flex: `0 0 ${size}px`,
        borderRadius: `${Math.round(size * 0.3)}px`,
        display: "grid",
        placeItems: "center",
        background: `linear-gradient(145deg, ${theme.vars.palette.primary.main}, ${theme.vars.palette.primary.dark})`,
        boxShadow: `0 10px 30px ${theme.alpha(theme.vars.palette.primary.main, 0.24)}`,
        position: "relative",
        "&::before, &::after": {
          content: '""',
          position: "absolute",
          bgcolor: "primary.contrastText",
          borderRadius: 99,
        },
        "&::before": { width: "48%", height: "16%" },
        "&::after": { width: "16%", height: "48%" },
      })}
    />
  );
}
