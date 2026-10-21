// Captures the landing-page screenshots (public/landing/*.jpg) from a running app, in light and
// dark mode, using your installed Google Chrome (playwright-core downloads no browsers).
//
// Needs the dev server (npm run dev) and a backend with a clinic that has a realistic morning of
// visits today. Name that clinic "Raccoon City Physical Therapy" so the board matches the demo
// clinic. Sign-in details come from the environment:
//
//   SCREENSHOT_ADMIN_USERNAME / SCREENSHOT_ADMIN_PASSWORD      front desk account (schedule shots)
//   SCREENSHOT_DISPLAY_USERNAME / SCREENSHOT_DISPLAY_PASSWORD  display account (board shots)
//   SCREENSHOT_BASE_URL                                        default http://localhost:5173
//
// Run: npm run screenshots
import { mkdir } from "node:fs/promises";
import { fileURLToPath } from "node:url";
import { chromium } from "playwright-core";

const baseUrl = process.env.SCREENSHOT_BASE_URL ?? "http://localhost:5173";
const outDir = fileURLToPath(new URL("../public/landing/", import.meta.url));
// The page clock reads 10:15 AM today whenever this runs, so the board's clock matches the visits.
const shotTime = new Date();
shotTime.setHours(10, 15, 0, 0);

function account(prefix) {
  const username = process.env[`${prefix}_USERNAME`];
  const password = process.env[`${prefix}_PASSWORD`];
  if (!username || !password) {
    console.error(`Set ${prefix}_USERNAME and ${prefix}_PASSWORD (see the top of this script).`);
    process.exit(1);
  }
  return { username, password };
}

const shots = [
  {
    name: "schedule",
    account: account("SCREENSHOT_ADMIN"),
    // Loaded once the day's visits have replaced the skeleton rows.
    ready: async (page) => {
      await page.getByRole("region", { name: "Day schedule" }).waitFor();
      await page.locator('section[aria-label="Day schedule"] section').first().waitFor();
    },
  },
  {
    name: "board",
    account: account("SCREENSHOT_DISPLAY"),
    ready: async (page) => {
      await page.getByRole("heading", { name: "Currently waiting" }).waitFor();
      await page.getByText(/^Updated /).waitFor();
    },
  },
];

await mkdir(outDir, { recursive: true });
const browser = await chromium.launch({ channel: "chrome" });

try {
  for (const mode of ["light", "dark"]) {
    for (const shot of shots) {
      // Retina-sharp 1440x900 viewport; the saved image is 2880x1800.
      const context = await browser.newContext({
        viewport: { width: 1440, height: 900 },
        deviceScaleFactor: 2,
        colorScheme: mode,
      });
      await context.addInitScript((m) => localStorage.setItem("mui-mode", m), mode);
      await context.clock.setFixedTime(shotTime);
      const page = await context.newPage();

      await page.goto(`${baseUrl}/login`);
      await page.getByLabel("Username").fill(shot.account.username);
      await page.getByLabel("Password").fill(shot.account.password);
      await page.getByRole("button", { name: "Sign in" }).click();
      await shot.ready(page);
      // Let fonts, transitions and the last refresh settle, and keep the pointer off the UI.
      await page.mouse.move(0, 0);
      await page.waitForLoadState("networkidle");
      await page.waitForTimeout(800);

      const path = `${outDir}${shot.name}-${mode}.jpg`;
      await page.screenshot({ path, type: "jpeg", quality: 85 });
      console.log(`Saved ${path}`);
      await context.close();
    }
  }
} finally {
  await browser.close();
}
