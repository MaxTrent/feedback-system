package com.balancee.backendtask.controller;

import com.balancee.backendtask.model.Feedback;
import com.balancee.backendtask.repository.FeedbackRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class FeedbackControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private FeedbackRepository repository;

    @Autowired
    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        repository.deleteAll();
    }

    @Test
    void shouldCreateFeedbackWithValidInput() throws Exception {
        Feedback feedback = new Feedback();
        feedback.setUserId("user1");
        feedback.setMessage("Great app!");
        feedback.setRating(5);

        mockMvc.perform(post("/api/feedback")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(feedback)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.userId").value("user1"))
                .andExpect(jsonPath("$.message").value("Great app!"))
                .andExpect(jsonPath("$.rating").value(5))
                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.createdAt").exists());
    }

    @Test
    void shouldRejectInvalidRating() throws Exception {
        Feedback feedback = new Feedback();
        feedback.setUserId("user1");
        feedback.setMessage("Great app!");
        feedback.setRating(6);

        mockMvc.perform(post("/api/feedback")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(feedback)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.rating").value("rating must be at most 5"));
    }

    @Test
    void shouldRejectEmptyUserId() throws Exception {
        Feedback feedback = new Feedback();
        feedback.setUserId("");
        feedback.setMessage("Great app!");
        feedback.setRating(5);

        mockMvc.perform(post("/api/feedback")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(feedback)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.userId").value("userId is required"));
    }

    @Test
    void shouldRejectEmptyMessage() throws Exception {
        Feedback feedback = new Feedback();
        feedback.setUserId("user1");
        feedback.setMessage("");
        feedback.setRating(5);

        mockMvc.perform(post("/api/feedback")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(feedback)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("message is required"));
    }

    @Test
    @WithMockUser(username = "admin", roles = {"ADMIN"})
    void shouldReturnAllFeedbackForAdmin() throws Exception {
        Feedback feedback1 = new Feedback();
        feedback1.setUserId("user1");
        feedback1.setMessage("Great app!");
        feedback1.setRating(5);
        repository.save(feedback1);

        Feedback feedback2 = new Feedback();
        feedback2.setUserId("user2");
        feedback2.setMessage("Needs work");
        feedback2.setRating(3);
        repository.save(feedback2);

        mockMvc.perform(get("/api/admin/feedback"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].rating").value(5))
                .andExpect(jsonPath("$[1].rating").value(3));
    }

    @Test
    @WithMockUser(username = "admin", roles = {"ADMIN"})
    void shouldFilterFeedbackByRating() throws Exception {
        Feedback feedback1 = new Feedback();
        feedback1.setUserId("user1");
        feedback1.setMessage("Great app!");
        feedback1.setRating(5);
        repository.save(feedback1);

        Feedback feedback2 = new Feedback();
        feedback2.setUserId("user2");
        feedback2.setMessage("Needs work");
        feedback2.setRating(3);
        repository.save(feedback2);

        mockMvc.perform(get("/api/admin/feedback?rating=5"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].rating").value(5));
    }

    @Test
    void shouldRejectUnauthorizedAccess() throws Exception {
        mockMvc.perform(get("/api/admin/feedback"))
                .andExpect(status().isUnauthorized());
    }
}