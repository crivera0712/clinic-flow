import { useState } from "react";
import Alert from "@mui/material/Alert";
import Button from "@mui/material/Button";
import CircularProgress from "@mui/material/CircularProgress";
import Stack from "@mui/material/Stack";
import TextField from "@mui/material/TextField";
import Typography from "@mui/material/Typography";
import { ApiError } from "./api/client";
import { useAuth } from "./auth/AuthContext";
import { AuthShell } from "./components/ui/AuthShell";
import Link from "@mui/material/Link";
import { Link as RouterLink } from "react-router-dom";

export default function LoginPage() {
  const { login } = useAuth();
  const [username, setUsername] = useState("");
  const [password, setPassword] = useState("");
  const [submitting, setSubmitting] = useState(false);
  const [error, setError] = useState<string | null>(null);

  async function handleSubmit(event: React.FormEvent<HTMLFormElement>) {
    event.preventDefault();
    setSubmitting(true);
    setError(null);

    try {
      await login({ username, password });
    } catch (submitError) {
      if (submitError instanceof ApiError) {
        setError(submitError.message);
      } else {
        setError("Unable to sign in right now.");
      }
    } finally {
      setSubmitting(false);
    }
  }

  return (
    <AuthShell>
      <Stack spacing={3.5} component="form" onSubmit={handleSubmit}>
        <div>
          <Typography variant="h4" component="h1">
            Sign in
          </Typography>
          <Typography sx={{ mt: 1, color: "text.secondary", lineHeight: 1.7 }}>
            Sign in to your clinic's schedule.
          </Typography>
        </div>

        {error && <Alert severity="error">{error}</Alert>}

        <TextField
          id="login-username"
          label="Username"
          value={username}
          onChange={(event) => setUsername(event.target.value)}
          autoComplete="username"
          variant="outlined"
          slotProps={{ inputLabel: { shrink: true }, htmlInput: { "aria-label": "Username" } }}
          fullWidth
          required
        />

        <TextField
          id="login-password"
          label="Password"
          type="password"
          value={password}
          onChange={(event) => setPassword(event.target.value)}
          autoComplete="current-password"
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
          {submitting ? <CircularProgress size={24} color="inherit" /> : "Sign in"}
        </Button>

        <Typography variant="body2" color="text.secondary" textAlign="center">
          Just looking?{" "}
          <Link component={RouterLink} to="/" fontWeight={650}>
            Try the demo
          </Link>
        </Typography>
      </Stack>
    </AuthShell>
  );
}
