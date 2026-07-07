/// <reference types="vitest/config" />
import { defineConfig, loadEnv } from "vite";
import react from "@vitejs/plugin-react";

export default defineConfig(({ mode }) => {
  const env = loadEnv(mode, process.cwd(), "");

  return {
    plugins: [react()],
    server: {
      host: env.VITE_DEV_HOST || "127.0.0.1",
      proxy: {
        "/api": {
          target: env.VITE_API_PROXY_TARGET || "http://localhost:8080",
          changeOrigin: true,
        },
      },
    },
    test: {
      globals: true,
      environment: "jsdom",
      setupFiles: "./src/test/setup.ts",
      css: true,
      // Absolute base so Node fetch + MSW can intercept (relative URLs are
      // rejected by Node fetch). Keep in sync with API_BASE_URL in src/test/constants.ts.
      env: { VITE_API_BASE_URL: "http://localhost/api" },
    },
  };
});
