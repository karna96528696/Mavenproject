# Troubleshooting

| Symptom | Check |
| --- | --- |
| `mvn` or `java` is not recognized | Install a JDK and Maven; reopen the terminal; check PATH and `mvn -version` |
| Unsupported class release | Ensure Maven is running with JDK 17 or newer |
| Frontend cannot load tasks | Start Java first; visit `http://127.0.0.1:8080/api/tasks` and inspect the backend terminal |
| Port 8080 already in use | Stop the other local service; if changing the API port, update the Vite proxy target too |
| Vite starts on another port | Open the exact URL Vite prints |
| `npm ci` fails due to Node version | Install Node 22.12+ or Node 24, then reopen the terminal |
| Maven/npm download fails | Check internet access, proxy settings, and access to the package registries |
| Database is locked | Run one backend instance per `backend/data` directory |
| A schema change is missing | Existing tables are preserved; use a database migration rather than expecting `CREATE TABLE IF NOT EXISTS` to alter columns |
| The static frontend loads but API calls fail | `npm run build` produces frontend files only; host `/api` behind a reverse proxy to Java or use Vite development mode |

For a local reset, stop the backend and back up `backend/data` before removing that generated directory. Restarting creates an empty database. This deliberately removes saved tasks, so it is not part of the normal setup steps.
