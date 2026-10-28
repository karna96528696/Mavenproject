package dev.narra.tasks;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

// Request validation is enforced by @Valid on the controller methods.
public record TaskRequest(
        @NotBlank(message = "Title is required")
        @Size(max = 100, message = "Title must be 100 characters or fewer") String title,
        @NotNull(message = "Description must be a string; use an empty string if omitted")
        @Size(max = 1000, message = "Description must be 1000 characters or fewer") String description,
        @NotNull(message = "Completed must be true or false") Boolean completed) {}
