package com.fourleaf.backend.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;

public record ChatResponse(
        String answer,
        List<SourceDocumentResponse> sources,
        @JsonProperty("session_id")
        String sessionId,
        @JsonProperty("course_name")
        String courseName,
        // AI가 학기·분반을 되물을 때의 선택지. 학생이 고른 값을 그대로 다음 message로 보낸다.
        List<String> options
) {
    public ChatResponse {
        options = options == null ? List.of() : options;
    }

    public ChatResponse(String answer) {
        this(answer, List.of(), null, null, List.of());
    }

    public ChatResponse(String answer, List<SourceDocumentResponse> sources, String sessionId) {
        this(answer, sources, sessionId, null, List.of());
    }
}
