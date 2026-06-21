-- ============================================================
-- AI 对话工具测试数据
-- 测试场景：学生通过 AI 获得课程推荐、科研项目推荐
-- ============================================================

USE education_system;

-- ===================== 1. 表结构修正（对齐 Java 模型） =====================

-- research_projects 增加 tags 字段
-- ALTER TABLE research_projects ADD COLUMN IF NOT EXISTS tags VARCHAR(500) COMMENT '标签，逗号分隔';

-- research_projects 状态增加 recruiting 值
-- ALTER TABLE research_projects MODIFY COLUMN status ENUM('open', 'closed', 'recruiting') DEFAULT 'recruiting';
-- ALTER TABLE teachers ADD COLUMN user_id INT AFTER teacher_id;
-- UPDATE teachers SET user_id = teacher_id;

-- ===================== 2. 用户基础数据 =====================

-- 学生用户（user_id 1-3）
INSERT INTO users (user_id, username, password, email, phone, role) VALUES
(1, 'zhangsan',  '$2a$10$6xA1dfAKHA91bqGF8rsPbOoru7f28XyNTnw7Yz0d3qWnR.P7UtrvK', 'zhangsan@edu.cn',  '13800001001', 'student'),
(2, 'lisi',     '$2a$10$6xA1dfAKHA91bqGF8rsPbOoru7f28XyNTnw7Yz0d3qWnR.P7UtrvK', 'lisi@edu.cn',      '13800001002', 'student'),
(3, 'wangwu',   '$2a$10$6xA1dfAKHA91bqGF8rsPbOoru7f28XyNTnw7Yz0d3qWnR.P7UtrvK', 'wangwu@edu.cn',    '13800001003', 'student')
ON DUPLICATE KEY UPDATE username=VALUES(username);

-- 教师用户（user_id 10-12）


INSERT INTO users (user_id, username, password, email, phone, role) VALUES
(10, 'teacher_zhang', '$2a$10$6xA1dfAKHA91bqGF8rsPbOoru7f28XyNTnw7Yz0d3qWnR.P7UtrvK', 'tzhang@edu.cn',  '13800002001', 'teacher'),
(11, 'teacher_li',    '$2a$10$6xA1dfAKHA91bqGF8rsPbOoru7f28XyNTnw7Yz0d3qWnR.P7UtrvK', 'tli@edu.cn',     '13800002002', 'teacher'),
(12, 'teacher_wang',  '$2a$10$6xA1dfAKHA91bqGF8rsPbOoru7f28XyNTnw7Yz0d3qWnR.P7UtrvK', 'twang@edu.cn',   '13800002003', 'teacher')
ON DUPLICATE KEY UPDATE username=VALUES(username);

-- 管理员（user_id 99）
-- INSERT INTO users (user_id, username, password, email, phone, role) VALUES
-- (99, 'admin', '{noop}admin123', 'admin@edu.cn', '13800000001', 'admin')
-- ON DUPLICATE KEY UPDATE username=VALUES(username);

-- ===================== 3. 学生信息 =====================

INSERT INTO students (student_id, user_id, name, student_number, major, grade, class, department, gender) VALUES
(1, 1, '张三', '2024001', '计算机科学与技术', '2024', '计科2401', '信息学院', '男'),
(2, 2, '李四', '2024002', '软件工程',         '2024', '软工2401', '信息学院', '女'),
(3, 3, '王五', '2023001', '数据科学与大数据', '2023', '大数据2301', '信息学院', '男')
ON DUPLICATE KEY UPDATE user_id=VALUES(user_id), name=VALUES(name);

-- ===================== 4. 教师信息 =====================

INSERT INTO teachers (teacher_id, user_id, name, title, department, bio) VALUES
(10, 10, '张教授', '教授',     '信息学院', '研究方向：人工智能、机器学习、自然语言处理'),
(11, 11, '李副教授', '副教授', '信息学院', '研究方向：软件工程、分布式系统、云计算'),
(12, 12, '王讲师', '讲师',     '信息学院', '研究方向：数据结构、算法设计、网络安全')
ON DUPLICATE KEY UPDATE name=VALUES(name);

