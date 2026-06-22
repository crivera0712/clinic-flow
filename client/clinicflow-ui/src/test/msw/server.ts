import { setupServer } from "msw/node";
import { handlers } from "./handlers";

// Shared MSW server for all tests. Lifecycle (listen/resetHandlers/close) is
// managed in src/test/setup.ts.
export const server = setupServer(...handlers);
