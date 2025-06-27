package com.balancee.backendtask.model;

import java.time.LocalDateTime;
import java.util.UUID;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import lombok.Data;

@Entity
@Data
public class Attachment {
    @Id
    private UUID id;

    @ManyToOne
    @JoinColumn(name = "feedback_id")
    private Feedback feedback;

    private String fileName;
    private String contentType;
    private Long fileSize;
    private String filePath;
    private LocalDateTime uploadedAt;

    public Attachment() {
        this.id = UUID.randomUUID();
        this.uploadedAt = LocalDateTime.now();
    }
}