package com.balancee.backendtask.repository;

import com.balancee.backendtask.model.Feedback;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;

public interface FeedbackRepository extends JpaRepository<Feedback, String> {
    List<Feedback> findByRating(int rating);
    List<Feedback> findByCreatedAtBetween(LocalDateTime start, LocalDateTime end);
    List<Feedback> findByRatingAndCreatedAtBetween(int rating, LocalDateTime start, LocalDateTime end);
}