import { useState, type ReactNode } from "react";
import BoltRoundedIcon from "@mui/icons-material/BoltRounded";
import CheckCircleRoundedIcon from "@mui/icons-material/CheckCircleRounded";
import ContrastRoundedIcon from "@mui/icons-material/ContrastRounded";
import DirectionsWalkRoundedIcon from "@mui/icons-material/DirectionsWalkRounded";
import DomainRoundedIcon from "@mui/icons-material/DomainRounded";
import EventNoteRoundedIcon from "@mui/icons-material/EventNoteRounded";
import GitHubIcon from "@mui/icons-material/GitHub";
import GroupsRoundedIcon from "@mui/icons-material/GroupsRounded";
import HowToRegRoundedIcon from "@mui/icons-material/HowToRegRounded";
import MonitorRoundedIcon from "@mui/icons-material/MonitorRounded";
import PlaylistAddRoundedIcon from "@mui/icons-material/PlaylistAddRounded";
import TvRoundedIcon from "@mui/icons-material/TvRounded";
import Box from "@mui/material/Box";
import Button from "@mui/material/Button";
import CircularProgress from "@mui/material/CircularProgress";
import IconButton from "@mui/material/IconButton";
import Link from "@mui/material/Link";
import Paper from "@mui/material/Paper";
import Stack from "@mui/material/Stack";
import Tooltip from "@mui/material/Tooltip";
import Typography from "@mui/material/Typography";
import { Link as RouterLink } from "react-router-dom";
import { useAuth } from "../../auth/AuthContext";
import { BrandMark } from "../../components/ui/BrandMark";
import { ColorModeIconButton } from "../../components/ui/ColorModeToggle";
import { useToast } from "../../components/ui/useToast";
import type { RoleName } from "../../types/auth";
import { ArchitectureDiagram } from "./ArchitectureDiagram";

const GITHUB_URL = "https://github.com/crivera0712/clinic-flow";

const pageWidth = { width: "100%", maxWidth: 1180, mx: "auto", px: { xs: 2, sm: 3, md: 4 } };

const steps = [
  {
    icon: <PlaylistAddRoundedIcon />,
    title: "Book the day",
    body: "Quick add puts a visit on the schedule in seconds. New patients are created as you type their name.",
  },
  {
    icon: <HowToRegRoundedIcon />,
    title: "Check patients in",
    body: "One click marks a patient as arrived, then starts their visit. The row updates instantly.",
  },
  {
    icon: <TvRoundedIcon />,
    title: "The board follows",
    body: "The screen in the gym shows who's waiting and who's next, refreshing every 30 seconds, so aides can start the visit.",
  },
];

const features = [
  {
    icon: <EventNoteRoundedIcon />,
    title: "The day at a glance",
    body: "Visits grouped by time, a summary that doubles as a filter, and a date stepper.",
  },
  {
    icon: <CheckCircleRoundedIcon />,
    title: "Instant check-in",
    body: "Status changes show immediately and roll back with a clear message if the server says no.",
  },
  {
    icon: <MonitorRoundedIcon />,
    title: "A board built for a TV",
    body: "Sized to read across the room, first name and last initial only, and a live update indicator.",
  },
  {
    icon: <GroupsRoundedIcon />,
    title: "Patients and care team",
    body: "Search patients, edit them in a side panel, and give each therapist a color that matches everywhere.",
  },
  {
    icon: <ContrastRoundedIcon />,
    title: "Light and dark",
    body: "One set of design tokens drives both themes, with contrast checked against WCAG AA.",
  },
  {
    icon: <DomainRoundedIcon />,
    title: "Many clinics, one app",
    body: "Each clinic's staff only ever see their own clinic's schedule, patients and therapists.",
  },
];

