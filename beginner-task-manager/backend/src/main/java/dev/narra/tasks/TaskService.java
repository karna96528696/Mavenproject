package dev.narra.tasks;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.List;
import java.util.UUID;

@Service
public class TaskService {
    private final TaskRepository repository;

    public TaskService(TaskRepository repository) {
        this.repository = repository;
    }

    public List<Task> findAll() {
        return repository.findAll();
    }

    public Task findById(UUID id) {
        return repository.findById(id).orElseThrow(TaskService::notFound);
    }

    @Transactional
    public Task create(TaskRequest request) {
        TaskRequest clean = normalize(request);
        Task task = new Task(UUID.randomUUID(), clean.title(), clean.description(),
                clean.completed(), OffsetDateTime.now(ZoneOffset.UTC));
        repository.insert(task);
        return findById(task.id());
    }

    @Transactional
    public Task update(UUID id, TaskRequest request) {
        if (!repository.update(id, normalize(request))) throw notFound();
        return findById(id);
    }

    @Transactional
    public void delete(UUID id) {
        if (!repository.delete(id)) throw notFound();
    }

    private TaskRequest normalize(TaskRequest request) {
        return new TaskRequest(request.title().strip(), request.description().strip(), request.completed());
    }

    private static ResponseStatusException notFound() {
        return new ResponseStatusException(HttpStatus.NOT_FOUND, "Task not found");
    }
}
