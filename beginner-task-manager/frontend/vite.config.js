import {defineConfig} from 'vite';

// Browser requests stay on the frontend origin; Vite forwards /api to Java.
export default defineConfig({
  server: {proxy: {'/api': 'http://127.0.0.1:8080'}}
});
