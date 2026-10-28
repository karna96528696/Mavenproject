# Task API

Base URL during local development: `http://127.0.0.1:8080/api/tasks`.

| Method | Path | Success |
| --- | --- | --- |
| GET | `/api/tasks` | 200 and all tasks, newest first |
| GET | `/api/tasks/{id}` | 200 and one task |
| POST | `/api/tasks` | 201, task JSON, and a Location header |
| PUT | `/api/tasks/{id}` | 200 and updated task JSON |
| DELETE | `/api/tasks/{id}` | 204 with an empty body |

## Create or update body

```json
{
  "title": "Build my first Java API",
  "description": "Read the controller and trace one request",
  "completed": false
}
```

All three fields are required. Use `""` for a description with no notes. Title must be nonblank and at most 100 characters; description is at most 1000 characters. Text is trimmed when saved. `completed` must be `true` or `false`. Updates replace all editable fields and preserve the ID and creation timestamp.

## Try it with curl — macOS/Linux

```sh
curl http://127.0.0.1:8080/api/tasks

curl -i -X POST http://127.0.0.1:8080/api/tasks \
  -H 'Content-Type: application/json' \
  -d '{"title":"Learn Java","description":"Practice REST","completed":false}'
```

Copy the response's real `id` into these requests in place of `TASK-ID`:

```sh
curl -X PUT http://127.0.0.1:8080/api/tasks/TASK-ID \
  -H 'Content-Type: application/json' \
  -d '{"title":"Learn Java","description":"Practice REST","completed":true}'

curl -i -X DELETE http://127.0.0.1:8080/api/tasks/TASK-ID
```

## Windows PowerShell

```powershell
$body = @{ title = 'Learn Java'; description = 'Practice REST'; completed = $false } | ConvertTo-Json
$task = Invoke-RestMethod -Method Post -Uri 'http://127.0.0.1:8080/api/tasks' -ContentType 'application/json' -Body $body
$task
Invoke-RestMethod -Uri 'http://127.0.0.1:8080/api/tasks'
Invoke-RestMethod -Method Delete -Uri "http://127.0.0.1:8080/api/tasks/$($task.id)"
```

## Errors

Invalid fields, malformed JSON, and invalid UUIDs return 400. Reading, editing, or deleting an unknown ID returns 404. Error responses use `application/problem+json`; validation responses include a readable `detail` field.

This local learning API has no authentication. Do not use it for shared accounts or sensitive information.
