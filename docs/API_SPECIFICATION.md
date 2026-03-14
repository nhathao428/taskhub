# API Specification

## Authentication

### POST /auth/login
- **Description:** Log in a user and retrieve a token.
- **Request Body:**  
  - `email`: string 
  - `password`: string
- **Responses:**  
  - `200 OK`: Returns user object and token.  
  - `401 Unauthorized`: Invalid credentials.

### POST /auth/register
- **Description:** Register a new user.
- **Request Body:** 
  - `name`: string  
  - `email`: string  
  - `password`: string
- **Responses:**  
  - `201 Created`: User created successfully.  
  - `400 Bad Request`: Validation errors.


## Employees

### GET /employees
- **Description:** Retrieve a list of employees.
- **Responses:**  
  - `200 OK`: Returns an array of employee objects.

### POST /employees
- **Description:** Create a new employee.
- **Request Body:**  
  - `name`: string  
  - `email`: string  
  - `position`: string
- **Responses:**  
  - `201 Created`: Employee created successfully.  
  - `400 Bad Request`: Validation errors.


## Projects

### GET /projects
- **Description:** Retrieve a list of projects.
- **Responses:**  
  - `200 OK`: Returns an array of project objects.

### POST /projects
- **Description:** Create a new project.
- **Request Body:**  
  - `name`: string  
  - `description`: string  
  - `start_date`: string (date)
  - `end_date`: string (date)
- **Responses:**  
  - `201 Created`: Project created successfully.  
  - `400 Bad Request`: Validation errors.


## Tasks

### GET /tasks
- **Description:** Retrieve a list of tasks.
- **Responses:**  
  - `200 OK`: Returns an array of task objects.

### POST /tasks
- **Description:** Create a new task.
- **Request Body:**  
  - `title`: string  
  - `description`: string  
  - `due_date`: string (date)
- **Responses:**  
  - `201 Created`: Task created successfully.  
  - `400 Bad Request`: Validation errors.


## Attendance

### GET /attendance
- **Description:** Retrieve attendance records.
- **Responses:**  
  - `200 OK`: Returns an array of attendance records.

### POST /attendance
- **Description:** Log attendance for an employee.
- **Request Body:** 
  - `employee_id`: string  
  - `status`: string (e.g. present, absent)
- **Responses:**  
  - `201 Created`: Attendance logged successfully.  
  - `400 Bad Request`: Validation errors.


## AI Suggestions

### GET /suggestions
- **Description:** Get AI-generated suggestions for project management.
- **Responses:**  
  - `200 OK`: Returns an array of suggestions.

### POST /suggestions/feedback
- **Description:** Send feedback on AI suggestions.
- **Request Body:**  
  - `suggestion_id`: string  
  - `feedback`: string
- **Responses:**  
  - `200 OK`: Feedback submitted successfully.  
  - `400 Bad Request`: Validation errors.