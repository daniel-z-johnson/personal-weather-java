import tailwindcss from '@tailwindcss/vite'
import react from '@vitejs/plugin-react'
import { defineConfig } from 'vite'

// https://vite.dev/config/
export default defineConfig({
  plugins: [react(), tailwindcss()],
  server: {
    // The backend (cd ../backend && ./gradlew bootRun) serves the API; proxying keeps it same-origin, so no CORS setup.
    proxy: {
      '/api': 'http://localhost:8080',
    },
  },
})
