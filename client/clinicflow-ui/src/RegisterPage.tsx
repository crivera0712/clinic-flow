import { useState } from "react";
import Alert from "@mui/material/Alert";
import Box from "@mui/material/Box";
import Button from "@mui/material/Button";
import CircularProgress from "@mui/material/CircularProgress";
import Paper from "@mui/material/Paper";
import Stack from "@mui/material/Stack";
import TextField from "@mui/material/TextField";
import Typography from "@mui/material/Typography";
import { useNavigate, useParams } from "react-router-dom";
import { register } from "./api/auth";
import { ApiError } from "./api/client";

const fieldSx = {
  "& .MuiInputLabel-root": { color: "#94a3b8" },
  "& .MuiInputLabel-root.MuiInputLabel-shrink": {
    px: 0.75,
    backgroundColor: "rgba(15, 23, 42, 0.92)",
  },
  "& .MuiOutlinedInput-root": {
    color: "#e2e8f0",
    backgroundColor: "rgba(15, 23, 42, 0.45)",
    "& fieldset": { borderColor: "rgba(148, 163, 184, 0.28)" },
    "&:hover fieldset": { borderColor: "rgba(56, 189, 248, 0.55)" },
    "&.Mui-focused fieldset": { borderColor: "#38bdf8" },
  },
  "& .MuiOutlinedInput-input:-webkit-autofill": {
    WebkitBoxShadow: "0 0 0 100px rgba(15, 23, 42, 0.45) inset",
    WebkitTextFillColor: "#e2e8f0",
    caretColor: "#e2e8f0",
    borderRadius: "inherit",
  },
};

export default function RegisterPage() {
  const { clinicSlug } = useParams<{ clinicSlug: string }>();
  const navigate = useNavigate();
  const [username, setUsername] = useState("");
  const [password, setPassword] = useState("");
  const [submitting, setSubmitting] = useState(false);
  const [error, setError] = useState<string | null>(null);

  async function handleSubmit(event: React.FormEvent<HTMLFormElement>) {
    event.preventDefault();
    if (!clinicSlug) return;
    setSubmitting(true);
    setError(null);

    try {
      await register(clinicSlug, { username, passwordHash: password });
      navigate("/", { replace: true });
    } catch (submitError) {
      if (submitError instanceof ApiError) {
        setError(submitError.message);
      } else {
        setError("Unable to create account right now.");
      }
    } finally {
      setSubmitting(false);
    }
  }

  return (
    <Box
      sx={{
        minHeight: "100vh",
        display: "grid",
        placeItems: "center",
        px: 2,
        background:
          "radial-gradient(circle at top, rgba(67, 56, 202, 0.18), transparent 35%), linear-gradient(180deg, #09111f 0%, #0f172a 100%)",
      }}
    >
      <Paper
        elevation={0}
        sx={{
          width: "100%",
          maxWidth: 460,
          p: 4,
          borderRadius: 4,
          color: "#e2e8f0",
          backgroundColor: "rgba(15, 23, 42, 0.92)",
          border: "1px solid rgba(148, 163, 184, 0.18)",
          boxShadow: "0 24px 60px rgba(15, 23, 42, 0.45)",
        }}
      >
        <Stack spacing={3} component="form" onSubmit={handleSubmit}>
          <Box>
            <Typography variant="overline" sx={{ color: "#38bdf8", letterSpacing: "0.22em" }}>
              Clinic Flow
            </Typography>
            <Typography variant="h4" sx={{ mt: 1, fontWeight: 700 }}>
              Create account
            </Typography>
            <Typography sx={{ mt: 1, color: "#94a3b8" }}>
              Register for <strong>{clinicSlug}</strong>. You'll be able to sign in after your account is created.
            </Typography>
          </Box>

          {error && <Alert severity="error">{error}</Alert>}

          <TextField
            label="Username"
            value={username}
            onChange={(e) => setUsername(e.target.value)}
            autoComplete="username"
            variant="outlined"
            slotProps={{ inputLabel: { shrink: true } }}
            sx={fieldSx}
            fullWidth
            required
          />

          <TextField
            label="Password"
            type="password"
            value={password}
            onChange={(e) => setPassword(e.target.value)}
            autoComplete="new-password"
            variant="outlined"
            slotProps={{ inputLabel: { shrink: true } }}
            sx={fieldSx}
            fullWidth
            required
          />

          <Button type="submit" variant="contained" size="large" disabled={submitting}>
            {submitting ? <CircularProgress size={24} color="inherit" /> : "Create account"}
          </Button>
        </Stack>
      </Paper>
    </Box>
  );
}
