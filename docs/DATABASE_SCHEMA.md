-- PostgreSQL schema for task management system  

-- Users Table  
CREATE TABLE users (  
    user_id SERIAL PRIMARY KEY,  
    username VARCHAR(50) NOT NULL UNIQUE,  
    password VARCHAR(255) NOT NULL,  
    email VARCHAR(100) NOT NULL UNIQUE,  
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP  
);  

-- Employees Table  
CREATE TABLE employees (  
    employee_id SERIAL PRIMARY KEY,  
    user_id INTEGER REFERENCES users(user_id),  
    first_name VARCHAR(50) NOT NULL,  
    last_name VARCHAR(50) NOT NULL,  
    position VARCHAR(50),  
    department VARCHAR(50),  
    hired_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP  
);  

-- Projects Table  
CREATE TABLE projects (  
    project_id SERIAL PRIMARY KEY,  
    name VARCHAR(100) NOT NULL,  
    description TEXT,  
    start_date TIMESTAMP NOT NULL,  
    end_date TIMESTAMP,  
    status VARCHAR(50) DEFAULT 'ongoing'  
);  

-- Tasks Table  
CREATE TABLE tasks (  
    task_id SERIAL PRIMARY KEY,  
    project_id INTEGER REFERENCES projects(project_id),  
    assigned_to INTEGER REFERENCES employees(employee_id),  
    title VARCHAR(100) NOT NULL,  
    description TEXT,  
    due_date TIMESTAMP,  
    status VARCHAR(50) DEFAULT 'pending'  
);  

-- Attendance Table  
CREATE TABLE attendance (  
    attendance_id SERIAL PRIMARY KEY,  
    employee_id INTEGER REFERENCES employees(employee_id),  
    date DATE NOT NULL,  
    check_in TIMESTAMP NOT NULL,  
    check_out TIMESTAMP  
);  

-- Skills Table  
CREATE TABLE skills (  
    skill_id SERIAL PRIMARY KEY,  
    employee_id INTEGER REFERENCES employees(employee_id),  
    skill_name VARCHAR(100) NOT NULL,  
    proficiency_level VARCHAR(50)  
);  

-- Suggestions Table  
CREATE TABLE suggestions (  
    suggestion_id SERIAL PRIMARY KEY,  
    user_id INTEGER REFERENCES users(user_id),  
    suggestion_text TEXT NOT NULL,  
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP  
);  
