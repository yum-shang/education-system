package com.example.education.system.users.service;

import com.example.education.system.users.dto.UserListResponse;
import com.example.education.system.users.dto.ProfileResponse;
import com.example.education.system.users.dto.StudentListResponse;
import com.example.education.system.users.dto.BatchImportStudentRequest;
import com.example.education.system.users.dto.BatchImportResultResponse;
import com.example.education.system.users.dto.BatchImportResultItem;
import com.example.education.system.users.dto.CreateStudentRequest;
import com.example.education.system.auth.model.User;
import com.example.education.system.users.model.Teacher;
import com.example.education.system.users.model.Student;
import com.example.education.system.users.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.springframework.web.multipart.MultipartFile;

/**
 * 用户管理服务
 * 
 * 负责用户信息的管理和查询，包括：
 * - 用户列表查询（支持分页和角色筛选）
 * - 用户密码修改
 * - 用户资料获取和更新
 * - 根据手机号/邮箱查询用户
 * - 教师和学生详细信息管理
 */
@Service
public class UserService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private com.example.education.system.auth.repository.UserRepository authUserRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    public UserListResponse getUserList(String role, Integer page, Integer pageSize) {
        int offset = (page - 1) * pageSize;
        List<User> users = userRepository.findUsers(role, offset, pageSize);
        Integer total = userRepository.countUsers(role);

        UserListResponse response = new UserListResponse();
        response.setCode(200);
        response.setMessage("获取成功");

        UserListResponse.Data data = new UserListResponse.Data();
        List<UserListResponse.UserInfo> userInfos = new ArrayList<>();

        for (User user : users) {
            UserListResponse.UserInfo info = new UserListResponse.UserInfo();
            info.setUserId(user.getUserId());
            info.setUsername(user.getUsername());
            info.setEmail(user.getEmail());
            info.setPhone(user.getPhone());
            info.setRole(user.getRole());
            info.setAvatarId(user.getAvatarId());
            info.setCreatedAt(user.getCreatedAt().toString());

            if ("teacher".equals(user.getRole())) {
                Teacher teacher = userRepository.findTeacherById(user.getUserId());
                if (teacher != null) {
                    info.setName(teacher.getName());
                    info.setTitle(teacher.getTitle());
                    info.setDepartment(teacher.getDepartment());
                    info.setBio(teacher.getBio());
                }
            } else if ("student".equals(user.getRole())) {
                Student student = userRepository.findStudentByUserId(user.getUserId());
                if (student != null) {
                    info.setName(student.getName());
                    info.setStudentNumber(student.getStudentNumber());
                    info.setMajor(student.getMajor());
                    info.setGrade(student.getGrade());
                    info.setClazz(student.getClazz());
                    info.setDepartment(student.getDepartment());
                    info.setGender(student.getGender());
                }
            }

            userInfos.add(info);
        }

        data.setList(userInfos);
        data.setTotal(total);
        data.setPage(page);
        data.setPageSize(pageSize);

