package com.balancee.backendtask.repository;

import com.balancee.backendtask.model.Feedback;
import com.balancee.backendtask.model.Category;
import com.balancee.backendtask.model.Status;
import com.balancee.backendtask.model.Priority;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public interface FeedbackRepository extends JpaRepository<Feedback, UUID> {
    List<Feedback> findByRating(int rating);
    List<Feedback> findByCreatedAtBetween(LocalDateTime start, LocalDateTime end);
    List<Feedback> findByRatingAndCreatedAtBetween(int rating, LocalDateTime start, LocalDateTime end);
    List<Feedback> findByCategory(Category category);
    List<Feedback> findByCategoryAndRating(Category category, int rating);
    List<Feedback> findByStatus(Status status);
    List<Feedback> findByStatusAndCategory(Status status, Category category);
    List<Feedback> findByPriority(Priority priority);
    List<Feedback> findByPriorityAndCategory(Priority priority, Category category);
}