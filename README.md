# Feedback Management System

## Overview
A simple feedback management system built with Spring Boot. Using this project to get familiar with how springboot works basically

## Core Features

### **User Management & Authentication**
- **JWT Authentication**
- **User Registration/Login**
- **Role-Based Access**: USER and ADMIN roles

### **Feedback Management**
- **Anonymous Feedback**
- **Categorization**: Bug Report, Feature Request, General
- **Status Tracking**: NEW → IN_PROGRESS → RESOLVED → CLOSED
- **Priority Levels**: LOW, MEDIUM, HIGH
- **Admin Responses**: Admins can respond to feedback
- **File Attachments**: Upload screenshots and documents

### **Advanced Features**
- **Pagination & Sorting**
- **Rate Limiting**: 10 requests/minute per IP
- **Comprehensive Filtering**: Filter by category, status, priority, rating, date range
- **Audit Logging**
- **Input Validation**

## API Endpoints

### **Authentication**
```http
POST /api/auth/register    # User registration
POST /api/auth/login       # User login
```

### **Feedback Submission**
```http
POST /api/feedback         # Submit feedback (public)
```

### **Admin Management** (Requires ADMIN role)
```http
GET  /api/admin/feedback                    # Get paginated feedback with filters
PUT  /api/admin/feedback/{id}/status        # Update feedback status
PUT  /api/admin/feedback/{id}/priority      # Update feedback priority
POST /api/admin/feedback/{id}/response      # Add admin response
GET  /api/admin/feedback/{id}/responses     # Get feedback responses
```

### **File Management**
```http
POST /api/feedback/{id}/attachments    # Upload file attachment
GET  /api/feedback/{id}/attachments    # Get feedback attachments
```

## Advanced Query Parameters

### **Pagination & Sorting**
```http
GET /api/admin/feedback?page=0&size=10&sortBy=createdAt&sortDir=desc
```

### **Filtering**
```http
GET /api/admin/feedback?category=BUG_REPORT&status=NEW&priority=HIGH&rating=1
```

## 🧪 Testing
```bash
# Run all tests
mvn test

# Run specific test class
mvn test -Dtest=FeedbackControllerTest
mvn test -Dtest=AuthControllerTest
```

## 🚀 Getting Started

1. **Clone the repository**
2. **Run the application**:
   ```bash
   mvn spring-boot:run
   ```
3. **Access the API**: `http://localhost:8080`
4. **Register a user** and start submitting feedback!

## 📈 Performance Features
- **Pagination**: Efficient handling of large datasets
- **Database Indexing**: Optimized queries
- **Rate Limiting**: Prevents system abuse
- **Caching**: Reduced database load