package com.fourleaf.backend.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

public record ChatRequest(
        String message,
        // python에서는 session_id Java는 SessionId 이므로 jsonProperty 사용
        @JsonProperty("session_id")
        String sessionId,
        @JsonProperty("course_name")
        String courseName
) {
    public ChatRequest(String message) {
        this(message, null, null);
    }

    public ChatRequest(String message, String sessionId) {
        this(message, sessionId, null);
    }
}
