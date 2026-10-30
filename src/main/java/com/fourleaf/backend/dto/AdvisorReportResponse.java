package com.fourleaf.backend.dto;

import java.util.List;

public record AdvisorReportResponse(
        String summary,
        List<String> keywords,
        List<String> recommendations
) {
    public AdvisorReportResponse(String summary) {
        this(summary, List.of(), List.of());
    }
}

