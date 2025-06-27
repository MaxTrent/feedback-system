package com.balancee.backendtask.controller;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

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
import com.balancee.backendtask.model.Priority;
import com.balancee.backendtask.model.AdminResponse;
import com.balancee.backendtask.model.Attachment;
import com.balancee.backendtask.repository.FeedbackRepository;
import com.balancee.backendtask.repository.AdminResponseRepository;
import com.balancee.backendtask.repository.AttachmentRepository;

import org.springframework.web.multipart.MultipartFile;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api")
public class FeedbackController {
    private static final Logger logger = LoggerFactory.getLogger(FeedbackController.class);
    private final FeedbackRepository repository;
    private final AdminResponseRepository adminResponseRepository;
    private final AttachmentRepository attachmentRepository;
    private final String uploadDir = "uploads/";

    public FeedbackController(FeedbackRepository repository, AdminResponseRepository adminResponseRepository, AttachmentRepository attachmentRepository) {
        this.repository = repository;
        this.adminResponseRepository = adminResponseRepository;
        this.attachmentRepository = attachmentRepository;
        
        // Create upload directory if it doesn't exist
        try {
            Files.createDirectories(Paths.get(uploadDir));
        } catch (Exception e) {
            logger.error("Failed to create upload directory", e);
        }
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
    public ResponseEntity<Page<Feedback>> getAllFeedback(
            @RequestParam Optional<Integer> rating,
            @RequestParam Optional<Category> category,
            @RequestParam Optional<Status> status,
            @RequestParam Optional<Priority> priority,
            @RequestParam Optional<LocalDateTime> startDate,
            @RequestParam Optional<LocalDateTime> endDate,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDir) {
        Sort sort = Sort.by(sortDir.equalsIgnoreCase("desc") ? Sort.Direction.DESC : Sort.Direction.ASC, sortBy);
        Pageable pageable = PageRequest.of(page, size, sort);
        
        logger.info("Fetching feedback with filters and pagination: page={}, size={}, sortBy={}, sortDir={}", 
                page, size, sortBy, sortDir);

        Page<Feedback> feedbackPage;
        if (priority.isPresent() && category.isPresent()) {
            feedbackPage = repository.findByPriorityAndCategory(priority.get(), category.get(), pageable);
        } else if (priority.isPresent()) {
            feedbackPage = repository.findByPriority(priority.get(), pageable);
        } else if (status.isPresent() && category.isPresent()) {
            feedbackPage = repository.findByStatusAndCategory(status.get(), category.get(), pageable);
        } else if (status.isPresent()) {
            feedbackPage = repository.findByStatus(status.get(), pageable);
        } else if (rating.isPresent() && category.isPresent()) {
            feedbackPage = repository.findByCategoryAndRating(category.get(), rating.get(), pageable);
        } else if (category.isPresent()) {
            feedbackPage = repository.findByCategory(category.get(), pageable);
        } else if (rating.isPresent()) {
            feedbackPage = repository.findByRating(rating.get(), pageable);
        } else {
            feedbackPage = repository.findAll(pageable);
        }

        logger.info("Returning {} feedback entries (page {} of {})", 
                feedbackPage.getNumberOfElements(), feedbackPage.getNumber() + 1, feedbackPage.getTotalPages());
        return ResponseEntity.ok(feedbackPage);
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

    @PostMapping("/admin/feedback/{id}/response")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<AdminResponse> addAdminResponse(
            @PathVariable UUID id,
            @Valid @RequestBody AdminResponse adminResponse) {
        logger.info("Adding admin response to feedback {}", id);
        
        Optional<Feedback> feedbackOpt = repository.findById(id);
        if (feedbackOpt.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        
        adminResponse.setFeedback(feedbackOpt.get());
        AdminResponse saved = adminResponseRepository.save(adminResponse);
        
        logger.info("Admin response added to feedback {} by admin {}", id, adminResponse.getAdminId());
        return ResponseEntity.status(HttpStatus.CREATED).body(saved);
    }

    @GetMapping("/admin/feedback/{id}/responses")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<AdminResponse>> getFeedbackResponses(@PathVariable UUID id) {
        Optional<Feedback> feedbackOpt = repository.findById(id);
        if (feedbackOpt.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        
        List<AdminResponse> responses = adminResponseRepository.findByFeedback(feedbackOpt.get());
        return ResponseEntity.ok(responses);
    }

    @PutMapping("/admin/feedback/{id}/priority")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Feedback> updateFeedbackPriority(
            @PathVariable UUID id,
            @RequestParam Priority priority) {
        logger.info("Updating feedback {} priority to {}", id, priority);
        
        Optional<Feedback> feedbackOpt = repository.findById(id);
        if (feedbackOpt.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        
        Feedback feedback = feedbackOpt.get();
        feedback.setPriority(priority);
        Feedback updated = repository.save(feedback);
        
        logger.info("Feedback {} priority updated to {}", id, priority);
        return ResponseEntity.ok(updated);
    }

    @PostMapping("/feedback/{id}/attachments")
    public ResponseEntity<?> uploadAttachment(
            @PathVariable UUID id,
            @RequestParam("file") MultipartFile file) {
        
        if (file.isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of("error", "File is required"));
        }
        
        Optional<Feedback> feedbackOpt = repository.findById(id);
        if (feedbackOpt.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        
        try {
            String fileName = System.currentTimeMillis() + "_" + file.getOriginalFilename();
            Path filePath = Paths.get(uploadDir + fileName);
            Files.copy(file.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);
            
            Attachment attachment = new Attachment();
            attachment.setFeedback(feedbackOpt.get());
            attachment.setFileName(file.getOriginalFilename());
            attachment.setContentType(file.getContentType());
            attachment.setFileSize(file.getSize());
            attachment.setFilePath(filePath.toString());
            
            Attachment saved = attachmentRepository.save(attachment);
            logger.info("File uploaded for feedback {}: {}", id, fileName);
            
            return ResponseEntity.status(HttpStatus.CREATED).body(saved);
        } catch (Exception e) {
            logger.error("Failed to upload file", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Failed to upload file"));
        }
    }

    @GetMapping("/feedback/{id}/attachments")
    public ResponseEntity<List<Attachment>> getFeedbackAttachments(@PathVariable UUID id) {
        Optional<Feedback> feedbackOpt = repository.findById(id);
        if (feedbackOpt.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        
        List<Attachment> attachments = attachmentRepository.findByFeedback(feedbackOpt.get());
        return ResponseEntity.ok(attachments);
    }
}