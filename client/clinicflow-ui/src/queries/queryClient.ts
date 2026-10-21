import { QueryClient } from "@tanstack/react-query";
import { ApiError } from "../api/client";

// Retry once for network failures and server (5xx) errors. A 4xx is a real answer (bad input,
// forbidden, not found), so retrying would only delay the error message. Writes never retry.
function shouldRetry(failureCount: number, error: Error): boolean {
  const isClientError = error instanceof ApiError && error.status >= 400 && error.status < 500;
  return !isClientError && failureCount < 1;
}

export function createQueryClient() {
  return new QueryClient({
    defaultOptions: {
      queries: {
        retry: shouldRetry,
        // Reuse data for 30s when moving between pages; writes invalidate what they change.
        staleTime: 30_000,
      },
      mutations: { retry: false },
    },
  });
}
