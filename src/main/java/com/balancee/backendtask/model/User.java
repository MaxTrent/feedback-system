package com.balancee.backendtask.model;

import java.time.LocalDateTime;
import java.util.UUID;

import jakarta.persistence.Entity;
import jakarta.persistence.Enumerated;
import jakarta.persistence.EnumType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Entity
@Data
@Table(name = "users")
public class User {
    @Id
    private UUID id;

    @NotBlank(message = "username is required")
    private String username;

    @Email(message = "valid email is required")
    @NotBlank(message = "email is required")
    private String email;

    @NotBlank(message = "password is required")
    private String password;

    @Enumerated(EnumType.STRING)
    private Role role;

    private LocalDateTime createdAt;

    public User() {
        this.id = UUID.randomUUID();
        this.createdAt = LocalDateTime.now();
        this.role = Role.USER;
    }

    public enum Role {
        USER, ADMIN
    }
}