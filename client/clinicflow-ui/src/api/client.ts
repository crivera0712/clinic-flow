type AuthHandlers = {
  getAccessToken: () => string | null;
  refreshAccessToken: () => Promise<string | null>;
  onAuthFailure: () => void;
};

type RequestOptions = Omit<RequestInit, "body"> & {
  body?: BodyInit | object | null;
  skipAuth?: boolean;
  retryOn401?: boolean;
  statusMessages?: Partial<Record<number, string>>;
};

export class ApiError extends Error {
  status: number;
  data: unknown;

  constructor(message: string, status: number, data: unknown) {
    super(message);
    this.name = "ApiError";
    this.status = status;
    this.data = data;
  }
}

const apiBaseUrl = (import.meta.env.VITE_API_BASE_URL || "/api").replace(/\/$/, "");

let authHandlers: AuthHandlers | null = null;

export function configureApiClient(handlers: AuthHandlers) {
  authHandlers = handlers;
}

export function resetApiClient() {
  authHandlers = null;
}

function buildUrl(path: string) {
  const normalizedPath = path.startsWith("/") ? path : `/${path}`;
  return `${apiBaseUrl}${normalizedPath}`;
}

function isPlainObject(value: unknown): value is Record<string, unknown> {
  return typeof value === "object" && value !== null && !Array.isArray(value);
}

async function parseResponseBody(response: Response) {
  if (response.status === 204) {
    return null;
  }

  const contentType = response.headers.get("content-type") || "";
  if (contentType.includes("application/json")) {
    return response.json();
  }

  const text = await response.text();
  return text || null;
}

function getErrorMessage(
  status: number,
  fallback: string,
  data: unknown,
  statusMessages?: Partial<Record<number, string>>,
) {
  if (typeof data === "string" && data.trim()) {
    return data;
  }

  if (isPlainObject(data) && typeof data.message === "string") {
    return data.message;
  }

  if (isPlainObject(data) && Array.isArray(data.errors) && data.errors.length > 0) {
    return String(data.errors[0]);
  }

  if (statusMessages?.[status]) {
    return statusMessages[status];
  }

  return fallback || `Request failed with status ${status}`;
}

function buildHeaders(
  headers: HeadersInit | undefined,
  body: RequestOptions["body"],
  token: string | null,
  skipAuth: boolean,
) {
  const nextHeaders = new Headers(headers);

  if (body !== null && body !== undefined && !(body instanceof FormData) && !nextHeaders.has("Content-Type")) {
    nextHeaders.set("Content-Type", "application/json");
  }

  if (!skipAuth && token) {
    nextHeaders.set("Authorization", `Bearer ${token}`);
  }

  return nextHeaders;
}

function normalizeBody(body: RequestOptions["body"]) {
  if (body === null || body === undefined || body instanceof FormData || typeof body === "string") {
    return body;
  }

  return JSON.stringify(body);
}

export async function apiRequest<T>(path: string, options: RequestOptions = {}): Promise<T> {
  const {
    body,
    headers,
    skipAuth = false,
    retryOn401 = !skipAuth,
    statusMessages,
    credentials = "include",
    ...init
  } = options;

  const accessToken = skipAuth ? null : authHandlers?.getAccessToken() ?? null;
  const response = await fetch(buildUrl(path), {
    ...init,
    body: normalizeBody(body),
    credentials,
    headers: buildHeaders(headers, body, accessToken, skipAuth),
  });

  if (response.status === 401 && retryOn401 && authHandlers && !skipAuth) {
    const refreshedToken = await authHandlers.refreshAccessToken();

    if (refreshedToken) {
      const retryResponse = await fetch(buildUrl(path), {
        ...init,
        body: normalizeBody(body),
        credentials,
        headers: buildHeaders(headers, body, refreshedToken, skipAuth),
      });

      if (retryResponse.ok) {
        return (await parseResponseBody(retryResponse)) as T;
      }

      const retryData = await parseResponseBody(retryResponse);
      if (retryResponse.status === 401) {
        authHandlers.onAuthFailure();
      }

      throw new ApiError(
        getErrorMessage(retryResponse.status, retryResponse.statusText, retryData, statusMessages),
        retryResponse.status,
        retryData,
      );
    }

    authHandlers.onAuthFailure();
  }

  if (!response.ok) {
    const data = await parseResponseBody(response);
    throw new ApiError(
      getErrorMessage(response.status, response.statusText, data, statusMessages),
      response.status,
      data,
    );
  }

  return (await parseResponseBody(response)) as T;
}
