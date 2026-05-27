-- Baseline schema — PostgreSQL 16.
-- Tương ứng với entities tại commit ratification constitution v1.0.0 (2026-05-27).
-- Các DB đã tồn tại (dev/prod) sẽ được Flyway baseline ở version 1 mà KHÔNG chạy file
-- này (vì spring.flyway.baseline-on-migrate=true + baseline-version=1).
-- Fresh DB mới: file này tạo toàn bộ schema, từ đó các migration V2, V3... đi tiếp.

CREATE TABLE IF NOT EXISTS users (
    user_id     BIGSERIAL PRIMARY KEY,
    username    VARCHAR(50)  NOT NULL UNIQUE,
    password    VARCHAR(255) NOT NULL,
    email       VARCHAR(100) NOT NULL UNIQUE,
    role        VARCHAR(20)  NOT NULL DEFAULT 'EMPLOYEE',
    created_at  TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS employees (
    employee_id     BIGSERIAL PRIMARY KEY,
    -- 1 user MUST map to at most 1 employee (business rule: unique).
    user_id         BIGINT UNIQUE REFERENCES users(user_id),
    first_name      VARCHAR(50) NOT NULL,
    last_name       VARCHAR(50) NOT NULL,
    position        VARCHAR(50),
    department      VARCHAR(50),
    hired_at        TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    employee_group  VARCHAR(100),
    skills          TEXT
);

CREATE TABLE IF NOT EXISTS projects (
    project_id     BIGSERIAL PRIMARY KEY,
    name           VARCHAR(100) NOT NULL,
    description    TEXT,
    start_date     DATE NOT NULL,
    end_date       DATE,
    status         VARCHAR(50) DEFAULT 'ongoing',
    project_group  VARCHAR(100)
);

CREATE TABLE IF NOT EXISTS tasks (
    task_id          BIGSERIAL PRIMARY KEY,
    project_id       BIGINT REFERENCES projects(project_id) ON DELETE CASCADE,
    -- ON DELETE SET NULL: nhân viên nghỉ → task không bị xoá, chỉ mất người được giao
    -- (manager có thể gán lại cho người khác).
    assigned_to      BIGINT REFERENCES employees(employee_id) ON DELETE SET NULL,
    title            VARCHAR(100) NOT NULL,
    description      TEXT,
    required_skills  TEXT,
    due_date         DATE,
    status           VARCHAR(50) DEFAULT 'pending',
    completed_at     TIMESTAMP
);

CREATE TABLE IF NOT EXISTS office_locations (
    id              BIGSERIAL PRIMARY KEY,
    name            VARCHAR(200) NOT NULL,
    address         VARCHAR(500),
    latitude        DOUBLE PRECISION NOT NULL,
    longitude       DOUBLE PRECISION NOT NULL,
    radius_meters   INTEGER NOT NULL DEFAULT 100,
    status          VARCHAR(20) NOT NULL DEFAULT 'ACTIVE',
    created_at      TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS attendance (
    attendance_id          BIGSERIAL PRIMARY KEY,
    employee_id            BIGINT REFERENCES employees(employee_id) ON DELETE CASCADE,
    date                   DATE NOT NULL,
    check_in               TIME NOT NULL,
    check_out              TIME,
    check_in_lat           DOUBLE PRECISION,
    check_in_lng           DOUBLE PRECISION,
    check_out_lat          DOUBLE PRECISION,
    check_out_lng          DOUBLE PRECISION,
    check_in_office_id     BIGINT REFERENCES office_locations(id),
    check_in_distance_m    INTEGER,
    review_status          VARCHAR(20) DEFAULT 'APPROVED',
    is_mocked              BOOLEAN DEFAULT FALSE
);

CREATE TABLE IF NOT EXISTS suggestions (
    suggestion_id    BIGSERIAL PRIMARY KEY,
    user_id          BIGINT REFERENCES users(user_id),
    suggestion_text  TEXT NOT NULL,
    feedback         TEXT,
    created_at       TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- Indexes (truy vấn thường gặp) — Principle IV (Performance Requirements).
-- employees.user_id đã là UNIQUE constraint ở column → PostgreSQL tự tạo unique index,
-- không cần khai báo lại.
CREATE INDEX IF NOT EXISTS idx_tasks_project_id           ON tasks (project_id);
CREATE INDEX IF NOT EXISTS idx_tasks_assigned_to          ON tasks (assigned_to);
CREATE INDEX IF NOT EXISTS idx_attendance_employee_date   ON attendance (employee_id, date);
CREATE INDEX IF NOT EXISTS idx_attendance_review_status   ON attendance (review_status);
CREATE INDEX IF NOT EXISTS idx_suggestions_user_id        ON suggestions (user_id);
