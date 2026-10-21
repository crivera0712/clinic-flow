import type { ReactElement } from "react";
import Tooltip from "@mui/material/Tooltip";

// Wraps an action that's disabled in the demo clinic with a "Read-only in the demo" tooltip.
// Disabled buttons don't fire mouse events, so the tooltip hangs on a wrapping span.
export function DemoReadOnly({ active, children }: { active: boolean; children: ReactElement }) {
  if (!active) return children;
  return (
    <Tooltip title="Read-only in the demo">
      <span style={{ display: "inline-flex" }}>{children}</span>
    </Tooltip>
  );
}
