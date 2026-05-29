-- Unified schema using UUID primary keys and foreign keys

CREATE EXTENSION IF NOT EXISTS "uuid-ossp";

CREATE TABLE IF NOT EXISTS departments (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    name VARCHAR(255) NOT NULL UNIQUE,
    created_at TIMESTAMP NOT NULL DEFAULT now()
);

CREATE TABLE IF NOT EXISTS academic_classes (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    name VARCHAR(255) NOT NULL,
    department_id UUID NOT NULL REFERENCES departments(id),
    UNIQUE(name, department_id),
    created_at TIMESTAMP NOT NULL DEFAULT now()
);

CREATE TABLE IF NOT EXISTS subjects (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    name VARCHAR(255) NOT NULL,
    department_id UUID NOT NULL REFERENCES departments(id),
    UNIQUE(name, department_id),
    created_at TIMESTAMP NOT NULL DEFAULT now()
);

CREATE TABLE IF NOT EXISTS users (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    email VARCHAR(255) NOT NULL UNIQUE,
    password_hash VARCHAR(255) NOT NULL,
    full_name VARCHAR(255) NOT NULL,
    role VARCHAR(20) NOT NULL,
    status VARCHAR(20) NOT NULL,
    department_id UUID REFERENCES departments(id),
    class_id UUID REFERENCES academic_classes(id),
    created_at TIMESTAMP NOT NULL DEFAULT now(),
    updated_at TIMESTAMP
);

CREATE TABLE IF NOT EXISTS staff_assignments (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    staff_id UUID NOT NULL REFERENCES users(id),
    subject_id UUID NOT NULL REFERENCES subjects(id),
    class_id UUID NOT NULL REFERENCES academic_classes(id),
    UNIQUE(staff_id, subject_id, class_id),
    created_at TIMESTAMP NOT NULL DEFAULT now()
);

CREATE TABLE IF NOT EXISTS courses (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    name VARCHAR(255) NOT NULL,
    description TEXT,
    teacher_id UUID NOT NULL REFERENCES users(id),
    subject_id UUID REFERENCES subjects(id),
    class_id UUID REFERENCES academic_classes(id),
    active BOOLEAN NOT NULL DEFAULT true,
    ai_mode VARCHAR(20),
    syllabus TEXT,
    schedule VARCHAR(255),
    student_count INTEGER DEFAULT 0,
    created_at TIMESTAMP NOT NULL DEFAULT now(),
    updated_at TIMESTAMP
);

CREATE TABLE IF NOT EXISTS sessions (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    course_id UUID NOT NULL REFERENCES courses(id),
    teacher_id UUID NOT NULL REFERENCES users(id),
    session_name VARCHAR(255) NOT NULL,
    start_time TIMESTAMP NOT NULL,
    end_time TIMESTAMP,
    is_active BOOLEAN NOT NULL DEFAULT false,
    participant_count INTEGER DEFAULT 0,
    ai_mode VARCHAR(20),
    created_at TIMESTAMP NOT NULL DEFAULT now()
);

CREATE TABLE IF NOT EXISTS chat_messages (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    session_id UUID NOT NULL REFERENCES sessions(id),
    user_id UUID REFERENCES users(id),
    content TEXT NOT NULL,
    is_ai_response BOOLEAN DEFAULT false,
    timestamp TIMESTAMP NOT NULL DEFAULT now()
);

CREATE TABLE IF NOT EXISTS progress (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    user_id UUID NOT NULL REFERENCES users(id),
    course_id UUID NOT NULL REFERENCES courses(id),
    completion DOUBLE PRECISION DEFAULT 0.0,
    quiz_score DOUBLE PRECISION DEFAULT 0.0,
    topics_learned INTEGER DEFAULT 0,
    questions_asked INTEGER DEFAULT 0,
    quizzes_taken INTEGER DEFAULT 0,
    attendance_rate DOUBLE PRECISION DEFAULT 100.0,
    created_at TIMESTAMP NOT NULL DEFAULT now(),
    updated_at TIMESTAMP
);

CREATE TABLE IF NOT EXISTS student_monitoring (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    student_id UUID NOT NULL REFERENCES users(id),
    session_id UUID NOT NULL REFERENCES sessions(id),
    activity VARCHAR(50),
    focus_score INTEGER,
    timestamp TIMESTAMP,
    ai_observation TEXT
);

