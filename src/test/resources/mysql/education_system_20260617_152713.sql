-- MySQL dump 10.13  Distrib 8.0.46, for Win64 (x86_64)
--
-- Host: localhost    Database: education_system
-- ------------------------------------------------------
-- Server version	8.0.46

/*!40101 SET @OLD_CHARACTER_SET_CLIENT=@@CHARACTER_SET_CLIENT */;
/*!40101 SET @OLD_CHARACTER_SET_RESULTS=@@CHARACTER_SET_RESULTS */;
/*!40101 SET @OLD_COLLATION_CONNECTION=@@COLLATION_CONNECTION */;
/*!50503 SET NAMES utf8 */;
/*!40103 SET @OLD_TIME_ZONE=@@TIME_ZONE */;
/*!40103 SET TIME_ZONE='+00:00' */;
/*!40014 SET @OLD_UNIQUE_CHECKS=@@UNIQUE_CHECKS, UNIQUE_CHECKS=0 */;
/*!40014 SET @OLD_FOREIGN_KEY_CHECKS=@@FOREIGN_KEY_CHECKS, FOREIGN_KEY_CHECKS=0 */;
/*!40101 SET @OLD_SQL_MODE=@@SQL_MODE, SQL_MODE='NO_AUTO_VALUE_ON_ZERO' */;
/*!40111 SET @OLD_SQL_NOTES=@@SQL_NOTES, SQL_NOTES=0 */;

--
-- Table structure for table `ai_messages`
--

DROP TABLE IF EXISTS `ai_messages`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `ai_messages` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键',
  `session_id` varchar(64) NOT NULL COMMENT '会话ID，关联 ai_sessions.session_id',
  `role` varchar(16) NOT NULL COMMENT '角色：user / assistant',
  `content` text NOT NULL COMMENT '消息内容',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`id`),
  KEY `idx_session_id` (`session_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='AI对话消息表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `ai_messages`
--

LOCK TABLES `ai_messages` WRITE;
/*!40000 ALTER TABLE `ai_messages` DISABLE KEYS */;
/*!40000 ALTER TABLE `ai_messages` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `ai_sessions`
--

DROP TABLE IF EXISTS `ai_sessions`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `ai_sessions` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '数据id',
  `session_id` varchar(32) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '会话id',
  `user_id` int NOT NULL COMMENT '用户id',
  `title` varchar(100) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '会话标题',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `creator` bigint DEFAULT NULL COMMENT '创建人',
  `updater` bigint DEFAULT NULL COMMENT '更新人',
  PRIMARY KEY (`id`),
  KEY `idx_session_id` (`session_id`),
  KEY `idx_user_id` (`user_id`),
  CONSTRAINT `ai_sessions_ibfk_1` FOREIGN KEY (`user_id`) REFERENCES `users` (`user_id`) ON DELETE CASCADE
) ENGINE=InnoDB AUTO_INCREMENT=2 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='AI会话表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `ai_sessions`
--

LOCK TABLES `ai_sessions` WRITE;
/*!40000 ALTER TABLE `ai_sessions` DISABLE KEYS */;
INSERT INTO `ai_sessions` VALUES (1,'e81bade3e2a84976b536b7b0f317799e',38,NULL,'2026-06-16 20:44:24','2026-06-16 20:44:23',NULL,NULL);
/*!40000 ALTER TABLE `ai_sessions` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `classrooms`
--

DROP TABLE IF EXISTS `classrooms`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `classrooms` (
  `classroom_id` int NOT NULL AUTO_INCREMENT COMMENT '教室ID',
  `classroom_name` varchar(100) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '教室名称',
  `capacity` int NOT NULL COMMENT '教室容量',
  `created_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`classroom_id`),
  UNIQUE KEY `classroom_name` (`classroom_name`),
  KEY `idx_classroom_name` (`classroom_name`),
  KEY `idx_capacity` (`capacity`)
) ENGINE=InnoDB AUTO_INCREMENT=11 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='教室表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `classrooms`
--

