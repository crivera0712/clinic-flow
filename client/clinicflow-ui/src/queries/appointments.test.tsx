import type { ReactNode } from "react";
import { act, renderHook, waitFor } from "@testing-library/react";
import { QueryClientProvider, type QueryClient } from "@tanstack/react-query";
import { http, HttpResponse } from "msw";
import { ApiError } from "../api/client";
import { apiUrl } from "../test/constants";
import { boardRowFixture } from "../test/fixtures";
import { server } from "../test/msw/server";
import { createTestQueryClient } from "../test/render";
import type { BoardRow } from "../types/appointment";
import { useAppointments, useUpdateAppointmentStatus } from "./appointments";
import { queryKeys } from "./keys";

const DATE = boardRowFixture.scheduledAt.slice(0, 10);
const key = queryKeys.appointments.byDate(DATE);

function wrapperFor(queryClient: QueryClient) {
  return function Wrapper({ children }: { children: ReactNode }) {
    return <QueryClientProvider client={queryClient}>{children}</QueryClientProvider>;
  };
}

// A response the test releases by hand, so it can look at the cache mid-request.
function deferred() {
  let release!: () => void;
  const promise = new Promise<void>((resolve) => (release = resolve));
  return { promise, release };
}

function statusInCache(queryClient: QueryClient) {
  return queryClient.getQueryData<BoardRow[]>(key)?.[0]?.status;
}

describe("useUpdateAppointmentStatus", () => {
  let serverRows: BoardRow[];

  beforeEach(() => {
    serverRows = [{ ...boardRowFixture, status: "SCHEDULED" }];
    server.use(http.get(apiUrl("/appointments/date"), () => HttpResponse.json(serverRows)));
  });

  function renderCheckIn(queryClient = createTestQueryClient()) {
    const view = renderHook(
      () => ({ day: useAppointments(DATE), checkIn: useUpdateAppointmentStatus() }),
      { wrapper: wrapperFor(queryClient) },
    );
    return { queryClient, ...view };
  }

  it("updates the row before the server answers and keeps it when the server agrees", async () => {
    const response = deferred();
    server.use(
      http.patch(apiUrl("/appointments/:id"), async () => {
        await response.promise;
        serverRows = [{ ...boardRowFixture, status: "WAITING" }];
        return HttpResponse.json(serverRows[0]);
      }),
    );
    const { queryClient, result } = renderCheckIn();
    await waitFor(() => expect(result.current.day.isSuccess).toBe(true));

    act(() => result.current.checkIn.mutate({ row: serverRows[0], status: "WAITING" }));

    // Optimistic: already WAITING while the request is still in flight.
    await waitFor(() => expect(statusInCache(queryClient)).toBe("WAITING"));
    expect(result.current.checkIn.isPending).toBe(true);

    response.release();
    await waitFor(() => expect(result.current.checkIn.isSuccess).toBe(true));
    await waitFor(() => expect(result.current.day.isFetching).toBe(false));
    expect(statusInCache(queryClient)).toBe("WAITING");
  });

  it("puts the row back when the server rejects the change", async () => {
    const response = deferred();
    server.use(
      http.patch(apiUrl("/appointments/:id"), async () => {
        await response.promise;
        return HttpResponse.json({ message: "Demo clinic is read-only" }, { status: 403 });
      }),
    );
    // Seed the cache with no component watching the day, so the post-error invalidation can't
    // refetch: only the rollback itself can restore the row.
    const queryClient = createTestQueryClient();
    queryClient.setQueryData(key, serverRows);
    const { result } = renderHook(() => useUpdateAppointmentStatus(), {
      wrapper: wrapperFor(queryClient),
    });

    act(() => result.current.mutate({ row: serverRows[0], status: "WAITING" }));
    await waitFor(() => expect(statusInCache(queryClient)).toBe("WAITING"));

    response.release();
    await waitFor(() => expect(result.current.isError).toBe(true));
    expect(statusInCache(queryClient)).toBe("SCHEDULED");
    expect(result.current.error).toBeInstanceOf(ApiError);
    expect((result.current.error as ApiError).status).toBe(403);
  });
});

describe("useAppointments", () => {
  it("keeps the previous date's rows as placeholder data while a new date loads", async () => {
    const nextDay = deferred();
    const nextRow: BoardRow = { ...boardRowFixture, id: 200, scheduledAt: "2026-06-21T09:00" };
    server.use(
      http.get(apiUrl("/appointments/date"), async ({ request }) => {
        if (new URL(request.url).searchParams.get("date") === "2026-06-21") {
          await nextDay.promise;
          return HttpResponse.json([nextRow]);
        }
        return HttpResponse.json([boardRowFixture]);
      }),
    );
    const { result, rerender } = renderHook(({ date }) => useAppointments(date), {
      initialProps: { date: DATE },
      wrapper: wrapperFor(createTestQueryClient()),
    });
    await waitFor(() => expect(result.current.isSuccess).toBe(true));

    rerender({ date: "2026-06-21" });

    // Still showing the previous day, flagged as placeholder so the page can dim it.
    expect(result.current.isPlaceholderData).toBe(true);
    expect(result.current.data).toEqual([boardRowFixture]);

    nextDay.release();
    await waitFor(() => expect(result.current.isPlaceholderData).toBe(false));
    expect(result.current.data).toEqual([nextRow]);
  });
});
