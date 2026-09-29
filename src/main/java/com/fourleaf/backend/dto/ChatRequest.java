package com.fourleaf.backend.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

public record ChatRequest(
        String message,
        // python에서는 session_id Java는 SessionId 이므로 jsonProperty 사용
        @JsonProperty("session_id") String sessionId) {
    public ChatRequest(String message) {
        this(message, null);
    }
}