LOCK TABLES `classrooms` WRITE;
/*!40000 ALTER TABLE `classrooms` DISABLE KEYS */;
INSERT INTO `classrooms` VALUES (1,'教一楼 101',30,'2026-05-26 08:44:53','2026-05-26 08:44:53'),(2,'教一楼 102',50,'2026-05-26 08:44:53','2026-05-26 08:44:53'),(3,'教一楼 103',30,'2026-05-26 08:44:53','2026-05-26 08:44:53'),(4,'教一楼 201',60,'2026-05-26 08:44:53','2026-05-26 08:44:53'),(5,'教一楼 202',60,'2026-05-26 08:44:53','2026-05-26 08:44:53'),(6,'教二楼 301',100,'2026-05-26 08:44:53','2026-05-26 08:44:53'),(7,'教二楼 302',100,'2026-05-26 08:44:53','2026-05-26 08:44:53'),(8,'实验楼 A101',40,'2026-05-26 08:44:53','2026-05-26 08:44:53'),(9,'实验楼 A102',40,'2026-05-26 08:44:53','2026-05-26 08:44:53'),(10,'实验楼 B201',80,'2026-05-26 08:44:53','2026-05-26 08:44:53');
/*!40000 ALTER TABLE `classrooms` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `course_enrollments`
--

DROP TABLE IF EXISTS `course_enrollments`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `course_enrollments` (
  `enrollment_id` int NOT NULL AUTO_INCREMENT,
  `student_id` int NOT NULL,
  `schedule_id` int NOT NULL,
  `enroll_time` timestamp NULL DEFAULT CURRENT_TIMESTAMP,
  `status` enum('enrolled','dropped') COLLATE utf8mb4_unicode_ci DEFAULT 'enrolled',
  PRIMARY KEY (`enrollment_id`),
  UNIQUE KEY `uk_student_schedule` (`student_id`,`schedule_id`),
  KEY `schedule_id` (`schedule_id`),
  CONSTRAINT `course_enrollments_ibfk_2` FOREIGN KEY (`schedule_id`) REFERENCES `course_schedules` (`schedule_id`) ON DELETE CASCADE,
  CONSTRAINT `course_enrollments_student_fk` FOREIGN KEY (`student_id`) REFERENCES `users` (`user_id`) ON DELETE CASCADE
) ENGINE=InnoDB AUTO_INCREMENT=78 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `course_enrollments`
--

LOCK TABLES `course_enrollments` WRITE;
/*!40000 ALTER TABLE `course_enrollments` DISABLE KEYS */;
INSERT INTO `course_enrollments` VALUES (25,38,14,'2026-05-28 14:26:56','enrolled'),(26,38,13,'2026-05-29 01:42:51','enrolled'),(30,38,17,'2026-05-29 02:50:36','enrolled'),(32,38,16,'2026-05-29 02:56:09','dropped'),(33,41,19,'2026-06-01 08:20:51','enrolled'),(34,41,17,'2026-06-04 07:27:52','enrolled'),(37,4,16,'2026-06-12 05:03:32','enrolled'),(38,5,16,'2026-06-12 05:03:32','enrolled'),(39,6,16,'2026-06-12 05:03:32','enrolled'),(40,7,16,'2026-06-12 05:03:32','enrolled'),(41,9,16,'2026-06-12 05:03:32','enrolled'),(42,10,16,'2026-06-12 05:03:32','enrolled'),(43,11,16,'2026-06-12 05:03:32','enrolled'),(44,12,16,'2026-06-12 05:03:32','enrolled'),(45,13,16,'2026-06-12 05:03:32','enrolled'),(46,34,16,'2026-06-12 05:03:32','enrolled'),(47,35,16,'2026-06-12 05:03:32','enrolled'),(48,37,16,'2026-06-12 05:03:32','enrolled'),(49,42,16,'2026-06-12 05:03:32','enrolled'),(50,4,17,'2026-06-12 05:03:36','enrolled'),(51,5,17,'2026-06-12 05:03:36','enrolled'),(52,6,17,'2026-06-12 05:03:36','enrolled'),(53,7,17,'2026-06-12 05:03:36','enrolled'),(54,9,17,'2026-06-12 05:03:36','enrolled'),(55,10,17,'2026-06-12 05:03:36','enrolled'),(56,11,17,'2026-06-12 05:03:36','enrolled'),(57,12,17,'2026-06-12 05:03:36','enrolled'),(58,13,17,'2026-06-12 05:03:36','enrolled'),(59,34,17,'2026-06-12 05:03:36','enrolled'),(60,35,17,'2026-06-12 05:03:36','enrolled'),(61,37,17,'2026-06-12 05:03:36','enrolled'),(62,42,17,'2026-06-12 05:03:36','enrolled'),(63,4,19,'2026-06-12 05:03:40','enrolled'),(64,5,19,'2026-06-12 05:03:40','enrolled'),(65,6,19,'2026-06-12 05:03:40','enrolled'),(66,7,19,'2026-06-12 05:03:40','enrolled'),(67,9,19,'2026-06-12 05:03:40','enrolled'),(68,10,19,'2026-06-12 05:03:40','enrolled'),(69,11,19,'2026-06-12 05:03:40','enrolled'),(70,12,19,'2026-06-12 05:03:40','enrolled'),(71,13,19,'2026-06-12 05:03:40','enrolled'),(72,34,19,'2026-06-12 05:03:40','enrolled'),(73,35,19,'2026-06-12 05:03:40','enrolled'),(74,37,19,'2026-06-12 05:03:40','enrolled'),(75,42,19,'2026-06-12 05:03:40','enrolled');
/*!40000 ALTER TABLE `course_enrollments` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `course_schedules`
--

DROP TABLE IF EXISTS `course_schedules`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `course_schedules` (
  `schedule_id` int NOT NULL AUTO_INCREMENT,
  `course_id` int NOT NULL,
  `teacher_id` int NOT NULL,
  `classroom` varchar(50) COLLATE utf8mb4_unicode_ci NOT NULL,
  `day_of_week` int NOT NULL,
  `start_time` time NOT NULL,
  `end_time` time NOT NULL,
  `semester` varchar(20) COLLATE utf8mb4_unicode_ci NOT NULL,
  `year` int NOT NULL,
  PRIMARY KEY (`schedule_id`),
  KEY `course_id` (`course_id`),
  KEY `idx_teacher_semester` (`teacher_id`,`semester`,`year`),
  KEY `idx_classroom_time` (`classroom`,`day_of_week`,`start_time`),
  CONSTRAINT `course_schedules_ibfk_1` FOREIGN KEY (`course_id`) REFERENCES `courses` (`course_id`) ON DELETE CASCADE,
  CONSTRAINT `course_schedules_ibfk_2` FOREIGN KEY (`teacher_id`) REFERENCES `teachers` (`teacher_id`) ON DELETE CASCADE
) ENGINE=InnoDB AUTO_INCREMENT=23 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `course_schedules`
--

LOCK TABLES `course_schedules` WRITE;
/*!40000 ALTER TABLE `course_schedules` DISABLE KEYS */;
INSERT INTO `course_schedules` VALUES (9,6,33,'教一楼 101',1,'14:00:00','15:00:00','1',2023),(12,1,15,'教一楼 101',2,'16:00:00','17:00:00','1',2023),(13,3,15,'教一楼 101',6,'10:00:00','11:00:00','1',2023),(14,4,15,'教一楼 101',2,'08:00:00','09:00:00','1',2023),(16,10,40,'教一楼 101',3,'10:00:00','11:00:00','1',2023),(17,10,40,'教一楼 103',1,'09:00:00','10:00:00','2',2023),(19,5,40,'教一楼 103',2,'09:00:00','10:00:00','1',2023);
/*!40000 ALTER TABLE `course_schedules` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `courses`
--

DROP TABLE IF EXISTS `courses`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `courses` (
  `course_id` int NOT NULL AUTO_INCREMENT,
  `course_name` varchar(100) COLLATE utf8mb4_unicode_ci NOT NULL,
  `credit` decimal(3,1) NOT NULL,
  `course_code` varchar(20) COLLATE utf8mb4_unicode_ci NOT NULL,
  `description` text COLLATE utf8mb4_unicode_ci,
  PRIMARY KEY (`course_id`),
  UNIQUE KEY `course_code` (`course_code`),
  KEY `idx_course_code` (`course_code`)
) ENGINE=InnoDB AUTO_INCREMENT=13 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `courses`
--

LOCK TABLES `courses` WRITE;
/*!40000 ALTER TABLE `courses` DISABLE KEYS */;
INSERT INTO `courses` VALUES (1,'数据结构与算法',4.0,'CS201','计算机专业核心课程，讲解常见数据结构和算法设计'),(3,'操作系统',4.0,'CS301','操作系统原理与实践'),(4,'计算机网络',3.0,'CS302','计算机网络原理'),(5,'数据库原理',3.0,'CS303','数据库系统概论'),(6,'软件工程',3.0,'SE201','软件工程导论'),(7,'高等数学',5.0,'MATH101','高等数学（上）'),(10,'线性代数',5.0,'MATH102','无'),(11,'测试',5.0,'CS2','无'),(12,'概率论',5.0,'MATH103','无');
/*!40000 ALTER TABLE `courses` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `grades`
--

DROP TABLE IF EXISTS `grades`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `grades` (
  `grade_id` int NOT NULL AUTO_INCREMENT,
  `enrollment_id` int NOT NULL,
  `score` decimal(5,2) NOT NULL,
  `grade_level` enum('excellent','good','average','pass','fail') COLLATE utf8mb4_unicode_ci NOT NULL,
  `teacher_id` int NOT NULL,
  `comment` text COLLATE utf8mb4_unicode_ci,
  `created_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`grade_id`),
  KEY `teacher_id` (`teacher_id`),
  KEY `idx_student_score` (`enrollment_id`,`score`),
  KEY `idx_grade_level` (`grade_level`),
  CONSTRAINT `grades_ibfk_1` FOREIGN KEY (`enrollment_id`) REFERENCES `course_enrollments` (`enrollment_id`) ON DELETE CASCADE,
  CONSTRAINT `grades_ibfk_2` FOREIGN KEY (`teacher_id`) REFERENCES `teachers` (`teacher_id`) ON DELETE CASCADE
) ENGINE=InnoDB AUTO_INCREMENT=43 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `grades`
--

LOCK TABLES `grades` WRITE;
/*!40000 ALTER TABLE `grades` DISABLE KEYS */;
INSERT INTO `grades` VALUES (25,30,90.00,'excellent',40,'好','2026-05-29 04:21:31'),(27,33,60.00,'pass',40,'无','2026-06-01 08:22:27'),(28,32,100.00,'excellent',40,'','2026-06-04 07:31:38'),(29,50,85.00,'good',40,'好','2026-06-13 12:48:11'),(30,51,52.00,'fail',40,'好','2026-06-13 12:48:11'),(31,52,98.00,'excellent',40,'好','2026-06-13 12:48:11'),(32,53,56.00,'fail',40,'好','2026-06-13 12:48:11'),(33,54,62.00,'pass',40,'好','2026-06-13 12:48:11'),(34,55,98.00,'excellent',40,'好','2026-06-13 12:48:11'),(35,56,12.00,'fail',40,'好','2026-06-13 12:48:11'),(36,57,89.00,'good',40,'好','2026-06-13 12:48:11'),(37,58,56.00,'fail',40,'好','2026-06-13 12:48:11'),(38,59,94.00,'excellent',40,'好','2026-06-13 12:48:11'),(39,60,56.00,'fail',40,'好','2026-06-13 12:48:12'),(40,61,51.00,'fail',40,'好','2026-06-13 12:48:12'),(41,62,78.00,'average',40,'好','2026-06-13 12:48:12'),(42,34,78.00,'average',40,'好','2026-06-13 12:48:12');
/*!40000 ALTER TABLE `grades` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `images`
--

DROP TABLE IF EXISTS `images`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `images` (
  `image_id` int NOT NULL AUTO_INCREMENT,
  `file_path` varchar(255) COLLATE utf8mb4_unicode_ci NOT NULL,
  `file_name` varchar(100) COLLATE utf8mb4_unicode_ci NOT NULL,
  `file_type` varchar(50) COLLATE utf8mb4_unicode_ci NOT NULL,
  `type` varchar(20) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT 'avatar or other',
  `user_id` int DEFAULT NULL,
  `upload_time` timestamp NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`image_id`),
  KEY `idx_user_id` (`user_id`),
  KEY `idx_type` (`type`),
  CONSTRAINT `images_ibfk_1` FOREIGN KEY (`user_id`) REFERENCES `users` (`user_id`) ON DELETE SET NULL
) ENGINE=InnoDB AUTO_INCREMENT=7 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `images`
--

LOCK TABLES `images` WRITE;
/*!40000 ALTER TABLE `images` DISABLE KEYS */;
INSERT INTO `images` VALUES (1,'2026/05/28/3ebe85df-90de-4480-a357-1e357ba8aaaa.jpg','签名.jpg','image/jpeg','avatar',37,'2026-05-28 03:45:55'),(2,'2026/05/29/41c83827-4150-4920-a3fc-71a378c06daf.jpg','da739d5f385e3fe4e81265c29d5de01.jpg','image/jpeg','avatar',38,'2026-05-29 05:54:52'),(3,'2026/05/29/4d6b44be-aad6-4f23-b147-afbf17f27792.jpg','da739d5f385e3fe4e81265c29d5de01.jpg','image/jpeg','avatar',38,'2026-05-29 05:56:37'),(4,'2026/05/29/8065e234-e2e9-4ed6-9cf0-00a2dbc956d7.jpg','da739d5f385e3fe4e81265c29d5de01.jpg','image/jpeg','avatar',38,'2026-05-29 05:58:02'),(5,'2026/05/29/c197299b-fc53-460e-90d1-3a6693f958a9.jpg','da739d5f385e3fe4e81265c29d5de01.jpg','image/jpeg','avatar',40,'2026-05-29 06:24:17'),(6,'2026/06/04/21ef8083-abc5-48dd-9978-95b2aaba040e.jpg','8640045db776617d78c95091fb1c9e89.jpg','image/jpeg','avatar',41,'2026-06-04 07:27:26');
/*!40000 ALTER TABLE `images` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `innovation_teams`
--

DROP TABLE IF EXISTS `innovation_teams`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `innovation_teams` (
  `team_id` int NOT NULL AUTO_INCREMENT,
  `team_name` varchar(100) COLLATE utf8mb4_unicode_ci NOT NULL,
  `project_name` varchar(200) COLLATE utf8mb4_unicode_ci NOT NULL,
  `leader_id` int NOT NULL,
  `status` enum('recruiting','closed') COLLATE utf8mb4_unicode_ci DEFAULT 'recruiting',
  `description` text COLLATE utf8mb4_unicode_ci,
  `created_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`team_id`),
  KEY `idx_team_status` (`status`),
  KEY `innovation_teams_leader_fk` (`leader_id`),
  CONSTRAINT `innovation_teams_leader_fk` FOREIGN KEY (`leader_id`) REFERENCES `users` (`user_id`) ON DELETE CASCADE
) ENGINE=InnoDB AUTO_INCREMENT=3 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `innovation_teams`
--

LOCK TABLES `innovation_teams` WRITE;
/*!40000 ALTER TABLE `innovation_teams` DISABLE KEYS */;
INSERT INTO `innovation_teams` VALUES (1,'测试一','大创赛',38,'recruiting','测试，欢迎新成员来参加呀……~……','2026-05-31 04:54:32'),(2,'测试组队','大创赛',38,'recruiting','欢迎加入,欢迎加入欢迎加入','2026-06-12 03:52:34');
/*!40000 ALTER TABLE `innovation_teams` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `password_reset_tokens`
--

DROP TABLE IF EXISTS `password_reset_tokens`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `password_reset_tokens` (
  `token_id` int NOT NULL AUTO_INCREMENT,
  `user_id` int NOT NULL,
  `token` varchar(255) COLLATE utf8mb4_unicode_ci NOT NULL,
  `expire_time` timestamp NOT NULL,
  `created_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`token_id`),
  UNIQUE KEY `uk_token` (`token`),
  KEY `user_id` (`user_id`),
  KEY `idx_token_expire` (`token`,`expire_time`),
  CONSTRAINT `password_reset_tokens_ibfk_1` FOREIGN KEY (`user_id`) REFERENCES `users` (`user_id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `password_reset_tokens`
--

LOCK TABLES `password_reset_tokens` WRITE;
/*!40000 ALTER TABLE `password_reset_tokens` DISABLE KEYS */;
/*!40000 ALTER TABLE `password_reset_tokens` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `project_applications`
--

DROP TABLE IF EXISTS `project_applications`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `project_applications` (
  `application_id` int NOT NULL AUTO_INCREMENT,
  `project_id` int NOT NULL,
  `student_id` int NOT NULL,
  `application_letter` text COLLATE utf8mb4_unicode_ci,
  `status` enum('pending','approved','rejected') COLLATE utf8mb4_unicode_ci DEFAULT 'pending',
  `apply_time` timestamp NULL DEFAULT CURRENT_TIMESTAMP,
  `review_time` timestamp NULL DEFAULT NULL,
  PRIMARY KEY (`application_id`),
  UNIQUE KEY `uk_project_student` (`project_id`,`student_id`),
  KEY `idx_application_status` (`status`),
  KEY `project_applications_student_fk` (`student_id`),
  CONSTRAINT `project_applications_ibfk_1` FOREIGN KEY (`project_id`) REFERENCES `research_projects` (`project_id`) ON DELETE CASCADE,
  CONSTRAINT `project_applications_student_fk` FOREIGN KEY (`student_id`) REFERENCES `users` (`user_id`) ON DELETE CASCADE
) ENGINE=InnoDB AUTO_INCREMENT=3 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `project_applications`
--

LOCK TABLES `project_applications` WRITE;
/*!40000 ALTER TABLE `project_applications` DISABLE KEYS */;
INSERT INTO `project_applications` VALUES (1,1,38,'我对这个研究方向非常感兴趣','rejected','2026-05-31 06:09:22','2026-05-31 06:53:06'),(2,2,38,'我要报名','approved','2026-05-31 06:33:17','2026-05-31 06:53:03');
/*!40000 ALTER TABLE `project_applications` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `reports`
--

DROP TABLE IF EXISTS `reports`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `reports` (
  `report_id` int NOT NULL AUTO_INCREMENT,
  `report_name` varchar(100) COLLATE utf8mb4_unicode_ci NOT NULL,
  `report_type` varchar(50) COLLATE utf8mb4_unicode_ci NOT NULL,
  `content` text COLLATE utf8mb4_unicode_ci,
  `generated_by` int NOT NULL,
  `generated_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP,
  `file_path` varchar(255) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  PRIMARY KEY (`report_id`),
  KEY `generated_by` (`generated_by`),
  KEY `idx_report_type` (`report_type`),
  CONSTRAINT `reports_ibfk_1` FOREIGN KEY (`generated_by`) REFERENCES `users` (`user_id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `reports`
--

LOCK TABLES `reports` WRITE;
/*!40000 ALTER TABLE `reports` DISABLE KEYS */;
/*!40000 ALTER TABLE `reports` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `research_projects`
--

DROP TABLE IF EXISTS `research_projects`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `research_projects` (
  `project_id` int NOT NULL AUTO_INCREMENT,
  `project_name` varchar(200) COLLATE utf8mb4_unicode_ci NOT NULL,
  `description` text COLLATE utf8mb4_unicode_ci,
  `teacher_id` int NOT NULL,
  `status` enum('open','closed','recruiting') COLLATE utf8mb4_unicode_ci DEFAULT 'recruiting',
  `start_date` date DEFAULT NULL,
  `end_date` date DEFAULT NULL,
  `created_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP,
  `tags` varchar(500) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  PRIMARY KEY (`project_id`),
  KEY `teacher_id` (`teacher_id`),
  KEY `idx_project_status` (`status`),
  CONSTRAINT `research_projects_ibfk_1` FOREIGN KEY (`teacher_id`) REFERENCES `teachers` (`teacher_id`) ON DELETE CASCADE
) ENGINE=InnoDB AUTO_INCREMENT=3 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `research_projects`
--

LOCK TABLES `research_projects` WRITE;
/*!40000 ALTER TABLE `research_projects` DISABLE KEYS */;
INSERT INTO `research_projects` VALUES (1,'科研项目一','无',40,'open','2026-05-31','2026-06-07','2026-05-31 05:54:39',NULL),(2,'科研项目二','无',40,'open','2026-05-31','2026-05-31','2026-05-31 06:33:01',NULL);
/*!40000 ALTER TABLE `research_projects` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `students`
--

DROP TABLE IF EXISTS `students`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `students` (
  `student_id` int NOT NULL AUTO_INCREMENT,
  `user_id` int DEFAULT NULL,
  `name` varchar(50) COLLATE utf8mb4_unicode_ci NOT NULL,
  `student_number` varchar(20) COLLATE utf8mb4_unicode_ci NOT NULL,
  `major` varchar(100) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `grade` varchar(20) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `class` varchar(50) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `department` varchar(100) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '院系',
  `gender` varchar(10) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '性别：男/女',
  PRIMARY KEY (`student_id`),
  UNIQUE KEY `student_number` (`student_number`),
  KEY `students_user_fk` (`user_id`),
  CONSTRAINT `students_user_fk` FOREIGN KEY (`user_id`) REFERENCES `users` (`user_id`) ON DELETE CASCADE
) ENGINE=InnoDB AUTO_INCREMENT=47 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `students`
--

LOCK TABLES `students` WRITE;
/*!40000 ALTER TABLE `students` DISABLE KEYS */;
INSERT INTO `students` VALUES (4,4,'王小红','2023150002','计算机科学与技术','2023','一班','计算机学院','女'),(5,5,'赵强','2023150003','计算机科学与技术','2023','二班','计算机学院','男'),(6,6,'陈娜','2023150004','计算机科学与技术','2023','一班','计算机学院','女'),(7,7,'刘洋','2023150005','计算机科学与技术','2023','一班','计算机学院','女'),(9,9,'周杰','2023150007','计算机科学与技术','2023','二班','计算机学院','男'),(10,10,'吴凡','2023150008','计算机科学与技术','2023','二班','计算机学院','男'),(11,11,'郑爽','2023150009','软件工程','2023','一班','计算机学院','女'),(12,12,'钱进','2023150010','软件工程','2023','一班','计算机学院','男'),(13,13,'黄丽','2023150011','软件工程','2023','二班','计算机学院','女'),(34,34,'测试学生A','2023999001','计算机科学与技术','2023','二班','计算机学院','男'),(35,35,'测试学生B','2023999002','计算机科学与技术','2023','二班','计算机学院','男'),(37,37,'测试','2023152221','软件工程','2022','二班',NULL,'女'),(38,38,'测试','202315222155','软件工程','2023','二班',NULL,NULL),(41,41,'大创赛组队','202315222478','软件工程','2023','二班',NULL,'男'),(42,42,'张四','202400000002','计算机科学与技术','2024级','计科2401','计算机学院','男'),(46,NULL,'张三','20231500125','计算机科学与技术','2023','一班','计算机学院','男');
/*!40000 ALTER TABLE `students` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `students_backup_20250614`
--

DROP TABLE IF EXISTS `students_backup_20250614`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `students_backup_20250614` (
  `student_id` int NOT NULL,
  `name` varchar(50) COLLATE utf8mb4_unicode_ci NOT NULL,
  `student_number` varchar(20) COLLATE utf8mb4_unicode_ci NOT NULL,
  `major` varchar(100) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `grade` varchar(20) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `class` varchar(50) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `department` varchar(100) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '院系',
  `gender` varchar(10) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '性别：男/女'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `students_backup_20250614`
--

LOCK TABLES `students_backup_20250614` WRITE;
/*!40000 ALTER TABLE `students_backup_20250614` DISABLE KEYS */;
INSERT INTO `students_backup_20250614` VALUES (4,'王小红','2023150002','计算机科学与技术','2023','一班','计算机学院','女'),(5,'赵强','2023150003','计算机科学与技术','2023','二班','计算机学院','男'),(6,'陈娜','2023150004','计算机科学与技术','2023','一班','计算机学院','女'),(7,'刘洋','2023150005','计算机科学与技术','2023','一班','计算机学院','女'),(9,'周杰','2023150007','计算机科学与技术','2023','二班','计算机学院','男'),(10,'吴凡','2023150008','计算机科学与技术','2023','二班','计算机学院','男'),(11,'郑爽','2023150009','软件工程','2023','一班','计算机学院','女'),(12,'钱进','2023150010','软件工程','2023','一班','计算机学院','男'),(13,'黄丽','2023150011','软件工程','2023','二班','计算机学院','女'),(34,'测试学生A','2023999001','计算机科学与技术','2023','二班','计算机学院','男'),(35,'测试学生B','2023999002','计算机科学与技术','2023','二班','计算机学院','男'),(37,'测试','2023152221','软件工程','2022','二班',NULL,'女'),(38,'测试','202315222155','软件工程','2023','二班',NULL,NULL),(41,'大创赛组队','202315222478','软件工程','2023','二班',NULL,'男'),(42,'张四','202400000002','计算机科学与技术','2024级','计科2401','计算机学院','男'),(44,'张三','20231500125','计算机科学与技术','2023','一班','计算机学院','男');
/*!40000 ALTER TABLE `students_backup_20250614` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `teachers`
--

DROP TABLE IF EXISTS `teachers`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `teachers` (
  `teacher_id` int NOT NULL,
  `user_id` int NOT NULL,
  `name` varchar(50) COLLATE utf8mb4_unicode_ci NOT NULL,
  `title` varchar(50) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `department` varchar(100) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `bio` text COLLATE utf8mb4_unicode_ci,
  PRIMARY KEY (`teacher_id`),
  KEY `user_id` (`user_id`),
  CONSTRAINT `teachers_ibfk_1` FOREIGN KEY (`teacher_id`) REFERENCES `users` (`user_id`) ON DELETE CASCADE,
  CONSTRAINT `teachers_ibfk_2` FOREIGN KEY (`user_id`) REFERENCES `users` (`user_id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `teachers`
--

LOCK TABLES `teachers` WRITE;
/*!40000 ALTER TABLE `teachers` DISABLE KEYS */;
INSERT INTO `teachers` VALUES (15,15,'张老师','讲师','计算机科学与技术',NULL),(33,33,'孙讲师','讲师','计算机科学与技术学院',NULL),(40,40,'老师测试','讲师','计算机与人工智能学院','');
/*!40000 ALTER TABLE `teachers` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `team_applications`
--

DROP TABLE IF EXISTS `team_applications`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `team_applications` (
  `application_id` int NOT NULL AUTO_INCREMENT,
  `team_id` int NOT NULL,
  `student_id` int NOT NULL,
  `application_letter` text COLLATE utf8mb4_unicode_ci,
  `status` enum('pending','approved','rejected') COLLATE utf8mb4_unicode_ci DEFAULT 'pending',
  `apply_time` timestamp NULL DEFAULT CURRENT_TIMESTAMP,
  `review_time` timestamp NULL DEFAULT NULL,
  PRIMARY KEY (`application_id`),
  UNIQUE KEY `uk_team_student` (`team_id`,`student_id`),
  KEY `idx_application_status` (`status`),
  KEY `team_applications_student_fk` (`student_id`),
  CONSTRAINT `team_applications_ibfk_1` FOREIGN KEY (`team_id`) REFERENCES `innovation_teams` (`team_id`) ON DELETE CASCADE,
  CONSTRAINT `team_applications_student_fk` FOREIGN KEY (`student_id`) REFERENCES `users` (`user_id`) ON DELETE CASCADE
) ENGINE=InnoDB AUTO_INCREMENT=3 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `team_applications`
--

LOCK TABLES `team_applications` WRITE;
/*!40000 ALTER TABLE `team_applications` DISABLE KEYS */;
INSERT INTO `team_applications` VALUES (1,1,41,'我想要加入组队','approved','2026-05-31 05:13:58','2026-05-31 05:48:36'),(2,2,41,'我想要申请','approved','2026-06-12 03:56:11','2026-06-12 03:57:42');
/*!40000 ALTER TABLE `team_applications` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `users`
--

DROP TABLE IF EXISTS `users`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `users` (
  `user_id` int NOT NULL AUTO_INCREMENT,
  `username` varchar(50) COLLATE utf8mb4_unicode_ci NOT NULL,
  `password` varchar(255) COLLATE utf8mb4_unicode_ci NOT NULL,
  `email` varchar(100) COLLATE utf8mb4_unicode_ci NOT NULL,
  `phone` varchar(20) COLLATE utf8mb4_unicode_ci NOT NULL,
  `role` enum('admin','teacher','student') COLLATE utf8mb4_unicode_ci NOT NULL,
  `avatar_id` int DEFAULT NULL,
  `created_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`user_id`),
  UNIQUE KEY `username` (`username`),
  UNIQUE KEY `email` (`email`),
  UNIQUE KEY `phone` (`phone`),
  KEY `idx_role` (`role`)
) ENGINE=InnoDB AUTO_INCREMENT=49 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `users`
--

LOCK TABLES `users` WRITE;
/*!40000 ALTER TABLE `users` DISABLE KEYS */;
INSERT INTO `users` VALUES (2,'lisi','{noop}123456','lisi@edu.cn','13800001002','student',NULL,'2026-06-17 03:18:18','2026-06-17 03:18:18'),(3,'wangwu','{noop}123456','wangwu@edu.cn','13800001003','student',NULL,'2026-06-17 03:18:18','2026-06-17 03:18:18'),(4,'stu002','$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy','stu002@test.com','13800000004','student',NULL,'2026-05-26 03:59:01','2026-05-28 02:57:59'),(5,'stu003','$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy','stu003@test.com','13800000005','student',NULL,'2026-05-26 03:59:01','2026-05-28 02:25:09'),(6,'stu004','$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy','stu004@test.com','13800000006','student',NULL,'2026-05-26 03:59:01','2026-05-28 03:20:27'),(7,'stu005','$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy','stu005@test.com','13800000007','student',NULL,'2026-05-26 03:59:01','2026-05-28 03:20:34'),(9,'stu007','$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy','stu007@test.com','13800000009','student',NULL,'2026-05-26 03:59:01','2026-05-28 03:20:48'),(10,'teacher_zhang','$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy','stu008@test.com','13800000010','student',NULL,'2026-05-26 03:59:01','2026-06-17 07:22:33'),(11,'teacher_li','$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy','stu009@test.com','13800000011','student',NULL,'2026-05-26 03:59:01','2026-06-17 07:22:33'),(12,'teacher_wang','$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy','stu010@test.com','13800000012','student',NULL,'2026-05-26 03:59:01','2026-06-17 07:22:33'),(13,'stu011','$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy','stu011@test.com','13800000013','student',NULL,'2026-05-26 03:59:01','2026-05-28 03:21:23'),(15,'teacher1','$2a$10$z4QCcd/Uucvp6u8T53LM3eoht3IWGM/IM1L6.DnbAR4.cbBQ08DmO','teacher1@example.com','13800138001','teacher',NULL,'2026-05-26 04:12:19','2026-05-26 04:12:19'),(28,'admin','$2a$10$.tPuanjfEWUHxf18IH2Gl.8f69NyyBzcIErLGk0VsH6rkg0wlqsvq','admin@example.com','13800138000','admin',NULL,'2026-05-26 08:42:08','2026-05-26 08:42:08'),(33,'teacher_sun','$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy','sun@school.com','13800000025','teacher',NULL,'2026-05-26 08:44:53','2026-05-26 08:44:53'),(34,'stu_schedule_01','$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy','s01@test.com','13900000001','student',NULL,'2026-05-26 08:44:53','2026-05-28 03:21:38'),(35,'stu_schedule_02','$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy','s02@test.com','13900000002','student',NULL,'2026-05-26 08:44:53','2026-05-28 03:21:44'),(37,'student1','$2a$10$Q3yyPwpIQm/WQLkHr6Ri9egy3sfk5HphD5Ir6pTlGabs8HrrMgL7.','222222222@qq.com','15245684515','student',NULL,'2026-05-28 03:38:58','2026-05-28 03:44:54'),(38,'15003806603','$2a$10$CXMCtiivIfcSUd/9aXktA.842e3n4tdgatyMOI6V9g86YMerSYzuC','zhangsan@example.com','15003806603','student',4,'2026-05-28 13:21:35','2026-05-31 03:55:59'),(40,'teacher','$2a$10$6KTUzNmQyr0o42VlVdtRjOOheEv4BH6D/zdvx3Z6L3gsZjW9CzGdK','3358328949@qq.com','15638198428','teacher',5,'2026-05-29 02:28:18','2026-05-29 06:24:17'),(41,'15738854576','$2a$10$kXnex1faYViREgqWwtrvuesXtWSBB08q5MHuapOnXCxga60Im5oNy','3333333333@qq.com','15738854576','student',6,'2026-05-31 04:03:46','2026-06-04 07:27:29'),(42,'zhangsan','$2a$10$llGe2RdCttlzV.uylA.Mqu6bUk0pH3QI2aFfeqUSUUcxhjWlhnzMy','anng@edu.cn','13800000001','student',NULL,NULL,NULL),(46,'13783720228','$2a$10$j1LwUM5AUzah2X9Ps6JfWervHKMHGKI9DnfQxmaGPDux7mFkaWHam','123456@qq.com','13783720228','student',NULL,'2026-06-14 07:09:32','2026-06-14 07:09:32'),(47,'13805652155','$2a$10$m.RmOZo5F94kaxBT/GWS6.mPU893N4v4mII322dgpoY9UV7oK2jme','xingyaos6@gmail.com','13805652155','student',NULL,'2026-06-14 07:19:42','2026-06-14 07:19:42'),(48,'13900139000','$2a$10$azE/y66QOYW84p0WijQXpOhiQ9VISeP6CBEPjbQ09vZEu.d08nPL2','zhangsan@test.com','13900139000','student',NULL,'2026-06-14 07:57:57','2026-06-14 07:57:57');
/*!40000 ALTER TABLE `users` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `verification_codes`
--

DROP TABLE IF EXISTS `verification_codes`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `verification_codes` (
  `code_id` int NOT NULL AUTO_INCREMENT,
  `user_id` int DEFAULT NULL,
  `code` varchar(10) COLLATE utf8mb4_unicode_ci NOT NULL,
  `type` enum('login','register','reset_password') COLLATE utf8mb4_unicode_ci NOT NULL,
  `expire_time` timestamp NOT NULL,
  `created_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`code_id`),
  KEY `user_id` (`user_id`),
  KEY `idx_code_expire` (`code`,`expire_time`),
  CONSTRAINT `verification_codes_ibfk_1` FOREIGN KEY (`user_id`) REFERENCES `users` (`user_id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `verification_codes`
--

LOCK TABLES `verification_codes` WRITE;
/*!40000 ALTER TABLE `verification_codes` DISABLE KEYS */;
/*!40000 ALTER TABLE `verification_codes` ENABLE KEYS */;
UNLOCK TABLES;
/*!40103 SET TIME_ZONE=@OLD_TIME_ZONE */;

/*!40101 SET SQL_MODE=@OLD_SQL_MODE */;
/*!40014 SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS */;
/*!40014 SET UNIQUE_CHECKS=@OLD_UNIQUE_CHECKS */;
/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
/*!40111 SET SQL_NOTES=@OLD_SQL_NOTES */;

-- Dump completed on 2026-06-17 15:27:13
