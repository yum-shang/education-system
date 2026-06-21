package com.example.education.system.classroom.repository;

import com.example.education.system.classroom.model.Classroom;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface ClassroomRepository {
    List<Classroom> findAll();
    Classroom findById(@Param("classroomId") Integer classroomId);
    Classroom findByName(@Param("classroomName") String classroomName);
}
