# Learn the application in six steps

## 1. Start with the user action

Open `frontend/src/main.jsx`. The `form` state holds the title, description, and completion flag. Each input updates that state with `setForm`. Submitting the form calls `save` and prevents the browser's normal page reload with `event.preventDefault()`.

Try changing the page heading. Refresh and confirm the change. Then find the three task counters and explain how they are calculated from the array.

## 2. Follow the HTTP call

`save` calls `taskApi` from `api.js`. A new task sends `POST /api/tasks` with a JSON body. Editing sends `PUT /api/tasks/{id}`. The browser uses a relative `/api` URL; Vite's proxy forwards it to the Java backend.

Open the browser's Network panel, add a task, and inspect its request body, status code, and response. Find the generated ID and `createdAt` timestamp. The backend chooses both values.

## 3. Find the Java endpoint

`@RestController` tells Spring to serialize return values as HTTP responses. `@RequestMapping("/api/tasks")` supplies a shared path. `@PostMapping` maps POST requests to `create`.

`@RequestBody` turns the incoming JSON into a `TaskRequest`. `@Valid` checks its annotations before the service runs. Try an empty title and inspect the 400 response. Frontend validation improves the user experience; backend validation protects the API even if a caller bypasses the form.

## 4. Read service and repository separately

The controller receives a `TaskService` through its constructor. Spring creates and supplies that dependency. The service similarly receives a repository. This is constructor-based dependency injection.

The service normalizes text, creates an ID, and coordinates database work. The repository contains SQL. Find `INSERT`, `SELECT`, `UPDATE`, and `DELETE`, then identify the `?` placeholders. Values are bound separately rather than concatenated into SQL.

`@Transactional` makes an operation commit as a unit or roll back on failure. In this app an update is followed by a read inside the same service transaction.

## 5. Observe persistence

Add two tasks, stop the Java process, restart it from the same backend directory, and refresh the browser. The H2 file keeps those tasks. Tests override the database URL to use a separate in-memory database.

Find the schema and compare each column to the `Task` record. A record provides immutable data accessors. For example, `task.title()` returns the title. The `TaskRequest` differs from `Task`: callers send editable fields; the backend returns the generated ID and creation time too.

## 6. Read a test as a user story

Open `TaskApiTest`. The main CRUD test creates a task, reads it, edits it, completes it, deletes it, and confirms that reading it now returns 404. Each assertion is a statement about observable API behavior.

Run the suite, change the title limit, update the corresponding test, and run it again. Explain why a test request does not change the tasks saved by your development server.

## Exercise: add a due date

1. Decide whether due date is optional and whether past dates are allowed.
2. Add a database column and a field to the response and request models.
3. Update the repository queries and row mapping.
4. Add the form input and display it in the task list.
5. Write tests for valid, absent, and invalid dates.
6. Plan a migration for existing data. `CREATE TABLE IF NOT EXISTS` does not update an already-created table.

## Questions you should be able to answer

- What is the difference between frontend state and database state?
- Why does creating return 201, updating 200, and deleting 204?
- Where does validation happen, and what does a missing task return?
- What happens when the frontend starts but Java is stopped?
- Why do we split controller, service, and repository?
- How does a parameterized query differ from string concatenation?
- What changes when several users edit the same task?
