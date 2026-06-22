// Absolute API base used in tests. The app normally fetches relative `/api/...`
// URLs, but Node's fetch (used by Vitest + MSW) rejects relative URLs, so we
// point `VITE_API_BASE_URL` at this absolute base for tests only.
//
// Keep in sync with the `test.env.VITE_API_BASE_URL` literal in vite.config.ts.
export const API_BASE_URL = "http://localhost/api";

// Build an absolute API URL for an MSW handler from a path like "/patients".
export const apiUrl = (path: string) => `${API_BASE_URL}${path}`;
