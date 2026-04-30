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

| 接口名称 | 请求方法 | 请求路径 | 说明 |
|---------|---------|---------|------|
| 注册 | POST | /api/auth/register | 注册用户（管理员/教师/学生） |
| 登录 | POST | /api/auth/login | 用户登录 |
| 获取验证码 | GET | /api/auth/verification-code | 获取登录验证码 |
| 找回密码 | POST | /api/auth/reset-password | 重置密码 |

### 用户管理模块

| 接口名称 | 请求方法 | 请求路径 | 说明 | 权限 |
|---------|---------|---------|------|------|
| 获取用户列表 | GET | /api/users | 分页获取用户列表 | 管理员 |
| 根据用户ID获取用户信息 | GET | /api/users/{user_id} | 根据ID获取用户详细信息 | 管理员/自己 |
| 修改用户密码 | PUT | /api/users/{user_id}/password | 修改用户密码 | 管理员/自己 |
| 获取个人信息 | GET | /api/users/profile | 获取当前登录用户信息 | 登录用户 |
| 修改个人信息 | PUT | /api/users/profile | 修改当前登录用户信息 | 登录用户 |

### 课程管理模块

| 接口名称 | 请求方法 | 请求路径 | 说明 | 权限 |
|---------|---------|---------|------|------|
| 创建课程 | POST | /api/courses | 创建新课程 | 管理员 |
| 获取课程列表 | GET | /api/courses | 获取课程列表 | 管理员 |
| 修改课程 | PUT | /api/courses/{course_id} | 修改课程信息 | 管理员 |
| 删除课程 | DELETE | /api/courses/{course_id} | 删除课程 | 管理员 |
| 创建排课 | POST | /api/course-schedules | 创建排课信息 | 管理员 |
| 获取排课列表 | GET | /api/course-schedules | 获取排课列表 | 管理员 |

### 成绩管理模块

| 接口名称 | 请求方法 | 请求路径 | 说明 | 权限 |
|---------|---------|---------|------|------|
| 录入成绩 | POST | /api/grades | 录入学生成绩 | 教师/管理员 |
| 修改成绩 | PUT | /api/grades/{grade_id} | 修改成绩信息 | 教师/管理员 |
| 教师获取课程成绩 | GET | /api/teacher/grades | 教师获取课程成绩列表 | 教师 |
| 学生获取成绩 | GET | /api/student/grades | 学生获取个人成绩 | 学生 |
| 下载成绩报表 | GET | /api/grades/report | 下载成绩报表 | 教师/管理员 |

### 科研项目模块

| 接口名称 | 请求方法 | 请求路径 | 说明 | 权限 |
|---------|---------|---------|------|------|
| 发布科研项目 | POST | /api/research-projects | 发布科研项目 | 教师/管理员 |
| 获取科研项目列表 | GET | /api/research-projects | 获取项目列表 | 所有用户 |
| 申请科研项目 | POST | /api/project-applications | 学生申请项目 | 学生 |
| 审核科研项目申请 | PUT | /api/project-applications/{application_id} | 审核申请 | 教师/管理员 |
| 获取科研项目申请列表 | GET | /api/project-applications | 获取申请列表 | 教师/管理员 |

### 大创赛组队模块

| 接口名称 | 请求方法 | 请求路径 | 说明 | 权限 |
|---------|---------|---------|------|------|
| 发起组队 | POST | /api/innovation-teams | 发起组队 | 学生/管理员 |
| 获取组队列表 | GET | /api/innovation-teams | 获取组队列表 | 所有用户 |
| 申请加入组队 | POST | /api/team-applications | 申请加入 | 学生 |
| 审核组队申请 | PUT | /api/team-applications/{application_id} | 审核申请 | 队长/管理员 |
| 获取组队申请列表 | GET | /api/team-applications | 获取申请列表 | 队长/管理员 |

### 图片管理模块

| 接口名称 | 请求方法 | 请求路径 | 说明 | 权限 |
|---------|---------|---------|------|------|
| 上传图片 | POST | /api/images | 上传图片 | 登录用户 |
| 获取图片列表 | GET | /api/images | 获取图片列表 | 登录用户 |

### 报表统计模块

| 接口名称 | 请求方法 | 请求路径 | 说明 | 权限 |
|---------|---------|---------|------|------|
| 生成成绩报表 | POST | /api/reports/grades | 生成成绩报表 | 教师/管理员 |
| 生成用户统计报表 | POST | /api/reports/users | 生成用户统计报表 | 管理员 |
| 获取报表列表 | GET | /api/reports | 获取报表列表 | 教师/管理员 |

### 查询检索模块

| 接口名称 | 请求方法 | 请求路径 | 说明 | 权限 |
|---------|---------|---------|------|------|
| 通用搜索 | GET | /api/search | 通用搜索接口 | 登录用户 |

## 用户管理模块详解

### 1. 用户角色说明

系统支持三种用户角色：

