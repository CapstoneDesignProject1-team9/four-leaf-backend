package com.fourleaf.backend.service;

import java.util.LinkedHashMap;
import java.util.Map;

import com.fourleaf.backend.dto.ChatRequest;
import com.fourleaf.backend.dto.ChatResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

@Service
public class AiService {

    private static final Logger log = LoggerFactory.getLogger(AiService.class);
    private final RestClient aiRestClient;

    public AiService(RestClient aiRestClient) {
        this.aiRestClient = aiRestClient;
    }

    /**
     * AI 서버(FastAPI)로 사용자의 질문을 전달하고 답변을 받아옵니다.
     * 
     * @param request 사용자 질문 DTO
     * @return AI 튜터 응답 DTO
     */
    public ChatResponse chat(ChatRequest request) {
        log.info("AI 채팅 요청 전송: message='{}', sessionId='{}'", request.message(), request.sessionId());

        try {
            Map<String, Object> aiRequestBody = new LinkedHashMap<>();
            aiRequestBody.put("message", request.message());

            if (request.sessionId() != null && !request.sessionId().isBlank()) {
                aiRequestBody.put("session_id", request.sessionId());
            }

            ChatResponse response = aiRestClient.post()
                    .uri("/api/v1/tutor/chat")
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(aiRequestBody)
                    .retrieve()
                    .body(ChatResponse.class);

            if (response == null) {
                log.warn("AI 서비스로부터 빈 응답을 받았습니다.");
                return new ChatResponse("AI 응답을 불러오지 못했습니다. 잠시 후 다시 시도해주세요.");
            }

            return response;

        } catch (RestClientException e) {
            log.error("AI 서비스 호출 실패: {}", e.getMessage(), e);
            return new ChatResponse("현재 AI 서비스 점검 중이거나 일시적인 장애가 발생했습니다. 잠시 후 다시 시도해주세요.");
        }
    }
}
