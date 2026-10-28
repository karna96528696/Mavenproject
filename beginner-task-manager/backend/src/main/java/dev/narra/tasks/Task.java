package dev.narra.tasks;

import java.time.OffsetDateTime;
import java.util.UUID;

// A record represents the task returned as JSON by the API.
public record Task(UUID id, String title, String description,
                   boolean completed, OffsetDateTime createdAt) {}