- **admin（管理员）**：拥有系统管理权限，可以管理所有用户、课程、成绩等
- **teacher（教师）**：可以管理课程、录入成绩、管理科研项目等
- **student（学生）**：可以选课、查看成绩、申请科研项目、发起组队等

### 2. 用户注册流程

注册时会根据用户角色自动创建相应的详细信息记录：

**管理员注册**：
```json
{
  "username": "admin",
  "password": "admin123",
  "email": "admin@example.com",
  "phone": "13800138000",
  "role": "admin",
  "name": "系统管理员"
}
```

**教师注册**：
```json
{
  "username": "teacher1",
  "password": "teacher123",
  "email": "teacher1@example.com",
  "phone": "13800138001",
  "role": "teacher",
  "name": "张老师",
  "title": "讲师",
  "department": "计算机科学与技术"
}
```

**学生注册**：
```json
{
  "username": "student1",
  "password": "student123",
  "email": "student1@example.com",
  "phone": "13800138002",
  "role": "student",
  "name": "李同学",
  "studentNumber": "2023150001",
  "major": "计算机科学与技术",
  "grade": "2023",
  "clazz": "一班"
}
```

### 3. 权限控制说明

#### 获取用户列表
- **权限**：仅管理员
- **说明**：管理员可以分页获取所有用户列表，支持按角色筛选

#### 根据用户ID获取用户信息
- **权限**：管理员可以查看所有用户，普通用户只能查看自己
- **说明**：根据用户ID获取用户的详细信息，包括角色相关的详细信息

#### 修改用户密码
- **权限**：管理员可以修改所有用户密码，普通用户只能修改自己密码
- **说明**：用户修改密码需要提供新密码

#### 获取/修改个人信息
- **权限**：登录用户可以获取和修改自己的信息
- **说明**：根据当前登录用户的JWT令牌获取用户ID，自动关联用户详细信息

## 接口测试数据

### 认证模块测试数据

**管理员登录**：
- 请求体：`{"username":"admin","password":"admin123","verification_code":"123456"}`
- 响应：`{"code":200,"message":"登录成功","data":{"userId":1,"username":"admin","role":"admin","token":"..."}}`

**教师登录**：
- 请求体：`{"username":"teacher1","password":"teacher123","verification_code":"123456"}`
- 响应：`{"code":200,"message":"登录成功","data":{"userId":2,"username":"teacher1","role":"teacher","token":"..."}}`

**学生登录**：
- 请求体：`{"username":"student1","password":"student123","verification_code":"123456"}`
- 响应：`{"code":200,"message":"登录成功","data":{"userId":3,"username":"student1","role":"student","token":"..."}}`

### 用户管理模块测试数据

**管理员获取用户列表**：
- URL：`GET /api/users?page=1&pageSize=10`
- 响应：`{"code":200,"message":"获取成功","data":{"list":[{"userId":1,"username":"admin","role":"admin"}],"total":3,"page":1,"pageSize":10}}`

**管理员获取指定用户信息**：
- URL：`GET /api/users/2`
- 响应：`{"code":200,"message":"获取成功","data":{"userId":2,"username":"teacher1","role":"teacher","teacher":{"teacherId":2,"name":"张老师","title":"讲师","department":"计算机科学与技术"}}}`

**教师修改自己密码**：
- URL：`PUT /api/users/2/password?newPassword=teacher456`
- 响应：`{"code":200,"message":"密码修改成功","data":null}`

**获取当前用户信息**：
- URL：`GET /api/users/profile`
- 响应：`{"code":200,"message":"获取成功","data":{"userId":2,"username":"teacher1","role":"teacher","teacher":{...}}}`

**修改个人信息**：
- URL：`PUT /api/users/profile`
- 请求体：
  ```json
  {
    "email": "teacher1_new@example.com",
    "phone": "13800138004",
    "name": "张教授",
    "title": "副教授",
    "department": "计算机科学与技术学院",
    "bio": "主要研究方向为人工智能"
  }
  ```
- 响应：`{"code":200,"message":"信息修改成功","data":null}`

## 注意事项

1. **身份验证**：所有接口都需要在请求头中携带 `Authorization: Bearer {token}` 进行身份验证
2. **权限控制**：
   - 管理员：可以管理所有资源
   - 教师：可以管理课程、成绩、科研项目
   - 学生：可以选课、查看成绩、申请项目和组队
3. **接口返回格式**：统一返回JSON格式，包含code、message和data字段
4. **分页参数**：所有列表接口支持page和pageSize参数
5. **错误码说明**：
   - 200：成功
   - 400：请求参数错误
   - 401：未登录或token过期
   - 403：权限不足
   - 500：服务器内部错误

## 开发指南

1. **代码风格**: 遵循Java代码规范，使用Lombok简化代码
2. **异常处理**: 统一处理异常，返回标准错误信息
3. **日志记录**: 使用SLF4J进行日志记录
4. **测试**: 编写单元测试和集成测试
5. **安全性**: 使用JWT进行身份验证，Spring Security进行权限控制

## 联系方式

- 开发者: 尚欣瑶
- 邮箱: example@example.com
- 版本: 1.0.0