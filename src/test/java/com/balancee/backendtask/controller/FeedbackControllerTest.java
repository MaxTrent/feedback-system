package com.balancee.backendtask.controller;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.balancee.backendtask.model.Feedback;
import com.balancee.backendtask.model.Category;
import com.balancee.backendtask.model.Status;
import com.balancee.backendtask.model.Priority;
import com.balancee.backendtask.model.AdminResponse;
import com.balancee.backendtask.repository.FeedbackRepository;
import com.balancee.backendtask.repository.AdminResponseRepository;
import com.fasterxml.jackson.databind.ObjectMapper;



@SpringBootTest
@AutoConfigureMockMvc
class FeedbackControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private FeedbackRepository repository;

    @Autowired
    private AdminResponseRepository adminResponseRepository;

    @Autowired
    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        adminResponseRepository.deleteAll();
        repository.deleteAll();
    }

    @Test
    void shouldCreateFeedbackWithValidInput() throws Exception {
        Feedback feedback = new Feedback();
        feedback.setUserId("user1");
        feedback.setMessage("Great app!");
        feedback.setRating(5);
        feedback.setCategory(Category.GENERAL);

        mockMvc.perform(post("/api/feedback")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(feedback)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.userId").value("user1"))
                .andExpect(jsonPath("$.message").value("Great app!"))
                .andExpect(jsonPath("$.rating").value(5))
                .andExpect(jsonPath("$.category").value("GENERAL"))
                .andExpect(jsonPath("$.status").value("NEW"))
                .andExpect(jsonPath("$.priority").value("MEDIUM"))
                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.createdAt").exists());
    }

    @Test
    void shouldRejectInvalidRating() throws Exception {
        Feedback feedback = new Feedback();
        feedback.setUserId("user1");
        feedback.setMessage("Great app!");
        feedback.setRating(6);
        feedback.setCategory(Category.GENERAL);

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
        feedback.setCategory(Category.GENERAL);

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
        feedback.setCategory(Category.GENERAL);

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
        feedback1.setCategory(Category.GENERAL);
        repository.save(feedback1);

        Feedback feedback2 = new Feedback();
        feedback2.setUserId("user2");
        feedback2.setMessage("Needs work");
        feedback2.setRating(3);
        feedback2.setCategory(Category.BUG_REPORT);
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
        feedback1.setCategory(Category.GENERAL);
        repository.save(feedback1);

        Feedback feedback2 = new Feedback();
        feedback2.setUserId("user2");
        feedback2.setMessage("Needs work");
        feedback2.setRating(3);
        feedback2.setCategory(Category.BUG_REPORT);
        repository.save(feedback2);

        mockMvc.perform(get("/api/admin/feedback?rating=5"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].rating").value(5));
    }

    @Test
    @WithMockUser(username = "admin", roles = {"ADMIN"})
    void shouldFilterFeedbackByCategory() throws Exception {
        Feedback feedback1 = new Feedback();
        feedback1.setUserId("user1");
        feedback1.setMessage("Found a bug!");
        feedback1.setRating(2);
        feedback1.setCategory(Category.BUG_REPORT);
        repository.save(feedback1);

        Feedback feedback2 = new Feedback();
        feedback2.setUserId("user2");
        feedback2.setMessage("Great app!");
        feedback2.setRating(5);
        feedback2.setCategory(Category.GENERAL);
        repository.save(feedback2);

        mockMvc.perform(get("/api/admin/feedback?category=BUG_REPORT"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].category").value("BUG_REPORT"));
    }

    @Test
    @WithMockUser(username = "admin", roles = {"ADMIN"})
    void shouldFilterFeedbackByStatus() throws Exception {
        Feedback feedback1 = new Feedback();
        feedback1.setUserId("user1");
        feedback1.setMessage("Bug found!");
        feedback1.setRating(2);
        feedback1.setCategory(Category.BUG_REPORT);
        feedback1.setStatus(Status.IN_PROGRESS);
        repository.save(feedback1);

        Feedback feedback2 = new Feedback();
        feedback2.setUserId("user2");
        feedback2.setMessage("Great app!");
        feedback2.setRating(5);
        feedback2.setCategory(Category.GENERAL);
        repository.save(feedback2);

        mockMvc.perform(get("/api/admin/feedback?status=IN_PROGRESS"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].status").value("IN_PROGRESS"));
    }

    @Test
    @WithMockUser(username = "admin", roles = {"ADMIN"})
    void shouldUpdateFeedbackStatus() throws Exception {
        Feedback feedback = new Feedback();
        feedback.setUserId("user1");
        feedback.setMessage("Bug report");
        feedback.setRating(2);
        feedback.setCategory(Category.BUG_REPORT);
        Feedback saved = repository.save(feedback);

        mockMvc.perform(put("/api/admin/feedback/" + saved.getId() + "/status?status=RESOLVED"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("RESOLVED"));
    }

    @Test
    @WithMockUser(username = "admin", roles = {"ADMIN"})
    void shouldAddAdminResponse() throws Exception {
        Feedback feedback = new Feedback();
        feedback.setUserId("user1");
        feedback.setMessage("Bug report");
        feedback.setRating(2);
        feedback.setCategory(Category.BUG_REPORT);
        Feedback saved = repository.save(feedback);

        AdminResponse response = new AdminResponse();
        response.setResponse("Thank you for the report. We're working on it.");
        response.setAdminId("admin1");

        mockMvc.perform(post("/api/admin/feedback/" + saved.getId() + "/response")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(response)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.response").value("Thank you for the report. We're working on it."))
                .andExpect(jsonPath("$.adminId").value("admin1"))
                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.createdAt").exists());
    }

    @Test
    @WithMockUser(username = "admin", roles = {"ADMIN"})
    void shouldGetFeedbackResponses() throws Exception {
        Feedback feedback = new Feedback();
        feedback.setUserId("user1");
        feedback.setMessage("Bug report");
        feedback.setRating(2);
        feedback.setCategory(Category.BUG_REPORT);
        Feedback saved = repository.save(feedback);

        AdminResponse response = new AdminResponse();
        response.setResponse("We're investigating this issue.");
        response.setAdminId("admin1");
        response.setFeedback(saved);
        adminResponseRepository.save(response);

        mockMvc.perform(get("/api/admin/feedback/" + saved.getId() + "/responses"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].response").value("We're investigating this issue."))
                .andExpect(jsonPath("$[0].adminId").value("admin1"));
    }

    @Test
    @WithMockUser(username = "admin", roles = {"ADMIN"})
    void shouldFilterFeedbackByPriority() throws Exception {
        Feedback feedback1 = new Feedback();
        feedback1.setUserId("user1");
        feedback1.setMessage("Critical bug!");
        feedback1.setRating(1);
        feedback1.setCategory(Category.BUG_REPORT);
        feedback1.setPriority(Priority.HIGH);
        repository.save(feedback1);

        Feedback feedback2 = new Feedback();
        feedback2.setUserId("user2");
        feedback2.setMessage("Minor issue");
        feedback2.setRating(4);
        feedback2.setCategory(Category.GENERAL);
        repository.save(feedback2);

        mockMvc.perform(get("/api/admin/feedback?priority=HIGH"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].priority").value("HIGH"));
    }

    @Test
    @WithMockUser(username = "admin", roles = {"ADMIN"})
    void shouldUpdateFeedbackPriority() throws Exception {
        Feedback feedback = new Feedback();
        feedback.setUserId("user1");
        feedback.setMessage("Important feedback");
        feedback.setRating(3);
        feedback.setCategory(Category.FEATURE_REQUEST);
        Feedback saved = repository.save(feedback);

        mockMvc.perform(put("/api/admin/feedback/" + saved.getId() + "/priority?priority=HIGH"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.priority").value("HIGH"));
    }

    @Test
    void shouldRejectUnauthorizedAccess() throws Exception {
        mockMvc.perform(get("/api/admin/feedback"))
                .andExpect(status().isUnauthorized());
    }
}