package com.balancee.backendtask.controller;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.balancee.backendtask.model.Feedback;
import com.balancee.backendtask.model.Category;
import com.balancee.backendtask.model.Status;
import com.balancee.backendtask.repository.FeedbackRepository;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api")
public class FeedbackController {
    private static final Logger logger = LoggerFactory.getLogger(FeedbackController.class);
    private final FeedbackRepository repository;

    public FeedbackController(FeedbackRepository repository) {
        this.repository = repository;
    }

    @PostMapping("/feedback")
    public ResponseEntity<Feedback> createFeedback(@Valid @RequestBody Feedback feedback) {
        logger.info("Received feedback submission: userId={}, rating={}", feedback.getUserId(), feedback.getRating());
        Feedback saved = repository.save(feedback);
        logger.info("Feedback saved with ID: {}", saved.getId());
        return ResponseEntity.status(HttpStatus.CREATED).body(saved);
    }

    @GetMapping("/admin/feedback")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<Feedback>> getAllFeedback(
            @RequestParam Optional<Integer> rating,
            @RequestParam Optional<Category> category,
            @RequestParam Optional<Status> status,
            @RequestParam Optional<LocalDateTime> startDate,
            @RequestParam Optional<LocalDateTime> endDate) {
        logger.info("Fetching feedback with filters: rating={}, category={}, status={}, startDate={}, endDate={}",
                rating.orElse(null), category.orElse(null), status.orElse(null), startDate.orElse(null), endDate.orElse(null));

        List<Feedback> feedbackList;
        if (status.isPresent() && category.isPresent()) {
            feedbackList = repository.findByStatusAndCategory(status.get(), category.get());
        } else if (status.isPresent()) {
            feedbackList = repository.findByStatus(status.get());
        } else if (rating.isPresent() && category.isPresent()) {
            feedbackList = repository.findByCategoryAndRating(category.get(), rating.get());
        } else if (category.isPresent()) {
            feedbackList = repository.findByCategory(category.get());
        } else if (rating.isPresent() && startDate.isPresent() && endDate.isPresent()) {
            feedbackList = repository.findByRatingAndCreatedAtBetween(rating.get(), startDate.get(), endDate.get());
        } else if (rating.isPresent()) {
            feedbackList = repository.findByRating(rating.get());
        } else if (startDate.isPresent() && endDate.isPresent()) {
            feedbackList = repository.findByCreatedAtBetween(startDate.get(), endDate.get());
        } else {
            feedbackList = repository.findAll();
        }

        logger.info("Returning {} feedback entries", feedbackList.size());
        return ResponseEntity.ok(feedbackList);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, String>> handleValidationExceptions(MethodArgumentNotValidException ex) {
        Map<String, String> errors = new HashMap<>();
        ex.getBindingResult().getAllErrors().forEach((error) -> {
            String fieldName = ((FieldError) error).getField();
            String errorMessage = error.getDefaultMessage();
            errors.put(fieldName, errorMessage);
            logger.error("Validation error on field {}: {}", fieldName, errorMessage);
        });
        return ResponseEntity.badRequest().body(errors);
    }

    @PutMapping("/admin/feedback/{id}/status")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Feedback> updateFeedbackStatus(
            @PathVariable UUID id,
            @RequestParam Status status) {
        logger.info("Updating feedback {} status to {}", id, status);
        
        Optional<Feedback> feedbackOpt = repository.findById(id);
        if (feedbackOpt.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        
        Feedback feedback = feedbackOpt.get();
        feedback.setStatus(status);
        Feedback updated = repository.save(feedback);
        
        logger.info("Feedback {} status updated to {}", id, status);
        return ResponseEntity.ok(updated);
    }
}