package com.example.education.system.users.dto;

import lombok.Data;
import java.util.List;

@Data
public class BatchImportStudentRequest {
    private List<CreateStudentRequest> students;
}
