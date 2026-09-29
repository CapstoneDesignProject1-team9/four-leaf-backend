package com.fourleaf.backend.controller;

import com.fourleaf.backend.dto.ChatRequest;
import com.fourleaf.backend.dto.ChatResponse;
import com.fourleaf.backend.service.AiService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/chat")
@CrossOrigin(origins = "*") // 개발 환경에서 프론트엔드 직접 호출 허용
public class ChatController {

    private final AiService aiService;

    public ChatController(AiService aiService) {
        this.aiService = aiService;
    }

    /**
     * AI 튜터 채팅 질의응답 API
     * 
     * @param request 사용자 질문 (message, sessionId)
     * @return AI 답변 및 참고 문서 (answer, sources, sessionId)
     */
    @PostMapping
    public ResponseEntity<ChatResponse> chat(@RequestBody ChatRequest request) {
        // 메시지 유효성 검사 (빈 메시지 방지)
        if (request == null || request.message() == null || request.message().trim().isEmpty()) {
            return ResponseEntity.badRequest().body(
                    new ChatResponse("질문 메시지를 입력해주세요.")
            );
        }

        ChatResponse response = aiService.chat(request);
        return ResponseEntity.ok(response);
    }
}
