# Feedback-System

## Overview
Feedback System is a simple User Feedback System built with Spring Boot and Java, using an in-memory H2 database. Users can submit feedback with a message and a rating (1–5), and admins can view all feedback with optional filtering by rating or date range.

## Features
- **POST /api/feedback**: Allows users to submit feedback.
- **GET /api/admin/feedback**: Allows admins to retrieve feedback, with optional filters for rating and date range.
- Input validation for `userId`, `message`, and `rating` (1–5).
- Admin authentication using HTTP Basic (username: `admin`, password: `admin-pass`).
- Timestamps for feedback entries (`createdAt`).
- Unit tests with JUnit and MockMvc.
- Logging with SLF4J for transparency.
