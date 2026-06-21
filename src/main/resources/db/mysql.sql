-- MySQL建表语句

-- 创建数据库
CREATE DATABASE IF NOT EXISTS education_system CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE education_system;

-- 1. 用户表 (users)
CREATE TABLE IF NOT EXISTS users (
    user_id INT AUTO_INCREMENT PRIMARY KEY,
    username VARCHAR(50) NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL,
    email VARCHAR(100) NOT NULL UNIQUE,
    phone VARCHAR(20) NOT NULL UNIQUE,
    role ENUM('admin', 'teacher', 'student') NOT NULL,
    avatar_id INT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_role (role)
);

-- 2. 教师表 (teachers)
CREATE TABLE IF NOT EXISTS teachers (
    teacher_id INT AUTO_INCREMENT PRIMARY KEY,
    user_id INT NOT NULL,
    name VARCHAR(50) NOT NULL,
    title VARCHAR(50),
    department VARCHAR(100),
    bio TEXT,
    FOREIGN KEY (user_id) REFERENCES users(user_id) ON DELETE CASCADE
);

-- 3. 学生表 (students)
CREATE TABLE IF NOT EXISTS students (
    student_id INT AUTO_INCREMENT PRIMARY KEY,
    user_id INT NOT NULL,
    name VARCHAR(50) NOT NULL,
    student_number VARCHAR(20) NOT NULL UNIQUE,
    major VARCHAR(100),
    grade VARCHAR(20),
    class VARCHAR(50),
    department VARCHAR(100),
    gender VARCHAR(10),
    FOREIGN KEY (user_id) REFERENCES users(user_id) ON DELETE CASCADE
);

-- 4. 课程表 (courses)
CREATE TABLE IF NOT EXISTS courses (
    course_id INT AUTO_INCREMENT PRIMARY KEY,
    course_name VARCHAR(100) NOT NULL,
    credit DECIMAL(3,1) NOT NULL,
    course_code VARCHAR(20) NOT NULL UNIQUE,
    description TEXT,
    INDEX idx_course_code (course_code)
);

-- 5. 排课表 (course_schedules)
CREATE TABLE IF NOT EXISTS course_schedules (
    schedule_id INT AUTO_INCREMENT PRIMARY KEY,
    course_id INT NOT NULL,
    teacher_id INT NOT NULL,
    classroom VARCHAR(50) NOT NULL,
    day_of_week INT NOT NULL,
    start_time TIME NOT NULL,
    end_time TIME NOT NULL,
    semester VARCHAR(20) NOT NULL,
    year INT NOT NULL,
    FOREIGN KEY (course_id) REFERENCES courses(course_id) ON DELETE CASCADE,
    FOREIGN KEY (teacher_id) REFERENCES teachers(teacher_id) ON DELETE CASCADE,
    INDEX idx_teacher_semester (teacher_id, semester, year),
    INDEX idx_classroom_time (classroom, day_of_week, start_time)
);

-- 6. 选课表 (course_enrollments)
CREATE TABLE IF NOT EXISTS course_enrollments (
    enrollment_id INT AUTO_INCREMENT PRIMARY KEY,
    student_id INT NOT NULL,
    schedule_id INT NOT NULL,
    enroll_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    status ENUM('enrolled', 'dropped') DEFAULT 'enrolled',
    FOREIGN KEY (student_id) REFERENCES students(student_id) ON DELETE CASCADE,
    FOREIGN KEY (schedule_id) REFERENCES course_schedules(schedule_id) ON DELETE CASCADE,
    UNIQUE KEY uk_student_schedule (student_id, schedule_id)
);

