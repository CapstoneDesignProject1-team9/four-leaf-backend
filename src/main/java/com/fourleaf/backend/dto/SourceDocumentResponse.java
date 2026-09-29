package com.fourleaf.backend.dto;

public record SourceDocumentResponse(
        String content,
        String source,
        String category
) {
}
