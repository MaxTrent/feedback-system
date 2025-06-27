package com.balancee.backendtask.repository;

import com.balancee.backendtask.model.AdminResponse;
import com.balancee.backendtask.model.Feedback;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface AdminResponseRepository extends JpaRepository<AdminResponse, UUID> {
    List<AdminResponse> findByFeedback(Feedback feedback);
}