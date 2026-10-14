import { StrictMode } from "react";
import { render, screen } from "@testing-library/react";
import { http, HttpResponse } from "msw";
import { AuthProvider, useAuth } from "./AuthContext";
import { server } from "../test/msw/server";
import { apiUrl } from "../test/constants";
import { currentUserFixture, jwtFixture } from "../test/fixtures";

function AuthStatusProbe() {
  const { status, currentUser } = useAuth();
  return (
    <>
      <span data-testid="status">{status}</span>
      <span data-testid="username">{currentUser?.username ?? ""}</span>
    </>
  );
}

beforeEach(() => sessionStorage.clear());

describe("AuthProvider bootstrap", () => {
  it("authenticates via refresh under StrictMode when no access token is stored", async () => {
    let refreshCalls = 0;
    server.use(
      http.post(apiUrl("/auth/refresh"), () => {
        refreshCalls += 1;
        return HttpResponse.json(jwtFixture);
      }),
      http.get(apiUrl("/auth/me"), () => HttpResponse.json(currentUserFixture)),
    );

    render(
      <StrictMode>
        <AuthProvider>
          <AuthStatusProbe />
        </AuthProvider>
      </StrictMode>,
    );

    expect(await screen.findByText("authenticated")).toBeInTheDocument();
    expect(screen.getByTestId("username")).toHaveTextContent(currentUserFixture.username);
    // Both StrictMode effect runs share one refresh request (refresh tokens may rotate).
    expect(refreshCalls).toBe(1);
    expect(sessionStorage.getItem("clinicflow.accessToken")).toBe(jwtFixture.token);
  });
});
