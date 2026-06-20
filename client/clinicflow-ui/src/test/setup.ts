import "@testing-library/jest-dom/vitest";
import { afterAll, afterEach, beforeAll } from "vitest";
import { cleanup } from "@testing-library/react";
import { server } from "./msw/server";

// Start the MSW server once for the whole suite. `onUnhandledRequest: "error"`
// turns any request without a matching handler (e.g. a typo'd URL) into a
// failing test.
beforeAll(() => server.listen({ onUnhandledRequest: "error" }));

// Reset request handlers added with `server.use()` and unmount React trees
// after every test so state doesn't leak between tests.
afterEach(() => {
  server.resetHandlers();
  cleanup();
});

afterAll(() => server.close());
