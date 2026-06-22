import { http, HttpResponse } from "msw";
import { getCurrentUser, login, logout, refresh, register } from "./auth";
import { configureApiClient, resetApiClient } from "./client";
import { server } from "../test/msw/server";
import { apiUrl } from "../test/constants";
import { currentUserFixture, jwtFixture } from "../test/fixtures";

beforeEach(() => {
  configureApiClient({
    getAccessToken: () => "tok",
    refreshAccessToken: vi.fn(async () => null),
    onAuthFailure: vi.fn(),
  });
});

afterEach(() => resetApiClient());

describe("auth api", () => {
  it("login POSTs credentials to /auth/login without an auth header", async () => {
    let captured: Request | undefined;
    let body: unknown;
    server.use(
      http.post(apiUrl("/auth/login"), async ({ request }) => {
        captured = request;
        body = await request.json();
        return HttpResponse.json(jwtFixture);
      }),
    );

    const res = await login({ username: "demo_admin", password: "secret" });

    expect(captured?.method).toBe("POST");
    expect(captured?.headers.get("authorization")).toBeNull();
    expect(body).toEqual({ username: "demo_admin", password: "secret" });
    expect(res).toEqual(jwtFixture);
  });

  it("register POSTs to the clinic-scoped path without an auth header", async () => {
    let captured: Request | undefined;
    let body: unknown;
    server.use(
      http.post(apiUrl("/auth/demo/register"), async ({ request }) => {
        captured = request;
        body = await request.json();
        return new HttpResponse(null, { status: 204 });
      }),
    );

    await register("demo", { username: "newuser", passwordHash: "hash" });

    expect(new URL(captured!.url).pathname).toBe("/api/auth/demo/register");
    expect(captured?.headers.get("authorization")).toBeNull();
    expect(body).toEqual({ username: "newuser", passwordHash: "hash" });
  });

  it("refresh POSTs to /auth/refresh without an auth header", async () => {
    let captured: Request | undefined;
    server.use(
      http.post(apiUrl("/auth/refresh"), ({ request }) => {
        captured = request;
        return HttpResponse.json(jwtFixture);
      }),
    );

    const res = await refresh();

    expect(captured?.headers.get("authorization")).toBeNull();
    expect(res).toEqual(jwtFixture);
  });

  it("logout POSTs to /auth/logout with the auth header", async () => {
    let captured: Request | undefined;
    server.use(
      http.post(apiUrl("/auth/logout"), ({ request }) => {
        captured = request;
        return new HttpResponse(null, { status: 204 });
      }),
    );

    await logout();

    expect(captured?.method).toBe("POST");
    expect(captured?.headers.get("authorization")).toBe("Bearer tok");
  });

  it("getCurrentUser GETs /auth/me with the auth header", async () => {
    let captured: Request | undefined;
    server.use(
      http.get(apiUrl("/auth/me"), ({ request }) => {
        captured = request;
        return HttpResponse.json(currentUserFixture);
      }),
    );

    const res = await getCurrentUser();

    expect(captured?.headers.get("authorization")).toBe("Bearer tok");
    expect(res).toEqual(currentUserFixture);
  });
});
