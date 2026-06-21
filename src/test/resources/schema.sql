-- H2 测试用表结构（与 MySQL 表结构一致）
DROP TABLE IF EXISTS grades;

CREATE TABLE grades (
    grade_id       INT AUTO_INCREMENT PRIMARY KEY,
    enrollment_id  INT          NOT NULL,
    score          DECIMAL(5,2) NOT NULL,
    grade_level    VARCHAR(20),
    teacher_id     INT,
    comment        VARCHAR(500),
    created_at     TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);
