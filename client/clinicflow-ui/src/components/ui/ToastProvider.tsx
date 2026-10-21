import { useMemo, useState, type ReactNode } from "react";
import Alert from "@mui/material/Alert";
import Snackbar from "@mui/material/Snackbar";
import { ToastContext, type ToastApi } from "./useToast";

type ToastSeverity = "success" | "error";
type Toast = { id: number; severity: ToastSeverity; message: string };

const AUTO_HIDE_MS: Record<ToastSeverity, number> = { success: 4000, error: 6000 };

// One app-wide toast, bottom-right. A new toast replaces the one on screen.
export function ToastProvider({ children }: { children: ReactNode }) {
  const [toast, setToast] = useState<Toast | null>(null);
  const [open, setOpen] = useState(false);

  const api = useMemo<ToastApi>(() => {
    const show = (severity: ToastSeverity, message: string) => {
      setToast({ id: Date.now(), severity, message });
      setOpen(true);
    };
    return {
      success: (message) => show("success", message),
      error: (message) => show("error", message),
    };
  }, []);

  return (
    <ToastContext.Provider value={api}>
      {children}
      <Snackbar
        // Remount per toast so the auto-hide timer restarts when one replaces another.
        key={toast?.id}
        open={open}
        autoHideDuration={toast ? AUTO_HIDE_MS[toast.severity] : null}
        onClose={(_, reason) => {
          if (reason !== "clickaway") setOpen(false);
        }}
        anchorOrigin={{ vertical: "bottom", horizontal: "right" }}
      >
        {toast ? (
          <Alert onClose={() => setOpen(false)} severity={toast.severity} variant="filled">
            {toast.message}
          </Alert>
        ) : undefined}
      </Snackbar>
    </ToastContext.Provider>
  );
}