-- 7. 成绩表 (grades)
CREATE TABLE IF NOT EXISTS grades (
    grade_id INT AUTO_INCREMENT PRIMARY KEY,
    enrollment_id INT NOT NULL,
    score DECIMAL(5,2) NOT NULL,
    grade_level ENUM('excellent', 'good', 'average', 'pass', 'fail') NOT NULL,
    teacher_id INT NOT NULL,
    comment TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (enrollment_id) REFERENCES course_enrollments(enrollment_id) ON DELETE CASCADE,
    FOREIGN KEY (teacher_id) REFERENCES teachers(teacher_id) ON DELETE CASCADE,
    INDEX idx_student_score (enrollment_id, score),
    INDEX idx_grade_level (grade_level)
);

-- 8. 科研项目表 (research_projects)
CREATE TABLE IF NOT EXISTS research_projects (
    project_id INT AUTO_INCREMENT PRIMARY KEY,
    project_name VARCHAR(200) NOT NULL,
    description TEXT,
    teacher_id INT NOT NULL,
    status ENUM('open', 'closed') DEFAULT 'open',
    start_date DATE,
    end_date DATE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (teacher_id) REFERENCES teachers(teacher_id) ON DELETE CASCADE,
    INDEX idx_project_status (status)
);

-- 9. 科研项目申请表 (project_applications)
CREATE TABLE IF NOT EXISTS project_applications (
    application_id INT AUTO_INCREMENT PRIMARY KEY,
    project_id INT NOT NULL,
    student_id INT NOT NULL,
    application_letter TEXT,
    status ENUM('pending', 'approved', 'rejected') DEFAULT 'pending',
    apply_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    review_time TIMESTAMP,
    FOREIGN KEY (project_id) REFERENCES research_projects(project_id) ON DELETE CASCADE,
    FOREIGN KEY (student_id) REFERENCES students(student_id) ON DELETE CASCADE,
    UNIQUE KEY uk_project_student (project_id, student_id),
    INDEX idx_application_status (status)
);

-- 10. 大创赛组队表 (innovation_teams)
CREATE TABLE IF NOT EXISTS innovation_teams (
    team_id INT AUTO_INCREMENT PRIMARY KEY,
    team_name VARCHAR(100) NOT NULL,
    project_name VARCHAR(200) NOT NULL,
    leader_id INT NOT NULL,
    status ENUM('recruiting', 'closed') DEFAULT 'recruiting',
    description TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (leader_id) REFERENCES students(student_id) ON DELETE CASCADE,
    INDEX idx_team_status (status)
);

-- 11. 组队申请表 (team_applications)
CREATE TABLE IF NOT EXISTS team_applications (
    application_id INT AUTO_INCREMENT PRIMARY KEY,
    team_id INT NOT NULL,
    student_id INT NOT NULL,
    application_letter TEXT,
    status ENUM('pending', 'approved', 'rejected') DEFAULT 'pending',
    apply_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    review_time TIMESTAMP,
    FOREIGN KEY (team_id) REFERENCES innovation_teams(team_id) ON DELETE CASCADE,
    FOREIGN KEY (student_id) REFERENCES students(student_id) ON DELETE CASCADE,
    UNIQUE KEY uk_team_student (team_id, student_id),
    INDEX idx_application_status (status)
);

-- 12. 图片表 (images)
CREATE TABLE IF NOT EXISTS images (
    image_id INT AUTO_INCREMENT PRIMARY KEY,
    file_path VARCHAR(255) NOT NULL,
    file_name VARCHAR(100) NOT NULL,
    file_type VARCHAR(50) NOT NULL,
    type VARCHAR(20) NOT NULL COMMENT 'avatar or other',
    user_id INT,
    upload_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES users(user_id) ON DELETE SET NULL,
    INDEX idx_user_id (user_id),
    INDEX idx_type (type)
);

-- 13. 报表表 (reports)
CREATE TABLE IF NOT EXISTS reports (
    report_id INT AUTO_INCREMENT PRIMARY KEY,
    report_name VARCHAR(100) NOT NULL,
    report_type VARCHAR(50) NOT NULL,
    content TEXT,
    generated_by INT NOT NULL,
    generated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    file_path VARCHAR(255),
    FOREIGN KEY (generated_by) REFERENCES users(user_id) ON DELETE CASCADE,
    INDEX idx_report_type (report_type)
);

