import { http, HttpResponse } from "msw";
import {
  createAppointment,
  listAppointmentsByDate,
  removeAppointment,
  updateAppointment,
} from "./appointmentService";
import { server } from "../test/msw/server";
import { apiUrl } from "../test/constants";
import { boardRowFixture } from "../test/fixtures";

describe("appointmentService", () => {
  it("listAppointmentsByDate passes the date query and returns board rows", async () => {
    let captured: Request | undefined;
    server.use(
      http.get(apiUrl("/appointments/date"), ({ request }) => {
        captured = request;
        return HttpResponse.json([boardRowFixture]);
      }),
    );

    const result = await listAppointmentsByDate("2026-06-20");

    const url = new URL(captured!.url);
    expect(url.pathname).toBe("/api/appointments/date");
    expect(url.searchParams.get("date")).toBe("2026-06-20");
    expect(result).toEqual([boardRowFixture]);
  });

  it("createAppointment POSTs an existing patient id", async () => {
    let body: unknown;
    server.use(
      http.post(apiUrl("/appointments"), async ({ request }) => {
        body = await request.json();
        return HttpResponse.json(boardRowFixture, { status: 201 });
      }),
    );

    await createAppointment({ patientId: 1, therapistId: 7, scheduledAt: "2026-06-20T14:30", type: "EVALUATION" });

    expect(body).toEqual({ patientId: 1, therapistId: 7, scheduledAt: "2026-06-20T14:30", type: "EVALUATION" });
  });

  it("createAppointment POSTs an inline new patient", async () => {
    let body: unknown;
    server.use(
      http.post(apiUrl("/appointments"), async ({ request }) => {
        body = await request.json();
        return HttpResponse.json(boardRowFixture, { status: 201 });
      }),
    );

    await createAppointment({
      patient: { firstName: "New", lastName: "Patient" },
      therapistId: 7,
      scheduledAt: "2026-06-20T15:00",
      type: "FOLLOW_UP",
    });

    expect(body).toEqual({
      patient: { firstName: "New", lastName: "Patient" },
      therapistId: 7,
      scheduledAt: "2026-06-20T15:00",
      type: "FOLLOW_UP",
    });
  });

  it("updateAppointment PATCHes a status transition", async () => {
    let captured: Request | undefined;
    let body: unknown;
    server.use(
      http.patch(apiUrl("/appointments/100"), async ({ request }) => {
        captured = request;
        body = await request.json();
        return HttpResponse.json({ ...boardRowFixture, status: "WAITING" });
      }),
    );

    const result = await updateAppointment(100, { status: "WAITING" });

    expect(new URL(captured!.url).pathname).toBe("/api/appointments/100");
    expect(body).toEqual({ status: "WAITING" });
    expect(result.status).toBe("WAITING");
  });

  it("removeAppointment DELETEs /appointments/:id", async () => {
    let captured: Request | undefined;
    server.use(
      http.delete(apiUrl("/appointments/100"), ({ request }) => {
        captured = request;
        return new HttpResponse(null, { status: 204 });
      }),
    );

    await removeAppointment(100);

    expect(captured?.method).toBe("DELETE");
    expect(new URL(captured!.url).pathname).toBe("/api/appointments/100");
  });
});
