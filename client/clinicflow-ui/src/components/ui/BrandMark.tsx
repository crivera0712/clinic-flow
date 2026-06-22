import Box from "@mui/material/Box";
import { colors } from "../../theme";

export function BrandMark({ size = 42 }: { size?: number }) {
  return (
    <Box
      aria-hidden="true"
      sx={{
        width: size,
        height: size,
        flex: `0 0 ${size}px`,
        borderRadius: `${Math.round(size * 0.3)}px`,
        display: "grid",
        placeItems: "center",
        background: `linear-gradient(145deg, ${colors.primary}, ${colors.primaryStrong})`,
        boxShadow: "0 10px 30px rgba(14, 165, 233, .24)",
        position: "relative",
        "&::before, &::after": {
          content: '""',
          position: "absolute",
          bgcolor: "#03111b",
          borderRadius: 99,
        },
        "&::before": { width: "48%", height: "16%" },
        "&::after": { width: "16%", height: "48%" },
      }}
    />
  );
}
