import { useState } from "react";
import Alert from "@mui/material/Alert";
import Button from "@mui/material/Button";
import CircularProgress from "@mui/material/CircularProgress";
import Stack from "@mui/material/Stack";
import TextField from "@mui/material/TextField";
import Typography from "@mui/material/Typography";
import { useNavigate, useParams } from "react-router-dom";
import { register } from "./api/auth";
import { ApiError } from "./api/client";
import { AuthShell } from "./components/ui/AuthShell";
import { BrandMark } from "./components/ui/BrandMark";

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
    <AuthShell>
      <Stack spacing={3.5} component="form" onSubmit={handleSubmit}>
        <Stack direction="row" spacing={1.5} alignItems="center" sx={{ display: { md: "none" } }}>
          <BrandMark size={38} />
          <Typography variant="h6">ClinicFlow</Typography>
        </Stack>
        <div>
          <Typography variant="overline" color="primary.main">
            Join your clinic
          </Typography>
          <Typography variant="h4" sx={{ mt: 0.5 }}>
            Create account
          </Typography>
          <Typography sx={{ mt: 1, color: "text.secondary", lineHeight: 1.7 }}>
            Register for <strong>{clinicSlug}</strong>. You'll be able to sign in after your account
            is created.
          </Typography>
        </div>

        {error && <Alert severity="error">{error}</Alert>}

        <TextField
          id="register-username"
          label="Username"
          value={username}
          onChange={(e) => setUsername(e.target.value)}
          autoComplete="username"
          variant="outlined"
          slotProps={{ inputLabel: { shrink: true }, htmlInput: { "aria-label": "Username" } }}
          fullWidth
          required
        />

        <TextField
          id="register-password"
          label="Password"
          type="password"
          value={password}
          onChange={(e) => setPassword(e.target.value)}
          autoComplete="new-password"
          variant="outlined"
          slotProps={{ inputLabel: { shrink: true }, htmlInput: { "aria-label": "Password" } }}
          fullWidth
          required
        />

        <Button
          type="submit"
          variant="contained"
          size="large"
          disabled={submitting}
          sx={{ mt: 0.5 }}
        >
          {submitting ? <CircularProgress size={24} color="inherit" /> : "Create account"}
        </Button>
      </Stack>
    </AuthShell>
  );
}