-- 14. 验证码表 (verification_codes)
CREATE TABLE IF NOT EXISTS verification_codes (
    code_id INT AUTO_INCREMENT PRIMARY KEY,
    user_id INT,
    code VARCHAR(10) NOT NULL,
    type ENUM('login', 'register', 'reset_password') NOT NULL,
    expire_time TIMESTAMP NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES users(user_id) ON DELETE CASCADE,
    INDEX idx_code_expire (code, expire_time)
);

-- 15. 密码找回令牌表 (password_reset_tokens)
CREATE TABLE IF NOT EXISTS password_reset_tokens (
    token_id INT AUTO_INCREMENT PRIMARY KEY,
    user_id INT NOT NULL,
    token VARCHAR(255) NOT NULL,
    expire_time TIMESTAMP NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES users(user_id) ON DELETE CASCADE,
    UNIQUE KEY uk_token (token),
    INDEX idx_token_expire (token, expire_time)
);

-- 16. 教室表 (classrooms)
CREATE TABLE IF NOT EXISTS classrooms (
    classroom_id INT AUTO_INCREMENT PRIMARY KEY COMMENT '教室ID',
    classroom_name VARCHAR(100) NOT NULL UNIQUE COMMENT '教室名称',
    capacity INT NOT NULL COMMENT '教室容量',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    INDEX idx_classroom_name (classroom_name),
    INDEX idx_capacity (capacity)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='教室表';

-- 17. AI会话表 (ai_sessions)
CREATE TABLE IF NOT EXISTS ai_sessions (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '数据id',
    session_id VARCHAR(32) NOT NULL COMMENT '会话id',
    user_id INT NOT NULL COMMENT '用户id',
    title VARCHAR(100) COMMENT '会话标题',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    expire_at DATETIME NULL COMMENT '会话过期时间（滑动续期，驱动 Redis TTL）',
    status VARCHAR(16) NOT NULL DEFAULT 'active' COMMENT '会话状态：active/closed/expired',
    creator BIGINT COMMENT '创建人',
    updater BIGINT COMMENT '更新人',
    FOREIGN KEY (user_id) REFERENCES users(user_id) ON DELETE CASCADE,
    INDEX idx_session_id (session_id),
    INDEX idx_user_id (user_id),
    INDEX idx_expire_at (expire_at),
    INDEX idx_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='AI会话表';

-- 18. AI对话消息表 (ai_messages)
CREATE TABLE IF NOT EXISTS `ai_messages` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键',
    `session_id` VARCHAR(64) NOT NULL COMMENT '会话ID，关联 ai_sessions.session_id',
    `role` VARCHAR(16) NOT NULL COMMENT '角色：user / assistant',
    `content` TEXT NOT NULL COMMENT '消息内容',
    `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    PRIMARY KEY (`id`),
    INDEX `idx_session_id` (`session_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='AI对话消息表';

-- 19. AI会话滚动摘要表 (ai_session_summaries)
-- 每个 session 仅保留最新一条滚动摘要；满 batch 条消息后异步合并更新
CREATE TABLE IF NOT EXISTS ai_session_summaries (
    id              BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '主键',
    session_id      VARCHAR(64)  NOT NULL COMMENT '会话ID，关联 ai_sessions.session_id',
    summary_content TEXT         NOT NULL COMMENT '滚动摘要正文',
    covered_count   INT          NOT NULL DEFAULT 0 COMMENT '已纳入摘要的消息条数(user+assistant合计)',
    version         INT          NOT NULL DEFAULT 1 COMMENT '摘要版本号，每次滚动+1',
    create_time     DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time     DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    UNIQUE KEY uk_session_id (session_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='AI会话滚动摘要';