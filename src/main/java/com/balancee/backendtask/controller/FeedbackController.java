package com.balancee.backendtask.controller;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.balancee.backendtask.model.Feedback;
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
            @RequestParam Optional<LocalDateTime> startDate,
            @RequestParam Optional<LocalDateTime> endDate) {
        logger.info("Fetching feedback with filters: rating={}, startDate={}, endDate={}",
                rating.orElse(null), startDate.orElse(null), endDate.orElse(null));

        List<Feedback> feedbackList;
        if (rating.isPresent() && startDate.isPresent() && endDate.isPresent()) {
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
}