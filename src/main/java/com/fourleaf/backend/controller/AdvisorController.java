package com.fourleaf.backend.controller;

import com.fourleaf.backend.dto.AdvisorAskRequest;
import com.fourleaf.backend.dto.AdvisorAskResponse;
import com.fourleaf.backend.service.AiService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/advisor")
@CrossOrigin(origins = "*")
public class AdvisorController {

    static final int MAX_DAYS = 124;

    private final AiService aiService;

    public AdvisorController(AiService aiService) {
        this.aiService = aiService;
    }

    /**
     * 교수자용 AI 어드바이저 질문 API.
     * 저장된 학생 질문 기록을 바탕으로 교수자의 자유 질문(예: "학생들 리포트 써줘")에 답합니다.
     *
     * @param request 교수자 질문과 (선택) 분석 기간
     * @return 답변, 주제, 유형별 질문 수와 대표 질문
     */
    @PostMapping("/ask")
    public ResponseEntity<AdvisorAskResponse> ask(@RequestBody(required = false) AdvisorAskRequest request) {
        if (request == null || request.question() == null || request.question().isBlank()) {
            return ResponseEntity.badRequest().body(AdvisorAskResponse.message("질문을 입력해주세요."));
        }
        if (request.days() != null && (request.days() < 1 || request.days() > MAX_DAYS)) {
            return ResponseEntity.badRequest()
                    .body(AdvisorAskResponse.message("기간은 1~" + MAX_DAYS + "일 사이로 입력해주세요."));
        }

        AdvisorAskResponse response = aiService.askAdvisor(
                new AdvisorAskRequest(request.question().trim(), request.days()));
        if (response == null) {
            return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE)
                    .body(AdvisorAskResponse.message("학생 질문을 분석하지 못했습니다. 잠시 후 다시 시도해주세요."));
        }
        return ResponseEntity.ok(response);
    }
}