-- additional tables
CREATE TABLE IF NOT EXISTS student_activity (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    session_id UUID NOT NULL REFERENCES sessions(id),
    student_id UUID NOT NULL REFERENCES users(id),
    join_time TIMESTAMP,
    leave_time TIMESTAMP,
    participation_score DOUBLE PRECISION
);

CREATE TABLE IF NOT EXISTS system_logs (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    level VARCHAR(50),
    action VARCHAR(255),
    details TEXT,
    timestamp TIMESTAMP
);

CREATE TABLE IF NOT EXISTS enrollments (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    student_id UUID NOT NULL REFERENCES users(id),
    course_id UUID NOT NULL REFERENCES courses(id),
    enrolled_at TIMESTAMP,
    status VARCHAR(20)
);

CREATE TABLE IF NOT EXISTS assignments (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    course_id UUID NOT NULL REFERENCES courses(id),
    title VARCHAR(255),
    description TEXT,
    due_date TIMESTAMP,
    grading_mode VARCHAR(20)
);

CREATE TABLE IF NOT EXISTS submissions (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    assignment_id UUID NOT NULL REFERENCES assignments(id),
    student_id UUID NOT NULL REFERENCES users(id),
    content TEXT,
    ai_score DOUBLE PRECISION,
    ai_feedback TEXT,
    submitted_at TIMESTAMP
);

CREATE TABLE IF NOT EXISTS recorded_lectures (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    session_id UUID NOT NULL REFERENCES sessions(id),
    video_url VARCHAR(1024),
    transcript TEXT,
    recorded_at TIMESTAMP
);

CREATE TABLE IF NOT EXISTS attendance_records (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    session_id UUID NOT NULL REFERENCES sessions(id),
    student_id UUID NOT NULL REFERENCES users(id),
    present BOOLEAN,
    face_confidence VARCHAR(50),
    marked_at TIMESTAMP
);

CREATE TABLE IF NOT EXISTS learning_paths (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    student_id UUID NOT NULL REFERENCES users(id),
    course_id UUID NOT NULL REFERENCES courses(id),
    path_json TEXT,
    generated_at TIMESTAMP
);

CREATE TABLE IF NOT EXISTS recommendations (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    student_id UUID NOT NULL REFERENCES users(id),
    course_id UUID NOT NULL REFERENCES courses(id),
    type VARCHAR(50),
    reason TEXT,
    score DOUBLE PRECISION,
    created_at TIMESTAMP
);

CREATE TABLE IF NOT EXISTS concept_mastery_profiles (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    student_id UUID NOT NULL REFERENCES users(id),
    course_id UUID NOT NULL REFERENCES courses(id),
    topic VARCHAR(255) NOT NULL,
    mastery_score INTEGER NOT NULL DEFAULT 0,
    difficulty_level VARCHAR(20) DEFAULT 'BEGINNER',
    attempt_count INTEGER DEFAULT 0,
    correct_count INTEGER DEFAULT 0,
    last_attempted_at TIMESTAMP,
    UNIQUE(student_id, course_id, topic)
);

CREATE TABLE IF NOT EXISTS concept_dependencies (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    course_id UUID NOT NULL REFERENCES courses(id),
    prerequisite_topic VARCHAR(255) NOT NULL,
    dependent_topic VARCHAR(255) NOT NULL,
    UNIQUE(course_id, prerequisite_topic, dependent_topic)
);

CREATE TABLE IF NOT EXISTS student_risk_profiles (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    student_id UUID NOT NULL REFERENCES users(id),
    risk_score INTEGER NOT NULL DEFAULT 0,
    risk_level VARCHAR(20) DEFAULT 'LOW',
    risk_factors TEXT,
    computed_at TIMESTAMP DEFAULT now()
);

CREATE TABLE IF NOT EXISTS sentiment_logs (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    student_id UUID NOT NULL REFERENCES users(id),
    session_id UUID NOT NULL REFERENCES sessions(id),
    sentiment VARCHAR(50),
    confidence DOUBLE PRECISION,
    raw_text TEXT,
    timestamp TIMESTAMP
);
