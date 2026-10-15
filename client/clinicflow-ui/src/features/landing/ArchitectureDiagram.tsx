import Box from "@mui/material/Box";

// How a request flows through the deployed app. Drawn as inline SVG so it stays sharp at any
// size and takes its colors from the theme (light and dark). Vertical, so it also fits a phone.
export function ArchitectureDiagram() {
  return (
    <Box
      component="svg"
      viewBox="0 0 360 452"
      role="img"
      aria-labelledby="architecture-title architecture-desc"
      sx={(theme) => {
        const p = theme.vars.palette;
        return {
          width: "100%",
          maxWidth: 420,
          height: "auto",
          display: "block",
          mx: "auto",
          fontFamily: theme.typography.fontFamily,
          "& .node": { fill: p.background.paper, stroke: p.border.strong, strokeWidth: 1.25 },
          "& .group": {
            fill: theme.alpha(p.primary.main, 0.05),
            stroke: theme.alpha(p.primary.main, 0.45),
            strokeDasharray: "6 5",
          },
          "& .title": { fill: p.text.primary, fontSize: 15, fontWeight: 700 },
          "& .detail": { fill: p.text.secondary, fontSize: 11.5, fontWeight: 500 },
          "& .group-label": { fill: p.primary.main, fontSize: 11.5, fontWeight: 700 },
          "& .edge": { stroke: p.text.disabled, strokeWidth: 1.5, fill: "none" },
          "& .edge-dashed": { strokeDasharray: "4 4" },
          "& .edge-label": { fill: p.text.secondary, fontSize: 11, fontWeight: 600 },
          "& .arrowhead": { fill: p.text.disabled },
        };
      }}
    >
      <title id="architecture-title">ClinicFlow architecture</title>
      <desc id="architecture-desc">
        The browser loads the React app from Render and calls the API over HTTPS with a JWT. On an
        AWS EC2 instance running Docker, Caddy terminates TLS and forwards to the Spring Boot API,
        which stores data in PostgreSQL.
      </desc>
      <defs>
        <marker
          id="architecture-arrow"
          viewBox="0 0 10 10"
          refX="9"
          refY="5"
          markerWidth="7"
          markerHeight="7"
          orient="auto-start-reverse"
        >
          <path className="arrowhead" d="M0,0 L10,5 L0,10 z" />
        </marker>
      </defs>

      {/* Browser and the static host */}
      <rect className="node" x="12" y="12" width="196" height="66" rx="12" />
      <text className="title" x="28" y="40">
        Browser
      </text>
      <text className="detail" x="28" y="60">
        React · MUI · TanStack Query
      </text>

      <rect className="node" x="232" y="12" width="116" height="66" rx="12" />
      <text className="title" x="246" y="40">
        Render
      </text>
      <text className="detail" x="246" y="60">
        Static site
      </text>
      <path className="edge edge-dashed" d="M232 45 H212" markerEnd="url(#architecture-arrow)" />

      {/* Browser to the API host */}
      <path className="edge" d="M110 78 V140" markerEnd="url(#architecture-arrow)" />
      <text className="edge-label" x="120" y="108">
        HTTPS · JWT access token
      </text>
      <text className="edge-label" x="120" y="124">
        + HttpOnly refresh cookie
      </text>

      {/* EC2 host */}
      <rect className="group" x="12" y="144" width="336" height="296" rx="16" />
      <text className="group-label" x="28" y="168">
        AWS EC2 · Docker
      </text>

      <rect className="node" x="28" y="180" width="304" height="58" rx="12" />
      <text className="title" x="44" y="205">
        Caddy
      </text>
      <text className="detail" x="44" y="224">
        TLS termination, reverse proxy
      </text>
      <path className="edge" d="M180 238 V266" markerEnd="url(#architecture-arrow)" />

      <rect className="node" x="28" y="270" width="304" height="74" rx="12" />
      <text className="title" x="44" y="295">
        Spring Boot API
      </text>
      <text className="detail" x="44" y="314">
        Spring Security · server-side sessions
      </text>
      <text className="detail" x="44" y="331">
        Every query scoped to the signed-in clinic
      </text>
      <path className="edge" d="M180 344 V372" markerEnd="url(#architecture-arrow)" />

      <rect className="node" x="28" y="376" width="304" height="52" rx="12" />
      <text className="title" x="44" y="399">
        PostgreSQL
      </text>
      <text className="detail" x="44" y="417">
        Schema managed by Flyway migrations
      </text>
    </Box>
  );
}
