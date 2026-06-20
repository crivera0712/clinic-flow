import { http, HttpResponse } from "msw";
import { createTherapist, listTherapists, removeTherapist } from "./therapistService";
import { server } from "../test/msw/server";
import { apiUrl } from "../test/constants";
import { therapistFixture } from "../test/fixtures";

describe("therapistService", () => {
  it("listTherapists GETs /therapists and returns the array", async () => {
    let captured: Request | undefined;
    server.use(
      http.get(apiUrl("/therapists"), ({ request }) => {
        captured = request;
        return HttpResponse.json([therapistFixture]);
      }),
    );

    const result = await listTherapists();

    expect(new URL(captured!.url).pathname).toBe("/api/therapists");
    expect(result).toEqual([therapistFixture]);
  });

  it("createTherapist POSTs the name", async () => {
    let body: unknown;
    server.use(
      http.post(apiUrl("/therapists"), async ({ request }) => {
        body = await request.json();
        return HttpResponse.json(therapistFixture);
      }),
    );

    const result = await createTherapist({ name: "Dr. Smith" });

    expect(body).toEqual({ name: "Dr. Smith" });
    expect(result).toEqual(therapistFixture);
  });

  it("removeTherapist DELETEs /therapists/:id", async () => {
    let captured: Request | undefined;
    server.use(
      http.delete(apiUrl("/therapists/7"), ({ request }) => {
        captured = request;
        return new HttpResponse(null, { status: 204 });
      }),
    );

    await removeTherapist(7);

    expect(captured?.method).toBe("DELETE");
    expect(new URL(captured!.url).pathname).toBe("/api/therapists/7");
  });
});
