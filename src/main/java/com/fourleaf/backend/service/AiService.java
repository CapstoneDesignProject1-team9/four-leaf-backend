package com.fourleaf.backend.service;

import java.util.LinkedHashMap;
import java.util.Map;

import com.fourleaf.backend.dto.AdvisorReportRequest;
import com.fourleaf.backend.dto.AdvisorReportResponse;
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

        } catch (RestClientException e) {
            log.error("AI 서비스 호출 실패: {}", e.getMessage(), e);
            return new ChatResponse("현재 AI 서비스 점검 중이거나 일시적인 장애가 발생했습니다. 잠시 후 다시 시도해주세요.");
        }
    }

    /**
     * AI 서버(FastAPI)로 교수자 어드바이저 리포트 생성을 요청합니다.
     *
     * @param request 과목명 및 (선택) 질문 목록
     * @return 요약, 키워드, 추천 사항이 담긴 리포트 DTO
     */
    public AdvisorReportResponse generateAdvisorReport(AdvisorReportRequest request) {
        log.info("AI 어드바이저 리포트 요청 전송: courseName='{}'", request.courseName());

        try {
            Map<String, Object> aiRequestBody = new LinkedHashMap<>();
            aiRequestBody.put("course_name", request.courseName());
            if (request.studentQuestions() != null && !request.studentQuestions().isEmpty()) {
                aiRequestBody.put("student_questions", request.studentQuestions());
            }

            AdvisorReportResponse response = aiRestClient.post()
                    .uri("/api/v1/advisor/report")
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(aiRequestBody)
                    .retrieve()
                    .body(AdvisorReportResponse.class);

            if (response == null) {
                log.warn("AI 어드바이저 서비스로부터 빈 응답을 받았습니다.");
                return new AdvisorReportResponse("어드바이저 리포트를 생성하지 못했습니다.");
            }

            return response;

        } catch (RestClientException e) {
            log.error("AI 어드바이저 서비스 호출 실패: {}", e.getMessage(), e);
            return new AdvisorReportResponse("해당 과목에 축적된 학생 질문이 없거나, 리포트 생성 중 일시적인 오류가 발생했습니다.");
        }
    }
}
