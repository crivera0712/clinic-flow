import { http, HttpResponse } from "msw";
import { createPatient, listPatients, searchPatients } from "./patientService";
import { server } from "../test/msw/server";
import { apiUrl } from "../test/constants";
import { pageOf, patientFixture } from "../test/fixtures";

describe("patientService", () => {
  it("searchPatients hits /patients/search and url-encodes a trimmed query", async () => {
    let captured: Request | undefined;
    server.use(
      http.get(apiUrl("/patients/search"), ({ request }) => {
        captured = request;
        return HttpResponse.json([patientFixture]);
      }),
    );

    const result = await searchPatients("  jane doe  ");

    const url = new URL(captured!.url);
    expect(url.pathname).toBe("/api/patients/search");
    expect(url.searchParams.get("q")).toBe("jane doe");
    expect(result).toEqual([patientFixture]);
  });

  it("listPatients pages and unwraps to rows/rowCount", async () => {
    let captured: Request | undefined;
    server.use(
      http.get(apiUrl("/patients"), ({ request }) => {
        captured = request;
        return HttpResponse.json(pageOf([patientFixture], 5));
      }),
    );

    const result = await listPatients({ page: 2, size: 10 });

    const url = new URL(captured!.url);
    expect(url.searchParams.get("page")).toBe("2");
    expect(url.searchParams.get("size")).toBe("10");
    expect(result.rows).toEqual([patientFixture]);
    expect(result.rowCount).toBe(5);
  });

  it("createPatient POSTs first/last name", async () => {
    let body: unknown;
    server.use(
      http.post(apiUrl("/patients"), async ({ request }) => {
        body = await request.json();
        return HttpResponse.json(patientFixture);
      }),
    );

    const result = await createPatient({ firstName: "Jane", lastName: "Doe" });

    expect(body).toEqual({ firstName: "Jane", lastName: "Doe" });
    expect(result).toEqual(patientFixture);
  });
});
