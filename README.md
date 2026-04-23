# 智能教学管理系统后端

## 项目介绍

智能教学管理系统是一个基于Spring Boot的后端服务，提供用户管理、课程管理、成绩管理、科研项目管理和大创赛组队等功能。系统采用Maven作为构建工具，MySQL作为数据库，实现了完整的RESTful API接口。

## 技术栈

- **框架**: Spring Boot 3.2.0
- **构建工具**: Maven
- **数据库**: MySQL
- **ORM**: MyBatis
- **认证**: JWT
- **安全**: Spring Security
- **其他**: Lombok, Commons IO

## 项目结构

```
education-system/
├── pom.xml                    # Maven项目配置文件
├── src/
│   ├── main/
│   │   ├── java/com/example/education/system/  # 源代码目录
│   │   │   ├── auth/          # 认证模块
│   │   │   ├── users/         # 用户管理模块
│   │   │   ├── courses/       # 课程管理模块
│   │   │   ├── grades/        # 成绩管理模块
│   │   │   ├── research/      # 科研项目模块
│   │   │   ├── innovation/     # 大创赛组队模块
│   │   │   ├── images/        # 图片管理模块
│   │   │   ├── reports/       # 报表统计模块
│   │   │   ├── search/        # 查询检索模块
│   │   │   └── EducationSystemApplication.java  # 应用主类
│   │   └── resources/         # 资源文件目录
│   │       ├── application.properties  # 应用配置文件
│   │       └── mapper/        # MyBatis映射文件目录
│   └── test/                  # 测试目录
└── README.md                  # 项目说明文件
```

## 安装部署

### 1. 环境准备

- JDK 17+
- MySQL 8.0+
- Maven 3.6+

### 2. 数据库配置

1. 创建数据库：
   ```sql
   CREATE DATABASE IF NOT EXISTS education_system CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
   ```

2. 执行建表语句：
   - 参考 `MySQL建表语句.md` 文件中的SQL语句

### 3. 项目配置

修改 `src/main/resources/application.properties` 文件中的数据库连接信息：

```properties
spring.datasource.url=jdbc:mysql://localhost:3306/education_system?useUnicode=true&characterEncoding=utf8&useSSL=false&serverTimezone=Asia/Shanghai
spring.datasource.username=root
spring.datasource.password=root
```

### 4. 构建项目

```bash
mvn clean package
```

### 5. 运行项目

```bash
java -jar target/education-system-0.0.1-SNAPSHOT.jar
```

或在IntelliJ IDEA中直接运行 `EducationSystemApplication.java` 类。

## API接口说明

### 认证模块

- **注册**: `POST /api/auth/register`
- **登录**: `POST /api/auth/login`
- **获取验证码**: `GET /api/auth/verification-code`
- **找回密码**: `POST /api/auth/reset-password`

### 用户管理模块

- **获取用户列表**: `GET /api/users`
- **修改用户密码**: `PUT /api/users/{user_id}/password`
- **获取个人信息**: `GET /api/users/profile`
- **修改个人信息**: `PUT /api/users/profile`

### 课程管理模块

- **创建课程**: `POST /api/courses`
- **获取课程列表**: `GET /api/courses`
- **修改课程**: `PUT /api/courses/{course_id}`
- **删除课程**: `DELETE /api/courses/{course_id}`
- **创建排课**: `POST /api/course-schedules`
- **获取排课列表**: `GET /api/course-schedules`

### 成绩管理模块

- **录入成绩**: `POST /api/grades`
- **修改成绩**: `PUT /api/grades/{grade_id}`
- **教师获取课程成绩**: `GET /api/teacher/grades`
- **学生获取成绩**: `GET /api/student/grades`
- **下载成绩报表**: `GET /api/grades/report`

### 科研项目模块

- **发布科研项目**: `POST /api/research-projects`
- **获取科研项目列表**: `GET /api/research-projects`
- **申请科研项目**: `POST /api/project-applications`
- **审核科研项目申请**: `PUT /api/project-applications/{application_id}`
- **获取科研项目申请列表**: `GET /api/project-applications`

### 大创赛组队模块

- **发起组队**: `POST /api/innovation-teams`
- **获取组队列表**: `GET /api/innovation-teams`
- **申请加入组队**: `POST /api/team-applications`
- **审核组队申请**: `PUT /api/team-applications/{application_id}`
- **获取组队申请列表**: `GET /api/team-applications`

### 图片管理模块

- **上传图片**: `POST /api/images`
- **获取图片列表**: `GET /api/images`

### 报表统计模块

- **生成成绩报表**: `POST /api/reports/grades`
- **生成用户统计报表**: `POST /api/reports/users`
- **获取报表列表**: `GET /api/reports`

### 查询检索模块

- **通用搜索**: `GET /api/search`

## 注意事项

1. 所有接口都需要在请求头中携带 `Authorization: Bearer {token}` 进行身份验证
2. 不同角色拥有不同的接口访问权限
3. 接口返回格式统一为JSON，包含code、message和data字段
4. 分页接口支持page和pageSize参数

## 开发指南

1. **代码风格**: 遵循Java代码规范，使用Lombok简化代码
2. **异常处理**: 统一处理异常，返回标准错误信息
3. **日志记录**: 使用SLF4J进行日志记录
4. **测试**: 编写单元测试和集成测试

## 联系方式

- 开发者: 尚欣瑶
- 邮箱: example@example.com
- 版本: 1.0.0
