package com.fourleaf.backend.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fourleaf.backend.dto.AdvisorReportRequest;
import com.fourleaf.backend.dto.AdvisorReportResponse;
import com.fourleaf.backend.service.AiService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(AdvisorController.class)
class AdvisorControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private AiService aiService;

    @Test
    @DisplayName("과목명과 함께 리포트 생성 요청 시 200 OK와 리포트를 반환한다")
    void generateReport_success() throws Exception {
        // given
        AdvisorReportRequest request = new AdvisorReportRequest("컴퓨터구조");
        AdvisorReportResponse expectedResponse = new AdvisorReportResponse(
                "학생들은 주로 파이프라인 해저드와 캐시 메모리에 대해 질문했습니다.",
                List.of("파이프라인", "해저드", "캐시메모리"),
                List.of("데이터 해저드 예시 보강 필요", "캐시 매핑 실습 추가 권장")
        );

        given(aiService.generateAdvisorReport(any(AdvisorReportRequest.class))).willReturn(expectedResponse);

        // when & then
        mockMvc.perform(post("/api/v1/advisor/report")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.summary").value("학생들은 주로 파이프라인 해저드와 캐시 메모리에 대해 질문했습니다."))
                .andExpect(jsonPath("$.keywords[0]").value("파이프라인"))
                .andExpect(jsonPath("$.recommendations[0]").value("데이터 해저드 예시 보강 필요"));
    }

    @Test
    @DisplayName("과목명이 누락되면 400 Bad Request를 반환한다")
    void generateReport_missingCourseName_badRequest() throws Exception {
        AdvisorReportRequest request = new AdvisorReportRequest("   ");

        mockMvc.perform(post("/api/v1/advisor/report")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.summary").value("과목명을 입력해주세요."));
    }

    @Test
    @DisplayName("GET 요청으로 과목 리포트를 정상 조회한다")
    void getReport_success() throws Exception {
        AdvisorReportResponse expectedResponse = new AdvisorReportResponse(
                "운영체제 질문 요약입니다.",
                List.of("가상메모리", "페이징"),
                List.of("페이지 교체 알고리즘 설명 보강")
        );

        given(aiService.generateAdvisorReport(any(AdvisorReportRequest.class))).willReturn(expectedResponse);

        mockMvc.perform(get("/api/v1/advisor/report")
                        .param("courseName", "운영체제"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.summary").value("운영체제 질문 요약입니다."))
                .andExpect(jsonPath("$.keywords[0]").value("가상메모리"));
    }
}

