import { http, HttpResponse } from "msw";
import { apiRequest, ApiError, configureApiClient, resetApiClient } from "./client";
import { server } from "../test/msw/server";
import { apiUrl } from "../test/constants";

// Await a promise expected to reject and return the (typed) ApiError it threw.
async function captureError(promise: Promise<unknown>): Promise<ApiError> {
  try {
    await promise;
    throw new Error("expected apiRequest to reject, but it resolved");
  } catch (error) {
    return error as ApiError;
  }
}

const authHandlers = (overrides: Partial<Parameters<typeof configureApiClient>[0]> = {}) => ({
  getAccessToken: () => null,
  refreshAccessToken: vi.fn(async () => null),
  onAuthFailure: vi.fn(),
  ...overrides,
});

afterEach(() => {
  resetApiClient();
});

describe("apiRequest — auth header", () => {
  it("attaches Authorization: Bearer when a token is configured", async () => {
    configureApiClient(authHandlers({ getAccessToken: () => "tok123" }));
    let seen: string | null = null;
    server.use(
      http.get(apiUrl("/ping"), ({ request }) => {
        seen = request.headers.get("authorization");
        return HttpResponse.json({ ok: true });
      }),
    );

    const res = await apiRequest("/ping");

    expect(seen).toBe("Bearer tok123");
    expect(res).toEqual({ ok: true });
  });

  it("omits Authorization when skipAuth is set", async () => {
    configureApiClient(authHandlers({ getAccessToken: () => "tok123" }));
    let seen: string | null = "unset";
    server.use(
      http.get(apiUrl("/ping"), ({ request }) => {
        seen = request.headers.get("authorization");
        return HttpResponse.json({ ok: true });
      }),
    );

    await apiRequest("/ping", { skipAuth: true });

    expect(seen).toBeNull();
  });
});

describe("apiRequest — request body", () => {
  it("serializes an object body to JSON and sets Content-Type", async () => {
    let contentType: string | null = null;
    let body: unknown = null;
    server.use(
      http.post(apiUrl("/echo"), async ({ request }) => {
        contentType = request.headers.get("content-type");
        body = await request.json();
        return HttpResponse.json({ ok: true });
      }),
    );

    await apiRequest("/echo", { method: "POST", body: { a: 1, b: "two" } });

    expect(contentType).toContain("application/json");
    expect(body).toEqual({ a: 1, b: "two" });
  });

  it("does not force JSON Content-Type for FormData bodies", async () => {
    let contentType: string | null = null;
    server.use(
      http.post(apiUrl("/upload"), ({ request }) => {
        contentType = request.headers.get("content-type");
        return HttpResponse.json({ ok: true });
      }),
    );
    const form = new FormData();
    form.append("file", "contents");

    await apiRequest("/upload", { method: "POST", body: form });

    expect(contentType).not.toContain("application/json");
    expect(contentType).toMatch(/multipart\/form-data/);
  });
});

describe("apiRequest — response parsing", () => {
  it("returns null for 204 No Content", async () => {
    server.use(http.delete(apiUrl("/thing/1"), () => new HttpResponse(null, { status: 204 })));

    const res = await apiRequest("/thing/1", { method: "DELETE" });

    expect(res).toBeNull();
  });

  it("parses JSON responses", async () => {
    server.use(http.get(apiUrl("/json"), () => HttpResponse.json({ value: 42 })));

    expect(await apiRequest("/json")).toEqual({ value: 42 });
  });

  it("returns text for non-JSON responses, and null when empty", async () => {
    server.use(
      http.get(apiUrl("/text"), () =>
        new HttpResponse("hello", { headers: { "content-type": "text/plain" } }),
      ),
    );
    expect(await apiRequest("/text")).toBe("hello");

    server.use(
      http.get(apiUrl("/empty"), () =>
        new HttpResponse("", { headers: { "content-type": "text/plain" } }),
      ),
    );
    expect(await apiRequest("/empty")).toBeNull();
  });
});

describe("apiRequest — error handling", () => {
  it("throws ApiError carrying status and data", async () => {
    server.use(
      http.get(apiUrl("/err"), () =>
        new HttpResponse("boom", { status: 400, headers: { "content-type": "text/plain" } }),
      ),
    );

    const err = await captureError(apiRequest("/err"));

    expect(err).toBeInstanceOf(ApiError);
    expect(err.status).toBe(400);
    expect(err.message).toBe("boom");
    expect(err.data).toBe("boom");
  });

  it("uses data.message as the error message", async () => {
    server.use(http.get(apiUrl("/err"), () => HttpResponse.json({ message: "bad input" }, { status: 422 })));

    const err = await captureError(apiRequest("/err"));

    expect(err.status).toBe(422);
    expect(err.message).toBe("bad input");
    expect(err.data).toEqual({ message: "bad input" });
  });

  it("uses the first entry of data.errors as the error message", async () => {
    server.use(
      http.get(apiUrl("/err"), () => HttpResponse.json({ errors: ["first", "second"] }, { status: 400 })),
    );

    const err = await captureError(apiRequest("/err"));

    expect(err.message).toBe("first");
  });

  it("falls back to a generic message when data has none", async () => {
    server.use(http.get(apiUrl("/err"), () => HttpResponse.json({}, { status: 500 })));

    const err = await captureError(apiRequest("/err"));

    expect(err.status).toBe(500);
    expect(err.message).toBeTruthy();
  });
});

