package com.fourleaf.backend.controller;

import com.fourleaf.backend.dto.AdvisorReportRequest;
import com.fourleaf.backend.dto.AdvisorReportResponse;
import com.fourleaf.backend.service.AiService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/advisor")
@CrossOrigin(origins = "*")
public class AdvisorController {

    private final AiService aiService;

    public AdvisorController(AiService aiService) {
        this.aiService = aiService;
    }

    /**
     * 교수자용 AI 어드바이저 리포트 생성 API (POST)
     *
     * @param request 과목명 및 (선택) 질문 목록
     * @return 질문 요약, 핵심 키워드, AI 개선 제안 사항
     */
    @PostMapping("/report")
    public ResponseEntity<AdvisorReportResponse> generateReport(@RequestBody AdvisorReportRequest request) {
        if (request == null || request.courseName() == null || request.courseName().trim().isEmpty()) {
            return ResponseEntity.badRequest().body(
                    new AdvisorReportResponse("과목명을 입력해주세요.")
            );
        }

        AdvisorReportResponse response = aiService.generateAdvisorReport(request);
        return ResponseEntity.ok(response);
    }

    /**
     * 교수자용 AI 어드바이저 리포트 조회 API (GET)
     *
     * @param courseName 강의명
     * @return 질문 요약, 핵심 키워드, AI 개선 제안 사항
     */
    @GetMapping("/report")
    public ResponseEntity<AdvisorReportResponse> getReport(@RequestParam(name = "courseName", required = false) String courseName) {
        if (courseName == null || courseName.trim().isEmpty()) {
            return ResponseEntity.badRequest().body(
                    new AdvisorReportResponse("조회할 과목명을 입력해주세요.")
            );
        }

        AdvisorReportResponse response = aiService.generateAdvisorReport(new AdvisorReportRequest(courseName.trim()));
        return ResponseEntity.ok(response);
    }
}