        response.setData(data);
        return response;
    }

    public void updateUserPassword(Integer userId, String newPassword) {
        User user = userRepository.findUserById(userId);
        if (user != null) {
            user.setPassword(passwordEncoder.encode(newPassword));
            userRepository.updateUser(user);
        }
    }

    public User findByPhone(String phone) {
        if (phone == null || phone.isEmpty()) {
            return null;
        }
        return userRepository.findByPhone(phone);
    }

    public User findByEmail(String email) {
        if (email == null || email.isEmpty()) {
            return null;
        }
        return userRepository.findByEmail(email);
    }

    public ProfileResponse getProfile(Integer userId) {
        User user = userRepository.findUserById(userId);
        if (user == null) {
            ProfileResponse response = new ProfileResponse();
            response.setCode(404);
            response.setMessage("用户不存在");
            return response;
        }

        ProfileResponse response = new ProfileResponse();
        response.setCode(200);
        response.setMessage("获取成功");

        ProfileResponse.Data data = new ProfileResponse.Data();
        data.setUserId(user.getUserId());
        data.setUsername(user.getUsername());
        data.setEmail(user.getEmail());
        data.setPhone(user.getPhone());
        data.setRole(user.getRole());
        data.setAvatarId(user.getAvatarId());

        // 根据用户角色查询详细信息
        if ("teacher".equals(user.getRole())) {
            Teacher teacher = userRepository.findTeacherById(userId);
            if (teacher != null) {
                data.setName(teacher.getName());
                data.setTitle(teacher.getTitle());
                data.setDepartment(teacher.getDepartment());
                data.setBio(teacher.getBio());
            }
        } else if ("student".equals(user.getRole())) {
            Student student = userRepository.findStudentByUserId(userId);
            if (student != null) {
                data.setName(student.getName());
                data.setStudentNumber(student.getStudentNumber());
                data.setMajor(student.getMajor());
                data.setGrade(student.getGrade());
                data.setClazz(student.getClazz());
                data.setDepartment(student.getDepartment());
                data.setGender(student.getGender());
            }
            Double credits = userRepository.sumStudentCredits(userId);
            data.setTotalCredit(credits != null ? credits : 0.0);
        }

        response.setData(data);
        return response;
    }

    public void updateProfile(Integer userId, ProfileResponse.Data profileData) {
        User user = userRepository.findUserById(userId);
        if (user != null) {
            // 检查手机号是否被其他用户使用（排除自己）
            if (profileData.getPhone() != null && !profileData.getPhone().isEmpty()) {
                // 只有当手机号真正改变时才检查
                if (!profileData.getPhone().equals(user.getPhone())) {
                    User existingUser = userRepository.findByPhone(profileData.getPhone());
                    if (existingUser != null) {
                        throw new RuntimeException("该手机号已被使用");
                    }
                }
            }
            
            // 检查邮箱是否被其他用户使用（排除自己）
            if (profileData.getEmail() != null && !profileData.getEmail().isEmpty()) {
                // 只有当邮箱真正改变时才检查
                if (!profileData.getEmail().equals(user.getEmail())) {
                    User existingUser = userRepository.findByEmail(profileData.getEmail());
                    if (existingUser != null) {
                        throw new RuntimeException("该邮箱已被使用");
                    }
                }
            }
            
            user.setEmail(profileData.getEmail());
            user.setPhone(profileData.getPhone());
            user.setAvatarId(profileData.getAvatarId());
            userRepository.updateUser(user);

            if ("teacher".equals(user.getRole())) {
                Teacher teacher = userRepository.findTeacherById(userId);
                if (teacher != null) {
                    teacher.setName(profileData.getName());
                    teacher.setTitle(profileData.getTitle());
                    teacher.setDepartment(profileData.getDepartment());
                    teacher.setBio(profileData.getBio());
                    userRepository.updateTeacher(teacher);
                } else {
                    Teacher newTeacher = new Teacher();
                    newTeacher.setTeacherId(userId);
                    newTeacher.setName(profileData.getName());
                    newTeacher.setTitle(profileData.getTitle());
                    newTeacher.setDepartment(profileData.getDepartment());
                    newTeacher.setBio(profileData.getBio());
                    userRepository.insertTeacher(newTeacher);
                }
            } else if ("student".equals(user.getRole())) {
                Student student = userRepository.findStudentByUserId(userId);
                if (student != null) {
                    student.setName(profileData.getName());
                    student.setStudentNumber(profileData.getStudentNumber());
                    student.setMajor(profileData.getMajor());
                    student.setGrade(profileData.getGrade());
                    student.setClazz(profileData.getClazz());
                    student.setDepartment(profileData.getDepartment());
                    student.setGender(profileData.getGender());
                    userRepository.updateStudent(student);
                } else {
                    Student newStudent = new Student();
                    newStudent.setUserId(userId);
                    newStudent.setName(profileData.getName());
                    newStudent.setStudentNumber(profileData.getStudentNumber());
                    newStudent.setMajor(profileData.getMajor());
                    newStudent.setGrade(profileData.getGrade());
                    newStudent.setClazz(profileData.getClazz());
                    newStudent.setDepartment(profileData.getDepartment());
                    newStudent.setGender(profileData.getGender());
                    userRepository.insertStudent(newStudent);
                }
            }
        }
    }

    public StudentListResponse getStudentList(String keyword, String department, String grade, Integer page, Integer pageSize) {
        int offset = (page - 1) * pageSize;
        List<Student> students = userRepository.findStudents(keyword, department, grade, offset, pageSize);
        Integer total = userRepository.countStudents(keyword, department, grade);

        StudentListResponse response = new StudentListResponse();
        response.setCode(200);
        response.setMessage("获取成功");

        StudentListResponse.Data data = new StudentListResponse.Data();
        List<StudentListResponse.StudentInfo> studentInfos = new ArrayList<>();

        for (Student student : students) {
            User user = userRepository.findUserById(student.getUserId());

            StudentListResponse.StudentInfo info = new StudentListResponse.StudentInfo();
            info.setUserId(student.getUserId());
            if (user != null) {
                info.setUsername(user.getUsername());
                info.setEmail(user.getEmail());
                info.setPhone(user.getPhone());
                info.setRole(user.getRole());
                info.setAvatarId(user.getAvatarId());
                info.setCreatedAt(user.getCreatedAt() != null ? user.getCreatedAt().toString() : null);
            }
            info.setName(student.getName());
            info.setStudentNumber(student.getStudentNumber());
            info.setMajor(student.getMajor());
            info.setGrade(student.getGrade());
            info.setClazz(student.getClazz());
            info.setDepartment(student.getDepartment());
            info.setGender(student.getGender());

            studentInfos.add(info);
        }

        data.setList(studentInfos);
        data.setTotal(total);
        data.setPage(page);
        data.setPageSize(pageSize);

        response.setData(data);
        return response;
    }

    @Transactional
    public StudentListResponse createStudent(com.example.education.system.users.dto.CreateStudentRequest request) {
        User existingByPhone = userRepository.findByPhone(request.getPhone());
        if (existingByPhone != null) {
            StudentListResponse response = new StudentListResponse();
            response.setCode(400);
            response.setMessage("手机号已被使用");
            return response;
        }

        User existingByEmail = userRepository.findByEmail(request.getEmail());
        if (existingByEmail != null) {
            StudentListResponse response = new StudentListResponse();
            response.setCode(400);
            response.setMessage("邮箱已被使用");
            return response;
        }

        User user = new User();
        user.setUsername(request.getUsername());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setEmail(request.getEmail());
        user.setPhone(request.getPhone());
        user.setRole("student");
        authUserRepository.insert(user);

        Student student = new Student();
        student.setUserId(user.getUserId());
        student.setName(request.getName());
        student.setStudentNumber(request.getStudentNumber() != null && !request.getStudentNumber().isEmpty()
                ? request.getStudentNumber() : "S" + System.currentTimeMillis() % 100000);
        student.setMajor(request.getMajor());
        student.setGrade(request.getGrade());
        student.setClazz(request.getClazz());
        student.setDepartment(request.getDepartment());
        student.setGender(request.getGender());
        userRepository.insertStudent(student);

        StudentListResponse response = new StudentListResponse();
        response.setCode(200);
        response.setMessage("学生添加成功");

        StudentListResponse.Data data = new StudentListResponse.Data();
        List<StudentListResponse.StudentInfo> list = new ArrayList<>();
        StudentListResponse.StudentInfo info = new StudentListResponse.StudentInfo();
        info.setUserId(user.getUserId());
        info.setUsername(user.getUsername());
        info.setName(student.getName());
        info.setStudentNumber(student.getStudentNumber());
        list.add(info);
        data.setList(list);
        data.setTotal(1);
        data.setPage(1);
        data.setPageSize(10);
        response.setData(data);

        return response;
    }

    @Transactional
    public StudentListResponse updateStudent(Integer userId, com.example.education.system.users.dto.UpdateStudentRequest request) {
        User user = userRepository.findUserById(userId);
        if (user == null || !"student".equals(user.getRole())) {
            StudentListResponse response = new StudentListResponse();
            response.setCode(404);
            response.setMessage("学生不存在");
            return response;
        }

        if (request.getEmail() != null && !request.getEmail().equals(user.getEmail())) {
            User existingByEmail = userRepository.findByEmail(request.getEmail());
            if (existingByEmail != null) {
                StudentListResponse response = new StudentListResponse();
                response.setCode(400);
                response.setMessage("邮箱已被使用");
                return response;
            }
            user.setEmail(request.getEmail());
        }
        if (request.getPhone() != null && !request.getPhone().equals(user.getPhone())) {
            User existingByPhone = userRepository.findByPhone(request.getPhone());
            if (existingByPhone != null) {
                StudentListResponse response = new StudentListResponse();
                response.setCode(400);
                response.setMessage("手机号已被使用");
                return response;
            }
            user.setPhone(request.getPhone());
        }
        userRepository.updateUser(user);

        Student student = userRepository.findStudentByUserId(userId);
        if (student != null) {
            if (request.getName() != null) student.setName(request.getName());
            if (request.getStudentNumber() != null) student.setStudentNumber(request.getStudentNumber());
            if (request.getMajor() != null) student.setMajor(request.getMajor());
            if (request.getGrade() != null) student.setGrade(request.getGrade());
            if (request.getClazz() != null) student.setClazz(request.getClazz());
            if (request.getDepartment() != null) student.setDepartment(request.getDepartment());
            if (request.getGender() != null) student.setGender(request.getGender());
            userRepository.updateStudent(student);
        }

        StudentListResponse response = new StudentListResponse();
        response.setCode(200);
        response.setMessage("学生信息修改成功");
        return response;
    }

    @Transactional
    public StudentListResponse deleteStudent(Integer userId) {
        User user = userRepository.findUserById(userId);
        if (user == null || !"student".equals(user.getRole())) {
            StudentListResponse response = new StudentListResponse();
            response.setCode(404);
            response.setMessage("学生不存在");
            return response;
        }

        userRepository.deleteUser(userId);

        StudentListResponse response = new StudentListResponse();
        response.setCode(200);
        response.setMessage("学生删除成功");
        return response;
    }

    @Transactional
    public BatchImportResultResponse batchImportStudents(BatchImportStudentRequest request) {
        BatchImportResultResponse response = new BatchImportResultResponse();
        response.setCode(200);

        List<BatchImportResultItem> results = new ArrayList<>();
        if (request.getStudents() == null || request.getStudents().isEmpty()) {
            response.setMessage("没有要导入的数据");
            response.setData(results);
            return response;
        }

        java.util.Set<String> batchPhones = new java.util.HashSet<>();
        java.util.Map<String, Integer> phoneRowMap = new java.util.LinkedHashMap<>();
        for (int i = 0; i < request.getStudents().size(); i++) {
            CreateStudentRequest item = request.getStudents().get(i);
            if (item.getPhone() != null && !item.getPhone().isEmpty()) {
                if (phoneRowMap.containsKey(item.getPhone())) {
                    int prevIdx = phoneRowMap.get(item.getPhone());
                    BatchImportResultItem fail = new BatchImportResultItem();
                    fail.setUsername(item.getUsername());
                    fail.setName(item.getName());
                    fail.setSuccess(false);
                    fail.setErrorMessage("手机号与第" + (prevIdx + 1) + "行重复");
                    results.add(fail);
                    continue;
                }
                phoneRowMap.put(item.getPhone(), i);
            }
            batchPhones.add(item.getPhone());
        }

        for (int i = 0; i < request.getStudents().size(); i++) {
            CreateStudentRequest item = request.getStudents().get(i);

            boolean alreadyFailed = false;
            for (BatchImportResultItem r : results) {
                if (r.getUsername() != null && r.getUsername().equals(item.getUsername())) {
                    alreadyFailed = true;
                    break;
                }
            }
            if (alreadyFailed) continue;

            try {
                if (item.getPassword() == null || item.getPassword().isEmpty()) {
                    item.setPassword("123456");
                }

                if (item.getPhone() != null && !item.getPhone().isEmpty()) {
                    User existingUser = userRepository.findByPhone(item.getPhone());
                    if (existingUser != null) {
                        BatchImportResultItem fail = new BatchImportResultItem();
                        fail.setUsername(item.getUsername());
                        fail.setName(item.getName());
                        fail.setSuccess(false);
                        fail.setErrorMessage("手机号已被注册");
                        results.add(fail);
                        continue;
                    }
                }

                StudentListResponse createResult = createStudent(item);
                BatchImportResultItem ok = new BatchImportResultItem();
                ok.setUsername(item.getUsername());
                ok.setName(item.getName());
                if (createResult.getCode() == 200 && createResult.getData() != null) {
                    ok.setSuccess(true);
                    if (item.getStudentNumber() != null && !item.getStudentNumber().isEmpty()) {
                        ok.setStudentNumber(item.getStudentNumber());
                    }
                } else {
                    ok.setSuccess(false);
                    ok.setErrorMessage(createResult.getMessage());
                }
                results.add(ok);
            } catch (Exception e) {
                BatchImportResultItem fail = new BatchImportResultItem();
                fail.setUsername(item.getUsername());
                fail.setName(item.getName());
                fail.setSuccess(false);
                fail.setErrorMessage(e.getMessage() != null ? e.getMessage() : "导入失败");
                results.add(fail);
            }
        }

        long successCount = results.stream().filter(BatchImportResultItem::isSuccess).count();
        response.setMessage("批量导入完成：成功 " + successCount + " 条，失败 " + (results.size() - successCount) + " 条");
        response.setData(results);
        return response;
    }

    public BatchImportResultResponse importStudentsFromFile(MultipartFile file) {
        BatchImportResultResponse response = new BatchImportResultResponse();
        response.setCode(200);

        String filename = file.getOriginalFilename();
        if (filename == null || filename.isEmpty()) {
            response.setCode(400);
            response.setMessage("文件名为空");
            return response;
        }

        List<String[]> rows;
        try {
            if (filename.endsWith(".xlsx") || filename.endsWith(".xls")) {
                rows = parseExcel(file);
            } else if (filename.endsWith(".csv")) {
                rows = parseCsv(file);
            } else {
                response.setCode(400);
                response.setMessage("不支持的文件格式，请上传 .xlsx、.xls 或 .csv 文件");
                return response;
            }
        } catch (IOException e) {
            response.setCode(500);
            response.setMessage("文件解析失败: " + e.getMessage());
            return response;
        }

        if (rows.isEmpty()) {
            response.setCode(400);
            response.setMessage("文件没有数据行");
            return response;
        }

        // 第一行是表头
        String[] headers = rows.get(0);
        Map<String, Integer> headerMap = new HashMap<>();
        for (int i = 0; i < headers.length; i++) {
            headerMap.put(headers[i].trim(), i);
        }

        List<BatchImportResultItem> results = new ArrayList<>();
        for (int i = 1; i < rows.size(); i++) {
            String[] row = rows.get(i);
            BatchImportResultItem item = new BatchImportResultItem();

            String studentNumber = getCell(row, headerMap.get("学号"));
            String name = getCell(row, headerMap.get("姓名"));

            item.setStudentNumber(studentNumber);
            item.setName(name);

            if (studentNumber == null || studentNumber.isEmpty()) {
                item.setSuccess(false);
                item.setErrorMessage("学号为空");
                results.add(item);
                continue;
            }
            if (name == null || name.isEmpty()) {
                item.setSuccess(false);
                item.setErrorMessage("姓名为空");
                results.add(item);
                continue;
            }
            if (userRepository.findStudentByStudentNumber(studentNumber) != null) {
                item.setSuccess(false);
                item.setErrorMessage("学号已存在");
                results.add(item);
                continue;
            }

            try {
                Student student = new Student();
                student.setUserId(null);
                student.setName(name);
                student.setStudentNumber(studentNumber);
                student.setMajor(getCell(row, headerMap.get("专业")));
                student.setGrade(getCell(row, headerMap.get("年级")));
                student.setClazz(getCell(row, headerMap.get("班级")));
                student.setDepartment(getCell(row, headerMap.get("院系")));
                student.setGender(getCell(row, headerMap.get("性别")));
                userRepository.insertStudent(student);
                item.setSuccess(true);
            } catch (Exception e) {
                item.setSuccess(false);
                item.setErrorMessage("导入失败: " + e.getMessage());
            }
            results.add(item);
        }

        long successCount = results.stream().filter(BatchImportResultItem::isSuccess).count();
        response.setMessage("批量导入完成：成功 " + successCount + " 条，失败 " + (results.size() - successCount) + " 条");
        response.setData(results);
        return response;
    }

    private List<String[]> parseExcel(MultipartFile file) throws IOException {
        List<String[]> rows = new ArrayList<>();
        Workbook workbook;
        if (file.getOriginalFilename() != null && file.getOriginalFilename().endsWith(".xls")) {
            workbook = new HSSFWorkbook(file.getInputStream());
        } else {
            workbook = new XSSFWorkbook(file.getInputStream());
        }
        Sheet sheet = workbook.getSheetAt(0);
        for (Row row : sheet) {
            String[] cells = new String[row.getLastCellNum()];
            for (int i = 0; i < row.getLastCellNum(); i++) {
                Cell cell = row.getCell(i);
                cells[i] = cell == null ? "" : cell.toString().trim();
            }
            rows.add(cells);
        }
        workbook.close();
        return rows;
    }

    private List<String[]> parseCsv(MultipartFile file) throws IOException {
        List<String[]> rows = new ArrayList<>();
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(file.getInputStream()))) {
            String line;
            while ((line = reader.readLine()) != null) {
                if (!line.trim().isEmpty()) {
                    rows.add(line.split(","));
                }
            }
        }
        return rows;
    }

    private String getCell(String[] row, Integer index) {
        if (index == null || index >= row.length) {
            return null;
        }
        String val = row[index];
        return (val == null || val.isEmpty()) ? null : val.trim();
    }
}