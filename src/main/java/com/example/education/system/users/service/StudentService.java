package com.example.education.system.users.service;

import com.example.education.system.users.dto.BatchImportResultItem;
import com.example.education.system.users.dto.BatchImportResultResponse;
import com.example.education.system.users.dto.BatchImportStudentRequest;
import com.example.education.system.users.dto.CreateStudentRequest;
import com.example.education.system.users.dto.StudentListResponse;
import com.example.education.system.users.model.Student;
import com.example.education.system.users.repository.UserRepository;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.*;

/**
 * 学生管理服务 — 负责学生的批量导入、文件解析等操作
 */
@Service
public class StudentService {

    @Qualifier("userRepository")
    @Autowired
    private UserRepository userRepository;

    @Autowired
    private UserService userService;

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

        // 预检：手机号重复
        Map<String, Integer> phoneRowMap = new LinkedHashMap<>();
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
                } else {
                    phoneRowMap.put(item.getPhone(), i);
                }
            }
        }

        Set<String> alreadyFailed = new HashSet<>();
        for (BatchImportResultItem r : results) {
            if (r.getUsername() != null) alreadyFailed.add(r.getUsername());
        }

        for (int i = 0; i < request.getStudents().size(); i++) {
            CreateStudentRequest item = request.getStudents().get(i);

            if (alreadyFailed.contains(item.getUsername())) continue;

            try {
                if (item.getPassword() == null || item.getPassword().isEmpty()) {
                    item.setPassword("123456");
                }

                StudentListResponse createResult = userService.createStudent(item);
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
