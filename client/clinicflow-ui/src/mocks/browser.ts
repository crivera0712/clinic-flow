import { setupWorker } from "msw/browser";
import { handlers } from "./handlers";

// Browser-side MSW worker for running the app against in-memory mocks
// (enabled via VITE_USE_MOCKS=true; started in main.tsx).
export const worker = setupWorker(...handlers);
