package com.balancee.backendtask.repository;

import com.balancee.backendtask.model.Attachment;
import com.balancee.backendtask.model.Feedback;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface AttachmentRepository extends JpaRepository<Attachment, UUID> {
    List<Attachment> findByFeedback(Feedback feedback);
}