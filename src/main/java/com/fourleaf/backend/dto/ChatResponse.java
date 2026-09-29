package com.fourleaf.backend.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;

public record ChatResponse(
        String answer,
        List<SourceDocumentResponse> sources,
        @JsonProperty("session_id")
        String sessionId
) {
    public ChatResponse(String answer) {
        this(answer, List.of(), null);
    }
}
