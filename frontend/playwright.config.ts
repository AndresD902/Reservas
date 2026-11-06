import { defineConfig, devices } from '@playwright/test'

const frontendPort = Number(process.env.E2E_FRONTEND_PORT ?? 5173)
const apiUrl = process.env.E2E_API_URL ?? 'http://localhost:8080'
const isCi = Boolean(process.env.CI)

export default defineConfig({
  testDir: './e2e',
  fullyParallel: false,
  forbidOnly: isCi,
  retries: isCi ? 2 : 0,
  workers: isCi ? 2 : undefined,
  reporter: [['list'], ['html', { open: 'never' }]],
  use: {
    baseURL: `http://localhost:${frontendPort}`,
    trace: 'on-first-retry',
    screenshot: 'only-on-failure',
  },
  projects: [
    {
      name: 'chromium',
      use: { ...devices['Desktop Chrome'] },
    },
  ],
  webServer: [
    {
      command: 'node scripts/start-backend.mjs',
      url: `${apiUrl}/recursos`,
      reuseExistingServer: !isCi,
      timeout: 240_000,
      stdout: 'pipe',
      stderr: 'pipe',
    },
    {
      command: `npm run dev -- --port ${frontendPort} --strictPort`,
      url: `http://localhost:${frontendPort}`,
      reuseExistingServer: !isCi,
      timeout: 120_000,
    },
  ],
})
