package com.balancee.backendtask.model;

import java.time.LocalDateTime;
import java.util.UUID;

import jakarta.persistence.Entity;
import jakarta.persistence.Enumerated;
import jakarta.persistence.EnumType;
import jakarta.persistence.Id;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Entity
@Data
public class Feedback {

    @Id
    private UUID id;

    private String userId;

    @NotBlank(message = "message is required")
    private String message;

    @Min(value = 1, message = "rating must be at least 1")
    @Max(value = 5, message = "rating must be at most 5")
    private int rating;

    @NotNull(message = "category is required")
    @Enumerated(EnumType.STRING)
    private Category category;

    @Enumerated(EnumType.STRING)
    private Status status;

    @Enumerated(EnumType.STRING)
    private Priority priority;

    private LocalDateTime createdAt;

    public Feedback() {
        this.id = UUID.randomUUID();
        this.createdAt = LocalDateTime.now();
        this.status = Status.NEW;
        this.priority = Priority.MEDIUM;
    }
}