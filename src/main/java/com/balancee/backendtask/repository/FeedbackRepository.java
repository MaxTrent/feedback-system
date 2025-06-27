package com.balancee.backendtask.repository;

import com.balancee.backendtask.model.Feedback;
import com.balancee.backendtask.model.Category;
import com.balancee.backendtask.model.Status;
import com.balancee.backendtask.model.Priority;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
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
    
    // Paginated versions
    Page<Feedback> findByRating(int rating, Pageable pageable);
    Page<Feedback> findByCategory(Category category, Pageable pageable);
    Page<Feedback> findByCategoryAndRating(Category category, int rating, Pageable pageable);
    Page<Feedback> findByStatus(Status status, Pageable pageable);
    Page<Feedback> findByStatusAndCategory(Status status, Category category, Pageable pageable);
    Page<Feedback> findByPriority(Priority priority, Pageable pageable);
    Page<Feedback> findByPriorityAndCategory(Priority priority, Category category, Pageable pageable);
}