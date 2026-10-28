# Taskboard — Beginner Java Full Stack Application

I built Taskboard to practice a complete request flow: a React form sends JSON to a Java API, and the API validates and saves the task in a database.

**Designed and built by Naga Sai Narra.**

## What the app does

- Add a task with a title and optional notes.
- Edit a task, mark it complete, or reopen it.
- Filter all, active, and completed tasks.
- Confirm before deleting a task.
- Show validation and connection errors.
- Keep tasks after restarting the Java backend using a local H2 database file.

## Technology

| Layer | Technology | What you learn |
| --- | --- | --- |
| Frontend | React, JavaScript, CSS, Vite | Components, state, forms, effects, fetch |
| Backend | Java 17, Spring Boot 4.1.2 | REST controllers, dependency injection, validation |
| Data access | Spring JDBC / JdbcTemplate | SQL, repositories, parameterized queries |
| Database | H2, stored on disk | Tables, inserts, queries, updates, deletes |
| Tests | JUnit and Spring Boot | Testing real HTTP requests against the application |

No database server installation, Docker, Kafka, cloud account, or paid service is needed. The application runs locally; there is no login or multi-user authorization in this first version.

## Requirements

Install a **JDK 17 or newer**, **Maven 3.9+**, and **Node.js 22.12+** (Node 24 is suitable). Confirm they are available in your terminal:

```sh
java -version
mvn -version
node -v
npm -v
```

## Download the branch

```sh
git clone --branch beginner/task-manager https://github.com/karna96528696/Mavenproject.git
cd Mavenproject/beginner-task-manager
```

This app lives in its own folder on the `beginner/task-manager` branch. The Java files at the original repository root belong to the earlier project. Run the commands below from this app's folders.

## Start the backend — terminal 1

```sh
cd backend
mvn spring-boot:run
```

The Java API listens at `http://127.0.0.1:8080`. Open `http://127.0.0.1:8080/api/tasks` to see a JSON array; a fresh database returns `[]`. Keep this terminal running.

## Start the frontend — terminal 2

From `Mavenproject/beginner-task-manager`:

```sh
cd frontend
npm ci
npm run dev
```

Open the URL printed by Vite, normally `http://127.0.0.1:5173`. Add a task, edit it, mark it complete, change filters, and delete it. Refresh the page to check that the Java backend returns the saved data.

The first run downloads Maven and npm dependencies, so it needs internet access. Later runs can use the local dependency caches.

## Tests and build

In `backend`:

```sh
mvn verify
```

In `frontend`:

```sh
npm run build
```

Backend tests use an isolated in-memory database, leaving your local tasks alone. They cover the complete create/read/update/delete flow, input limits, missing tasks, malformed requests, and SQL punctuation stored as text.

The frontend build creates `frontend/dist`. This is a compilation check; it is not an automated browser test. The Vite development proxy is for local development. Serving `dist` separately requires a server that forwards `/api` requests to Java.

## Project structure

| File or folder | Responsibility |
| --- | --- |
| `frontend/src/main.jsx` | Task form, task list, filters, and UI state |
| `frontend/src/api.js` | HTTP calls and API error handling |
| `frontend/vite.config.js` | Forwards browser `/api` requests to Java during development |
| `backend/.../TaskController.java` | Maps HTTP requests to Java methods |
| `backend/.../TaskRequest.java` | Defines and validates incoming JSON |
| `backend/.../TaskService.java` | Coordinates task operations and transaction boundaries |
| `backend/.../TaskRepository.java` | Executes parameterized SQL |
| `backend/.../Task.java` | Represents the task returned as JSON |
| `backend/.../ApiErrors.java` | Makes validation errors readable |
| `backend/src/main/resources/schema.sql` | Creates the tasks table |
| `backend/src/test/.../TaskApiTest.java` | Exercises the running API |

See [the learning walkthrough](docs/LEARN.md), [API examples](docs/API.md), and [troubleshooting](docs/TROUBLESHOOTING.md).

## Local data and scope

Tasks are stored in `backend/data/tasks.mv.db` when you start Maven from `backend`. That generated directory is ignored by Git. The backend binds to the loopback address. This learning version has no accounts, permissions, pagination, or concurrent-edit conflict detection; the last successful update wins.

## Reference material

- [Spring: Accessing Relational Data using JDBC](https://spring.io/guides/gs/relational-data-access/)
- [Spring Boot system requirements](https://docs.spring.io/spring-boot/system-requirements.html)
- [React documentation](https://react.dev/learn)

## Next small improvements

Add a due date, write its validation test, then display overdue tasks. After that, experiment with PostgreSQL, Spring Data JPA, frontend tests, and login as separate learning steps.
