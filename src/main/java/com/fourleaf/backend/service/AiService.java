package com.fourleaf.backend.service;

import java.util.LinkedHashMap;
import java.util.Map;

import com.fourleaf.backend.dto.AdvisorAskRequest;
import com.fourleaf.backend.dto.AdvisorAskResponse;
import com.fourleaf.backend.dto.ChatRequest;
import com.fourleaf.backend.dto.ChatResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

@Service
public class AiService {

    private static final Logger log = LoggerFactory.getLogger(AiService.class);
    static final String MODEL_WARMING_UP_MESSAGE = "AI 모델을 준비하고 있어요. 1~2분 후 다시 질문해 주세요.";
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
        log.info("AI 채팅 요청 전송: message='{}', sessionId='{}', courseName='{}'",
                request.message(), request.sessionId(), request.courseName());

        try {
            Map<String, Object> aiRequestBody = new LinkedHashMap<>();
            aiRequestBody.put("message", request.message());

            if (request.sessionId() != null && !request.sessionId().isBlank()) {
                aiRequestBody.put("session_id", request.sessionId());
            }

            if (request.courseName() != null && !request.courseName().isBlank()) {
                aiRequestBody.put("course_name", request.courseName());
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

        } catch (HttpServerErrorException.ServiceUnavailable e) {
            // LLM 서버(Modal)가 꺼져 있다가 켜지는 중. 첫 요청이 서버를 깨우므로 잠시 후 다시 물으면 된다.
            log.warn("AI 모델 준비 중: {}", e.getMessage());
            return new ChatResponse(MODEL_WARMING_UP_MESSAGE);
        } catch (RestClientException e) {
            log.error("AI 서비스 호출 실패: {}", e.getMessage(), e);
            return new ChatResponse("현재 AI 서비스 점검 중이거나 일시적인 장애가 발생했습니다. 잠시 후 다시 시도해주세요.");
        }
    }

    /**
     * AI 서버(FastAPI)로 교수자 질문을 전달하고, 학생 질문 기록을 분석한 답변을 받아옵니다.
     *
     * @param request 교수자 질문과 (선택) 분석 기간
     * @return 답변과 집계. AI 서비스 호출에 실패하면 null
     */
    public AdvisorAskResponse askAdvisor(AdvisorAskRequest request) {
        log.info("AI 어드바이저 질문 전송: question='{}', days={}", request.question(), request.days());

        try {
            Map<String, Object> aiRequestBody = new LinkedHashMap<>();
            aiRequestBody.put("question", request.question());
            if (request.days() != null) {
                aiRequestBody.put("days", request.days());
            }

            AdvisorAskResponse response = aiRestClient.post()
                    .uri("/api/v1/advisor/ask")
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(aiRequestBody)
                    .retrieve()
                    .body(AdvisorAskResponse.class);

            if (response == null) {
                log.warn("AI 어드바이저 서비스로부터 빈 응답을 받았습니다.");
            }
            return response;

        } catch (RestClientException e) {
            log.error("AI 어드바이저 서비스 호출 실패: {}", e.getMessage(), e);
            return null;
        }
    }
}
