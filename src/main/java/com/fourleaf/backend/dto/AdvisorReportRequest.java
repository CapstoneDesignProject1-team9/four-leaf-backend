package com.fourleaf.backend.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;

public record AdvisorReportRequest(
        @JsonProperty("course_name")
        String courseName,
        @JsonProperty("student_questions")
        List<String> studentQuestions
) {
    public AdvisorReportRequest(String courseName) {
        this(courseName, List.of());
    }
}

