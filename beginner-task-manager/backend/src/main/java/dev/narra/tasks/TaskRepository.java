package dev.narra.tasks;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public class TaskRepository {
    private final JdbcTemplate jdbc;
    private final RowMapper<Task> mapper = (row, index) -> new Task(
            row.getObject("id", UUID.class), row.getString("title"),
            row.getString("description"), row.getBoolean("completed"),
            row.getObject("created_at", OffsetDateTime.class));

    public TaskRepository(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    public List<Task> findAll() {
        return jdbc.query("SELECT * FROM tasks ORDER BY created_at DESC, id", mapper);
    }

    public Optional<Task> findById(UUID id) {
        return jdbc.query("SELECT * FROM tasks WHERE id = ?", mapper, id).stream().findFirst();
    }

    public void insert(Task task) {
        // ? parameters keep user-entered text separate from SQL commands.
        jdbc.update("INSERT INTO tasks(id, title, description, completed, created_at) VALUES (?, ?, ?, ?, ?)",
                task.id(), task.title(), task.description(), task.completed(), task.createdAt());
    }

    public boolean update(UUID id, TaskRequest request) {
        return jdbc.update("UPDATE tasks SET title = ?, description = ?, completed = ? WHERE id = ?",
                request.title(), request.description(), request.completed(), id) == 1;
    }

    public boolean delete(UUID id) {
        return jdbc.update("DELETE FROM tasks WHERE id = ?", id) == 1;
    }
}