describe("apiRequest — 401 refresh/retry", () => {
  it("refreshes the token and retries once on 401, carrying the new token", async () => {
    const refreshAccessToken = vi.fn(async () => "newtok");
    const onAuthFailure = vi.fn();
    configureApiClient(authHandlers({ getAccessToken: () => "oldtok", refreshAccessToken, onAuthFailure }));

    const seenAuth: (string | null)[] = [];
    server.use(
      http.get(
        apiUrl("/secure"),
        ({ request }) => {
          seenAuth.push(request.headers.get("authorization"));
          return new HttpResponse(null, { status: 401 });
        },
        { once: true },
      ),
      http.get(apiUrl("/secure"), ({ request }) => {
        seenAuth.push(request.headers.get("authorization"));
        return HttpResponse.json({ ok: true });
      }),
    );

    const res = await apiRequest("/secure");

    expect(res).toEqual({ ok: true });
    expect(refreshAccessToken).toHaveBeenCalledTimes(1);
    expect(seenAuth).toEqual(["Bearer oldtok", "Bearer newtok"]);
    expect(onAuthFailure).not.toHaveBeenCalled();
  });

  it("calls onAuthFailure and throws when refresh yields no token", async () => {
    const refreshAccessToken = vi.fn(async () => null);
    const onAuthFailure = vi.fn();
    configureApiClient(authHandlers({ getAccessToken: () => "oldtok", refreshAccessToken, onAuthFailure }));
    server.use(http.get(apiUrl("/secure"), () => HttpResponse.json({ message: "expired" }, { status: 401 })));

    const err = await captureError(apiRequest("/secure"));

    expect(err.status).toBe(401);
    expect(refreshAccessToken).toHaveBeenCalledTimes(1);
    expect(onAuthFailure).toHaveBeenCalledTimes(1);
  });

  it("calls onAuthFailure and throws when the retry is also 401", async () => {
    const refreshAccessToken = vi.fn(async () => "newtok");
    const onAuthFailure = vi.fn();
    configureApiClient(authHandlers({ getAccessToken: () => "oldtok", refreshAccessToken, onAuthFailure }));
    server.use(http.get(apiUrl("/secure"), () => HttpResponse.json({ message: "nope" }, { status: 401 })));

    const err = await captureError(apiRequest("/secure"));

    expect(err.status).toBe(401);
    expect(refreshAccessToken).toHaveBeenCalledTimes(1);
    expect(onAuthFailure).toHaveBeenCalledTimes(1);
  });

  it("does not call onAuthFailure when the retry fails with a non-401", async () => {
    const refreshAccessToken = vi.fn(async () => "newtok");
    const onAuthFailure = vi.fn();
    configureApiClient(authHandlers({ getAccessToken: () => "oldtok", refreshAccessToken, onAuthFailure }));
    server.use(
      http.get(apiUrl("/secure"), () => new HttpResponse(null, { status: 401 }), { once: true }),
      http.get(apiUrl("/secure"), () => HttpResponse.json({ message: "server boom" }, { status: 500 })),
    );

    const err = await captureError(apiRequest("/secure"));

    expect(err.status).toBe(500);
    expect(refreshAccessToken).toHaveBeenCalledTimes(1);
    expect(onAuthFailure).not.toHaveBeenCalled();
  });

  it("does not refresh on 401 when skipAuth is set", async () => {
    const refreshAccessToken = vi.fn(async () => "newtok");
    configureApiClient(authHandlers({ getAccessToken: () => "tok", refreshAccessToken }));
    server.use(http.get(apiUrl("/secure"), () => new HttpResponse(null, { status: 401 })));

    const err = await captureError(apiRequest("/secure", { skipAuth: true }));

    expect(err.status).toBe(401);
    expect(refreshAccessToken).not.toHaveBeenCalled();
  });

  it("does not retry on 401 when no auth handlers are configured", async () => {
    server.use(http.get(apiUrl("/secure"), () => new HttpResponse(null, { status: 401 })));

    const err = await captureError(apiRequest("/secure"));

    expect(err.status).toBe(401);
  });
});