-- ===================== 5. 课程数据 =====================

INSERT INTO courses (course_id, course_name, credit, course_code, description) VALUES
(1,  '数据结构',         4.0, 'CS101', '线性表、栈、队列、树、图等基本数据结构的原理与实现'),
(2,  'Java程序设计',     3.5, 'CS102', 'Java语言基础、面向对象编程、集合框架、IO流、多线程'),
(3,  '数据库原理',       3.5, 'CS103', '关系数据库理论、SQL语言、数据库设计与范式'),
(4,  '操作系统',         4.0, 'CS104', '进程管理、内存管理、文件系统、设备管理的原理与实现'),
(5,  '计算机网络',       3.5, 'CS105', 'TCP/IP协议栈、网络层、传输层、应用层协议'),
(6,  '人工智能导论',     3.0, 'CS201', '搜索算法、知识表示、机器学习基础、神经网络入门'),
(7,  '机器学习',         3.5, 'CS202', '监督学习、无监督学习、深度学习、模型评估与调优'),
(8,  '软件工程',         3.0, 'CS203', '软件生命周期、需求分析、系统设计、测试与项目管理'),
(9,  'Python数据分析',   3.0, 'CS204', 'NumPy、Pandas、Matplotlib、数据清洗与可视化'),
(10, 'Web前端开发',      3.0, 'CS205', 'HTML/CSS/JavaScript、Vue.js、响应式布局'),
(11, '网络安全基础',     3.0, 'CS206', '密码学基础、网络攻防、安全协议、漏洞分析'),
(12, '编译原理',         3.5, 'CS301', '词法分析、语法分析、语义分析、代码生成与优化')
ON DUPLICATE KEY UPDATE course_name=VALUES(course_name);

-- ===================== 6. 排课数据 =====================

INSERT INTO course_schedules (schedule_id, course_id, teacher_id, classroom, day_of_week, start_time, end_time, semester, year) VALUES
(1,  1,  12, 'A101', 1, '08:00', '09:40', '2025-2026-2', 2026),
(2,  2,  11, 'A102', 2, '08:00', '09:40', '2025-2026-2', 2026),
(3,  3,  10, 'A201', 1, '10:00', '11:40', '2025-2026-2', 2026),
(4,  4,  12, 'A202', 3, '08:00', '09:40', '2025-2026-2', 2026),
(5,  5,  11, 'B101', 2, '14:00', '15:40', '2025-2026-2', 2026),
(6,  6,  10, 'B102', 4, '10:00', '11:40', '2025-2026-2', 2026),
(7,  7,  10, 'B201', 5, '08:00', '09:40', '2025-2026-2', 2026),
(8,  8,  11, 'A301', 3, '14:00', '15:40', '2025-2026-2', 2026),
(9,  9,  10, 'B202', 4, '14:00', '15:40', '2025-2026-2', 2026),
(10, 10, 12, 'C101', 1, '14:00', '15:40', '2025-2026-2', 2026),
(11, 11, 12, 'C102', 5, '14:00', '15:40', '2025-2026-2', 2026),
(12, 12, 11, 'A302', 4, '08:00', '09:40', '2025-2026-2', 2026)
ON DUPLICATE KEY UPDATE course_id=VALUES(course_id);

-- ===================== 7. 选课数据 =====================

-- 张三（计科）：选了 数据结构、数据库原理、操作系统、Python数据分析
INSERT INTO course_enrollments (enrollment_id, student_id, schedule_id, status) VALUES
(1,  1, 1,  'enrolled'),
(2,  1, 3,  'enrolled'),
(3,  1, 4,  'enrolled'),
(4,  1, 9,  'enrolled')
ON DUPLICATE KEY UPDATE student_id=VALUES(student_id);

-- 李四（软工）：选了 Java程序设计、计算机网络、软件工程、Web前端开发
INSERT INTO course_enrollments (enrollment_id, student_id, schedule_id, status) VALUES
(5,  2, 2,  'enrolled'),
(6,  2, 5,  'enrolled'),
(7,  2, 8,  'enrolled'),
(8,  2, 10, 'enrolled')
ON DUPLICATE KEY UPDATE student_id=VALUES(student_id);

