package com.example.education.system.grades.service;

import com.example.education.system.grades.dto.CreateGradeRequest;
import com.example.education.system.grades.dto.GradeListResponse;
import com.example.education.system.grades.model.Grade;
import com.example.education.system.grades.repository.GradeRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Service
public class GradeService {

    @Autowired
    private GradeRepository gradeRepository;

    @Transactional
    public GradeListResponse createGrade(CreateGradeRequest request, Integer teacherId) {
        if (teacherId == null) {
            throw new IllegalArgumentException("教师ID不能为空");
        }

        Grade grade = new Grade();
        grade.setEnrollmentId(request.getEnrollmentId());
        grade.setScore(request.getScore());
        grade.setGradeLevel(request.getGradeLevel());
        grade.setTeacherId(teacherId);
        grade.setComment(request.getComment());
        grade.setCreatedAt(new Timestamp(new Date().getTime()));

        gradeRepository.insertGrade(grade);

        GradeListResponse response = new GradeListResponse();
        response.setCode(200);
        response.setMessage("成绩录入成功");

        GradeListResponse.Data data = new GradeListResponse.Data();
        data.setList(new ArrayList<>());
        data.setTotal(0);
        data.setPage(1);
        data.setPageSize(10);

        response.setData(data);
        return response;
    }

    @Transactional
    public GradeListResponse updateGrade(Integer gradeId, CreateGradeRequest request) {
        Grade grade = gradeRepository.findGradeById(gradeId);
        if (grade != null) {
            grade.setScore(request.getScore());
            grade.setGradeLevel(request.getGradeLevel());
            grade.setComment(request.getComment());
            gradeRepository.updateGrade(grade);
        }

        GradeListResponse response = new GradeListResponse();
        response.setCode(200);
        response.setMessage("成绩修改成功");
        return response;
    }

    public GradeListResponse getTeacherGrades(Integer scheduleId, String gradeLevel, Integer page, Integer pageSize) {
        int offset = (page - 1) * pageSize;
        List<Grade> grades = gradeRepository.findGradesByScheduleId(scheduleId, gradeLevel, offset, pageSize);

        GradeListResponse response = new GradeListResponse();
        response.setCode(200);
        response.setMessage("获取成功");

        GradeListResponse.Data data = new GradeListResponse.Data();
        List<GradeListResponse.GradeInfo> gradeInfos = new ArrayList<>();

        for (Grade grade : grades) {
            GradeListResponse.GradeInfo info = new GradeListResponse.GradeInfo();
            info.setGradeId(grade.getGradeId());
            info.setScore(grade.getScore());
            info.setGradeLevel(grade.getGradeLevel());
            info.setComment(grade.getComment());
            info.setCreatedAt(grade.getCreatedAt().toString());
            gradeInfos.add(info);
        }

        data.setList(gradeInfos);
        data.setTotal(0); // 实际应该查询总数
        data.setPage(page);
        data.setPageSize(pageSize);

        response.setData(data);
        return response;
    }

    public GradeListResponse getStudentGrades(Integer studentId, String semester, Integer year, Integer page, Integer pageSize) {
        int offset = (page - 1) * pageSize;
        List<Grade> grades = gradeRepository.findGradesByStudentId(studentId, semester, year, offset, pageSize);

        GradeListResponse response = new GradeListResponse();
        response.setCode(200);
        response.setMessage("获取成功");

        GradeListResponse.Data data = new GradeListResponse.Data();
        List<GradeListResponse.GradeInfo> gradeInfos = new ArrayList<>();

        for (Grade grade : grades) {
            GradeListResponse.GradeInfo info = new GradeListResponse.GradeInfo();
            info.setGradeId(grade.getGradeId());
            info.setScore(grade.getScore());
            info.setGradeLevel(grade.getGradeLevel());
            info.setComment(grade.getComment());
            gradeInfos.add(info);
        }

        data.setList(gradeInfos);
        data.setTotal(0); // 实际应该查询总数
        data.setPage(page);
        data.setPageSize(pageSize);

        response.setData(data);
        return response;
    }
}