package com.example.education.system.courses.dto;

import lombok.Data;

@Data
public class SemesterInfo {
    private String semester;
    private Integer year;

    public String getLabel() {
        if (semester == null) return "";

        if (("1".equals(semester) || "2".equals(semester)) && year != null) {
            String name = "1".equals(semester) ? "第一学期" : "第二学期";
            return year + "-" + (year + 1) + " " + name;
        }

        String[] parts = semester.split("-");
        if (parts.length == 3) {
            try {
                int y = Integer.parseInt(parts[0]);
                String name = "1".equals(parts[2]) ? "第一学期" : "第二学期";
                return y + "-" + (y + 1) + " " + name;
            } catch (NumberFormatException ignored) {}
        }

        return semester;
    }
}