// Why ClinicFlow exists: the check-in problem at the clinic it was built for.
const story = [
  {
    icon: <DirectionsWalkRoundedIcon />,
    label: "Before",
    title: "Every check-in meant a walk",
    body: "The front desk carried each patient's chart back to the gym. That meant stopping whatever they were doing, or making the patient wait until they were free. Check-in slowed down, and so did everyone working.",
  },
  {
    icon: <BoltRoundedIcon />,
    label: "Now",
    title: "The gym sees it right away",
    body: "The front desk checks the patient in on the schedule, and within 30 seconds the screen in the gym shows them as waiting. Aides grab the chart and start the visit themselves, without anyone having to tell them a patient is here.",
  },
];

const stack = [
  {
    area: "Frontend",
    items: "React 19, TypeScript, MUI 7 with a CSS-variable theme, TanStack Query, Vite",
  },
  {
    area: "Backend",
    items: "Spring Boot 3.5 on Java 17, Spring Security, MapStruct, PostgreSQL 16 with Flyway",
  },
  {
    area: "Auth",
    items:
      "Short-lived JWT access tokens and HttpOnly refresh cookies, backed by server-side sessions that are revoked on logout",
  },
  {
    area: "Multi-tenancy",
    items: "Every query is scoped to the signed-in clinic, taken from the authenticated principal",
  },
  {
    area: "Quality",
    items: "Vitest with MSW, JUnit with Mockito, lint and format checks, GitHub Actions CI",
  },
  {
    area: "Deploy",
    items: "API and database in Docker on AWS EC2 behind Caddy (TLS); frontend on Render",
  },
];

// A product screenshot in a simple browser frame. The light and dark captures swap with the
// color mode.
function Screenshot({ name, alt }: { name: string; alt: string }) {
  return (
    <Paper
      sx={(theme) => ({
        overflow: "hidden",
        borderRadius: 1,
        boxShadow: theme.vars.palette.elevation.overlay,
      })}
    >
      <Stack
        direction="row"
        spacing={0.75}
        alignItems="center"
        sx={{
          px: 1.5,
          py: 1,
          borderBottom: 1,
          borderColor: "divider",
          bgcolor: "background.subtle",
        }}
        aria-hidden="true"
      >
        {[0, 1, 2].map((dot) => (
          <Box
            key={dot}
            sx={{ width: 9, height: 9, borderRadius: "50%", bgcolor: "border.strong" }}
          />
        ))}
      </Stack>
      <Box
        component="img"
        src={`/landing/${name}-light.jpg`}
        alt={alt}
        width={1440}
        height={900}
        sx={(theme) => ({
          display: "block",
          width: "100%",
          height: "auto",
          ...theme.applyStyles("dark", { display: "none" }),
        })}
      />
      <Box
        component="img"
        src={`/landing/${name}-dark.jpg`}
        alt={alt}
        width={1440}
        height={900}
        loading="lazy"
        sx={(theme) => ({
          display: "none",
          width: "100%",
          height: "auto",
          ...theme.applyStyles("dark", { display: "block" }),
        })}
      />
    </Paper>
  );
}

function SectionHeading({
  id,
  eyebrow,
  title,
  children,
}: {
  id: string;
  eyebrow: string;
  title: string;
  children?: ReactNode;
}) {
  return (
    <Box sx={{ maxWidth: 680 }}>
      <Typography variant="overline" color="primary.main">
        {eyebrow}
      </Typography>
      <Typography id={id} variant="h4" component="h2" sx={{ mt: 0.5 }}>
        {title}
      </Typography>
      {children && (
        <Typography color="text.secondary" sx={{ mt: 1.5, fontSize: "1.05rem", lineHeight: 1.7 }}>
          {children}
        </Typography>
      )}
    </Box>
  );
}

function IconTile({ children }: { children: ReactNode }) {
  return (
    <Box
      sx={(theme) => ({
        width: 40,
        height: 40,
        borderRadius: 2,
        display: "grid",
        placeItems: "center",
        color: "primary.main",
        bgcolor: theme.alpha(theme.vars.palette.primary.main, 0.11),
      })}
    >
      {children}
    </Box>
  );
}

