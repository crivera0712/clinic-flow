import { ApiError } from "../api/client";
import { createQueryClient } from "./queryClient";

describe("createQueryClient retry policy", () => {
  const { queries, mutations } = createQueryClient().getDefaultOptions();
  const retry = queries?.retry as (failureCount: number, error: Error) => boolean;

  it("retries a network or server error once", () => {
    expect(retry(0, new TypeError("Failed to fetch"))).toBe(true);
    expect(retry(0, new ApiError("Server error", 500, null))).toBe(true);
    expect(retry(1, new ApiError("Server error", 500, null))).toBe(false);
  });

  it("never retries a 4xx answer", () => {
    expect(retry(0, new ApiError("Demo clinic is read-only", 403, null))).toBe(false);
    expect(retry(0, new ApiError("Not found", 404, null))).toBe(false);
  });

  it("never retries writes", () => {
    expect(mutations?.retry).toBe(false);
  });
});
