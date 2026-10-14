-- =======================================================
-- DigiView LMS - Supabase PostgreSQL Database Schema
-- Run this in your Supabase project's SQL Editor
-- =======================================================

-- 1. Users table (Students & Accounts)
CREATE TABLE IF NOT EXISTS users (
    id BIGSERIAL PRIMARY KEY,
    full_name VARCHAR(255) NOT NULL,
    age INT NOT NULL DEFAULT 16,
    gender VARCHAR(50) NOT NULL,
    strand VARCHAR(100) NOT NULL,
    grade_section VARCHAR(100) NOT NULL,
    username VARCHAR(100) UNIQUE NOT NULL,
    password VARCHAR(255) NOT NULL,
    xp INT NOT NULL DEFAULT 0,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

-- 2. Quiz History Records
CREATE TABLE IF NOT EXISTS quiz_records (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT REFERENCES users(id) ON DELETE CASCADE,
    username VARCHAR(100) NOT NULL,
    quiz_name VARCHAR(100) NOT NULL,
    score INT NOT NULL,
    total INT NOT NULL,
    percent INT NOT NULL,
    attempt_date VARCHAR(100) NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

-- 3. Achievements Table
CREATE TABLE IF NOT EXISTS user_achievements (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT REFERENCES users(id) ON DELETE CASCADE,
    username VARCHAR(100) NOT NULL,
    achievement_name VARCHAR(150) NOT NULL,
    unlocked_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    UNIQUE(user_id, achievement_name)
);

-- Performance Indexes
CREATE INDEX IF NOT EXISTS idx_users_username ON users(username);
CREATE INDEX IF NOT EXISTS idx_users_xp ON users(xp DESC);
CREATE INDEX IF NOT EXISTS idx_quiz_records_username ON quiz_records(username);
CREATE INDEX IF NOT EXISTS idx_achievements_username ON user_achievements(username);
