# Balancee-Backend-Task

## Overview
Balancee-Backend-Task is a User Feedback System built with Spring Boot and Java, using an in-memory H2 database. Users can submit feedback with a message and a rating (1–5), and admins can view all feedback with optional filtering by rating or date range.

## Features
- **POST /api/feedback**: Allows users to submit feedback.
- **GET /api/admin/feedback**: Allows admins to retrieve feedback, with optional filters for rating and date range.
- Input validation for `userId`, `message`, and `rating` (1–5).
- Admin authentication using HTTP Basic (username: `admin`, password: `admin-pass`).
- Timestamps for feedback entries (`createdAt`).
- Unit tests with JUnit and MockMvc.
- Logging with SLF4J for transparency.

## Example Requests and Responses

### POST /api/feedback
**Request:**
```bash
curl -X POST http://localhost:8080/api/feedback \
-H "Content-Type: application/json" \
-d '{"userId": "user1", "message": "Great app!", "rating": 5}'

Response (201 Created):
{
  "id": "123e4567-e89b-12d3-a456-426614174000",
  "userId": "user1",
  "message": "Great app!",
  "rating": 5,
  "createdAt": "2025-05-22T16:17:00"
}

Error Response (400 Bad Request):
{
  "rating": "rating must be at most 5"
}

GET /api/admin/feedback
Request:
curl -X GET http://localhost:8080/api/admin/feedback \
-H "Authorization: Basic YWRtaW46YWRtaW4tcGFzcw=="

Response (200 OK):
[
  {
    "id": "123e4567-e89b-12d3-a456-426614174000",
    "userId": "user1",
    "message": "Great app!",
    "rating": 5,
    "createdAt": "2025-05-22T16:17:00"
  },
  {
    "id": "987fcdeb-12ab-34cd-56ef-426614174001",
    "userId": "user2",
    "message": "Needs work",
    "rating": 3,
    "createdAt": "2025-05-22T16:18:00"
  }
]

Filtered Request (by rating):
curl -X GET "http://localhost:8080/api/admin/feedback?rating=5" \
-H "Authorization: Basic YWRtaW46YWRtaW4tcGFzcw=="

Filtered Response (200 OK):
[
  {
    "id": "123e4567-e89b-12d3-a456-426614174000",
    "userId": "user1",
    "message": "Great app!",
    "rating": 5,
    "createdAt": "2025-05-22T16:17:00"
  }
]

Error Response (401 Unauthorized):
{
  "status": 401,
  "error": "Unauthorized",
  "message": "Unauthorized",
  "path": "/api/admin/feedback"
}

Setup and Running Locally

Prerequisites:

Java 17 or later
Maven
Homebrew (optional, for easier installation)


Installation:
cd Balancee-Backend-Task
mvn install


Running the Service:
mvn spring-boot:run

The server runs on http://localhost:8080.

Running Tests:
mvn test

Tests cover valid/invalid feedback creation, admin feedback retrieval, and unauthorized access.


Dependencies

spring-boot-starter-web: Web framework
spring-boot-starter-data-jpa: JPA for database access
spring-boot-starter-security: Security for admin authentication
h2: In-memory database
spring-boot-starter-test, spring-security-test: Testing



