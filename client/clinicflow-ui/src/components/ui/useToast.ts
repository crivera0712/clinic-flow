import { createContext, useContext } from "react";

export type ToastApi = { success: (message: string) => void; error: (message: string) => void };

export const ToastContext = createContext<ToastApi | null>(null);

// Show an app-wide toast: `toast.success("Saved.")`, `toast.error(message)`.
export function useToast(): ToastApi {
  const context = useContext(ToastContext);
  if (!context) {
    throw new Error("useToast must be used within a ToastProvider");
  }
  return context;
}