export default function LandingPage() {
  const { loginAsDemo } = useAuth();
  const toast = useToast();
  const [pendingRole, setPendingRole] = useState<RoleName | null>(null);

  // One click into the shared demo clinic. On success the app switches to the console (front
  // desk) or the board (display) on its own.
  async function tryDemo(role: RoleName) {
    setPendingRole(role);
    try {
      await loginAsDemo(role);
    } catch (error) {
      toast.error(
        error instanceof Error
          ? error.message
          : "The demo isn't available right now. Try again soon.",
      );
      setPendingRole(null);
    }
  }

  function demoButton(role: RoleName, label: string, variant: "contained" | "outlined") {
    return (
      <Button
        size="large"
        variant={variant}
        disabled={pendingRole !== null}
        onClick={() => void tryDemo(role)}
        startIcon={
          pendingRole === role ? <CircularProgress size={18} color="inherit" /> : undefined
        }
        sx={{ minHeight: 52, px: 3 }}
      >
        {label}
      </Button>
    );
  }

  return (
    <Box sx={{ minHeight: "100vh", bgcolor: "background.default", color: "text.primary" }}>
      {/* Top bar */}
      <Box
        component="header"
        sx={(theme) => ({
          position: "sticky",
          top: 0,
          zIndex: theme.zIndex.appBar,
          bgcolor: theme.alpha(theme.vars.palette.background.default, 0.85),
          backdropFilter: "blur(14px)",
          borderBottom: 1,
          borderColor: "divider",
        })}
      >
        <Stack direction="row" alignItems="center" spacing={1} sx={{ ...pageWidth, minHeight: 64 }}>
          <Stack direction="row" spacing={1.25} alignItems="center" sx={{ flexGrow: 1 }}>
            <BrandMark size={32} />
            <Typography fontWeight={720}>ClinicFlow</Typography>
          </Stack>
          <Stack
            component="nav"
            aria-label="Page sections"
            direction="row"
            spacing={0.5}
            sx={{ display: { xs: "none", md: "flex" } }}
          >
            {[
              ["How it works", "#how-it-works"],
              ["Features", "#features"],
              ["Under the hood", "#under-the-hood"],
            ].map(([label, href]) => (
              <Button key={href} href={href} color="inherit" sx={{ color: "text.secondary" }}>
                {label}
              </Button>
            ))}
          </Stack>
          <ColorModeIconButton />
          <Tooltip title="Source on GitHub">
            <IconButton
              component="a"
              href={GITHUB_URL}
              target="_blank"
              rel="noreferrer"
              aria-label="Source on GitHub"
              sx={{ color: "text.secondary" }}
            >
              <GitHubIcon />
            </IconButton>
          </Tooltip>
          <Button component={RouterLink} to="/login" variant="outlined">
            Sign in
          </Button>
        </Stack>
      </Box>

      <Box component="main">
        {/* Hero */}
        <Box
          component="section"
          aria-labelledby="hero-title"
          sx={(theme) => ({
            background: `radial-gradient(circle at 10% 0%, ${theme.alpha(theme.vars.palette.primary.main, 0.12)}, transparent 45%)`,
          })}
        >
          <Box
            sx={{
              ...pageWidth,
              py: { xs: 6, md: 10 },
              display: "grid",
              gridTemplateColumns: { xs: "1fr", md: "5fr 7fr" },
              gap: { xs: 5, md: 6 },
              alignItems: "center",
            }}
          >
            <Box>
              <Typography variant="overline" color="primary.main">
                Scheduling for physical therapy clinics
              </Typography>
              <Typography
                id="hero-title"
                variant="h2"
                component="h1"
                sx={{ mt: 1, fontSize: { xs: "2.4rem", md: "3.2rem" }, lineHeight: 1.1 }}
              >
                Run the front desk and the gym from one schedule.
              </Typography>
              <Typography
                color="text.secondary"
                sx={{ mt: 2.5, fontSize: "1.15rem", lineHeight: 1.7, maxWidth: 520 }}
              >
                ClinicFlow keeps today's visits, check-ins and the screen in the gym in sync, so the
                front desk stays at the desk and aides always know who's ready to start.
              </Typography>
              <Stack direction={{ xs: "column", sm: "row" }} spacing={1.5} sx={{ mt: 4 }}>
                {demoButton("ADMIN", "Try as Front Desk", "contained")}
                {demoButton("DISPLAY", "Try as Display", "outlined")}
              </Stack>
              <Typography variant="body2" color="text.secondary" sx={{ mt: 2 }}>
                No sign-up. Opens a shared demo clinic with a fresh day of visits.
              </Typography>
            </Box>
            <Screenshot
              name="schedule"
              alt="The ClinicFlow schedule: a day summary, quick add, and visits grouped by time with check-in buttons."
            />
          </Box>
        </Box>

        {/* How it works */}
        <Box
          component="section"
          id="how-it-works"
          aria-labelledby="how-it-works-title"
          sx={{ ...pageWidth, py: { xs: 7, md: 10 }, scrollMarginTop: 64 }}
        >
          <SectionHeading
            id="how-it-works-title"
            eyebrow="How it works"
            title="From the front desk to the gym"
          >
            The front desk works from the schedule; the screen in the gym reads the same data.
          </SectionHeading>
          <Box
            sx={{
              mt: 5,
              display: "grid",
              gridTemplateColumns: { xs: "1fr", md: "5fr 7fr" },
              gap: { xs: 4, md: 6 },
              alignItems: "center",
            }}
          >
            <Stack component="ol" spacing={3} sx={{ m: 0, p: 0, listStyle: "none" }}>
              {steps.map((step, index) => (
                <Stack component="li" key={step.title} direction="row" spacing={2}>
                  <IconTile>{step.icon}</IconTile>
                  <Box>
                    <Typography fontWeight={720}>
                      {index + 1}. {step.title}
                    </Typography>
                    <Typography color="text.secondary" sx={{ mt: 0.5, lineHeight: 1.7 }}>
                      {step.body}
                    </Typography>
                  </Box>
                </Stack>
              ))}
            </Stack>
            <Screenshot
              name="board"
              alt="The ClinicFlow board for the gym screen: patients currently waiting and up next, by first name and last initial."
            />
          </Box>
        </Box>

        {/* Features */}
        <Box
          component="section"
          id="features"
          aria-labelledby="features-title"
          sx={{
            bgcolor: "background.subtle",
            borderTop: 1,
            borderBottom: 1,
            borderColor: "divider",
            scrollMarginTop: 64,
          }}
        >
          <Box sx={{ ...pageWidth, py: { xs: 7, md: 10 } }}>
            <SectionHeading
              id="features-title"
              eyebrow="Features"
              title="Built for a busy front desk"
            />
            <Box
              sx={{
                mt: 5,
                display: "grid",
                gridTemplateColumns: { xs: "1fr", sm: "repeat(2, 1fr)", md: "repeat(3, 1fr)" },
                gap: 2,
              }}
            >
              {features.map((feature) => (
                <Paper key={feature.title} sx={{ p: 3 }}>
                  <IconTile>{feature.icon}</IconTile>
                  <Typography component="h3" fontWeight={720} sx={{ mt: 2 }}>
                    {feature.title}
                  </Typography>
                  <Typography color="text.secondary" sx={{ mt: 0.75, lineHeight: 1.7 }}>
                    {feature.body}
                  </Typography>
                </Paper>
              ))}
            </Box>
          </Box>
        </Box>

        {/* Why it was built */}
        <Box
          component="section"
          id="why"
          aria-labelledby="why-title"
          sx={{ ...pageWidth, py: { xs: 7, md: 10 }, scrollMarginTop: 64 }}
        >
          <SectionHeading
            id="why-title"
            eyebrow="Why I built it"
            title="Made for a clinic in Corte Madera"
          >
            ClinicFlow started at a physical therapy clinic in Corte Madera, California, where it
            runs today. The office is shaped like an L: check-in and the waiting room on one side,
            the gym and staff area on the other.
          </SectionHeading>
          <Box
            sx={{
              mt: 5,
              display: "grid",
              gridTemplateColumns: { xs: "1fr", md: "repeat(2, 1fr)" },
              gap: 2,
            }}
          >
            {story.map((part) => (
              <Paper key={part.label} sx={{ p: 3 }}>
                <Stack direction="row" spacing={1.5} alignItems="center">
                  <IconTile>{part.icon}</IconTile>
                  <Typography variant="overline" color="text.secondary">
                    {part.label}
                  </Typography>
                </Stack>
                <Typography component="h3" fontWeight={720} sx={{ mt: 2 }}>
                  {part.title}
                </Typography>
                <Typography color="text.secondary" sx={{ mt: 0.75, lineHeight: 1.7 }}>
                  {part.body}
                </Typography>
              </Paper>
            ))}
          </Box>
        </Box>

        {/* Under the hood */}
        <Box
          component="section"
          id="under-the-hood"
          aria-labelledby="under-the-hood-title"
          sx={{ borderTop: 1, borderColor: "divider", scrollMarginTop: 64 }}
        >
          <Box sx={{ ...pageWidth, py: { xs: 7, md: 10 } }}>
            <SectionHeading
              id="under-the-hood-title"
              eyebrow="Under the hood"
              title="How it's built"
            >
              A Spring Boot API and a React client, deployed as a real multi-tenant app.
            </SectionHeading>
            <Box
              sx={{
                mt: 5,
                display: "grid",
                gridTemplateColumns: { xs: "1fr", md: "7fr 5fr" },
                gap: { xs: 5, md: 6 },
                alignItems: "start",
              }}
            >
              <Box component="dl" sx={{ m: 0 }}>
                {stack.map((row) => (
                  <Box
                    key={row.area}
                    sx={{
                      display: "grid",
                      gridTemplateColumns: { xs: "1fr", sm: "140px 1fr" },
                      gap: { xs: 0.25, sm: 2 },
                      py: 1.75,
                      borderBottom: 1,
                      borderColor: "divider",
                    }}
                  >
                    <Typography component="dt" fontWeight={720}>
                      {row.area}
                    </Typography>
                    <Typography
                      component="dd"
                      color="text.secondary"
                      sx={{ m: 0, lineHeight: 1.7 }}
                    >
                      {row.items}
                    </Typography>
                  </Box>
                ))}
              </Box>
              <ArchitectureDiagram />
            </Box>
          </Box>
        </Box>
      </Box>

      {/* Footer */}
      <Box component="footer" sx={{ borderTop: 1, borderColor: "divider" }}>
        <Stack
          direction={{ xs: "column", sm: "row" }}
          spacing={2}
          alignItems={{ sm: "center" }}
          justifyContent="space-between"
          sx={{ ...pageWidth, py: 4 }}
        >
          <Stack direction="row" spacing={1.25} alignItems="center">
            <BrandMark size={26} />
            <Typography fontWeight={700}>ClinicFlow</Typography>
          </Stack>
          <Stack direction="row" spacing={3}>
            <Link
              href={GITHUB_URL}
              target="_blank"
              rel="noreferrer"
              underline="hover"
              color="text.secondary"
            >
              Source on GitHub
            </Link>
            <Link component={RouterLink} to="/login" underline="hover" color="text.secondary">
              Sign in
            </Link>
          </Stack>
        </Stack>
      </Box>
    </Box>
  );
}