-- 王五（大数据）：选了 人工智能导论、机器学习、Python数据分析、数据库原理
INSERT INTO course_enrollments (enrollment_id, student_id, schedule_id, status) VALUES
(9,  3, 6,  'enrolled'),
(10, 3, 7,  'enrolled'),
(11, 3, 9,  'enrolled'),
(12, 3, 3,  'enrolled')
ON DUPLICATE KEY UPDATE student_id=VALUES(student_id);

-- ===================== 8. 科研项目数据 =====================

INSERT INTO research_projects (project_id, project_name, description, teacher_id, status, tags, start_date, end_date) VALUES
(1, '基于深度学习的图像识别研究',
    '利用CNN和Transformer架构实现高精度图像分类与目标检测', 10, 'recruiting',
    'Python数据分析,机器学习,人工智能导论', '2026-03-01', '2026-12-31'),

(2, '大规模数据库性能优化',
    '研究MySQL和Redis在海量数据下的索引优化与查询加速策略', 11, 'recruiting',
    '数据库原理,数据结构,操作系统', '2026-04-01', '2027-03-31'),

(3, '云原生微服务架构实践',
    '基于Spring Boot + Docker + Kubernetes的微服务部署与治理', 11, 'recruiting',
    'Java程序设计,软件工程,Web前端开发', '2026-03-15', '2026-09-15'),

(4, 'Web安全漏洞自动检测系统',
    '开发自动化工具检测SQL注入、XSS、CSRF等常见Web安全漏洞', 12, 'recruiting',
    '网络安全基础,Web前端开发,Java程序设计', '2026-05-01', '2026-11-30'),

(5, '自然语言处理课程智能问答',
    '基于大语言模型的课程内容问答与知识点推理系统', 10, 'recruiting',
    '人工智能导论,机器学习,Python数据分析,数据结构', '2026-06-01', '2027-06-01'),

(6, '分布式编译优化工具链',
    '设计并实现基于LLVM的分布式编译优化工具，提升大规模C++项目编译速度', 12, 'closed',
    '编译原理,数据结构,操作系统', '2025-09-01', '2026-02-28')
ON DUPLICATE KEY UPDATE project_name=VALUES(project_name);

-- ===================== 测试用例说明 =====================
--
-- 【用例1】学生查课程 — "帮我看看我选了哪些课"
--   预期：AI 调用 getStudentEnrollments(1) → 返回张三的4门课
--
-- 【用例2】课程推荐 — "帮我推荐适合我的课程"
--   预期：AI 调用 getStudentEnrollments(1) → 了解已选课
--        → 调用 findCourses(null) 获取全部课程
--        → 对比已选和未选，推荐 CS102(Java)、CS105(计算机网络) 等
--
-- 【用例3】查询学生信息 — "查一下学号2023001的学生信息"
--   预期：AI 调用 findStudentById(3, null, null, null) → 返回王五的信息
--
-- 【用例4】科研项目推荐（张三）— "有没有适合我的科研项目"
--   预期：AI 调用 recommendProjects(1)
--        → 张三已选[数据结构,数据库原理,操作系统,Python数据分析]
--        → 匹配项目标签，推荐项目2(数据库), 项目5(NLP问答), 项目1(图像识别)
--
-- 【用例5】科研项目推荐（李四）— "推荐科研项目"
--   预期：AI 调用 recommendProjects(2)
--        → 李四已选[Java程序设计,计算机网络,软件工程,Web前端开发]
--        → 匹配项目标签，推荐项目3(微服务), 项目4(Web安全)
--
-- 【用例6】查看科研项目列表 — "有哪些招募中的科研项目"
--   预期：AI 调用 findResearchProjects(null) → 返回5个recruiting项目
--        → AI 用自然语言列出项目名称和方向
--
-- 【用例7】按方向搜科研项目 — "有没有人工智能方向的科研项目"
--   预期：AI 调用 findResearchProjects("人工智能")
--        → 返回项目1(图像识别)和项目5(NLP问答)
