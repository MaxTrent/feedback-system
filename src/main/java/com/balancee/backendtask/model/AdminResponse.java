package com.balancee.backendtask.model;

import java.time.LocalDateTime;
import java.util.UUID;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Entity
@Data
public class AdminResponse {
    @Id
    private UUID id;

    @ManyToOne
    @JoinColumn(name = "feedback_id")
    private Feedback feedback;

    @NotBlank(message = "response is required")
    private String response;

    @NotBlank(message = "adminId is required")
    private String adminId;

    private LocalDateTime createdAt;

    public AdminResponse() {
        this.id = UUID.randomUUID();
        this.createdAt = LocalDateTime.now();
    }
}