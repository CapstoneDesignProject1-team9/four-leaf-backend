package com.fourleaf.backend.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fourleaf.backend.dto.AdvisorAskRequest;
import com.fourleaf.backend.dto.AdvisorAskResponse;
import com.fourleaf.backend.dto.QuestionGroup;
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
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.never;
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
    @DisplayName("교수자 질문을 보내면 답변과 유형별 집계를 snake_case JSON으로 반환한다")
    void ask_success() throws Exception {
        AdvisorAskResponse expected = new AdvisorAskResponse(
                "캐시 관련 질문이 가장 많았습니다.",
                "summary",
                null,
                "컴퓨터구조",
                3,
                10,
                30,
                List.of(new QuestionGroup("캐시 매핑", 2, List.of("직접 매핑이 뭐야?", "세트 연관 매핑은?")),
                        new QuestionGroup("파이프라인 해저드", 1, List.of("포워딩이 뭐야?")))
        );
        given(aiService.askAdvisor(any(AdvisorAskRequest.class))).willReturn(expected);

        mockMvc.perform(post("/api/v1/advisor/ask")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"question\":\"  컴퓨터구조 관련 학생들 질문은 뭐가 많았어?  \",\"days\":30}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.answer").value("캐시 관련 질문이 가장 많았습니다."))
                .andExpect(jsonPath("$.task").value("summary"))
                .andExpect(jsonPath("$.topic").value("컴퓨터구조"))
                .andExpect(jsonPath("$.matched_count").value(3))
                .andExpect(jsonPath("$.total_count").value(10))
                .andExpect(jsonPath("$.period_days").value(30))
                .andExpect(jsonPath("$.groups[0].label").value("캐시 매핑"))
                .andExpect(jsonPath("$.groups[0].count").value(2))
                .andExpect(jsonPath("$.groups[0].examples[1]").value("세트 연관 매핑은?"));

        then(aiService).should().askAdvisor(new AdvisorAskRequest("컴퓨터구조 관련 학생들 질문은 뭐가 많았어?", 30));
    }

    @Test
    @DisplayName("퀴즈 생성 결과의 작업 종류와 문항 수를 그대로 전달한다")
    void ask_quiz_passesTaskAndItemCount() throws Exception {
        AdvisorAskResponse expected = new AdvisorAskResponse(
                "1. (근거 유형: 캐시) ...", "quiz", 5, "컴퓨터구조", 3, 10, null,
                List.of(new QuestionGroup("캐시", 3, List.of("직접 매핑이 뭐야?"))));
        given(aiService.askAdvisor(any(AdvisorAskRequest.class))).willReturn(expected);

        mockMvc.perform(post("/api/v1/advisor/ask")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new AdvisorAskRequest("컴퓨터구조 퀴즈 5문제 만들어줘", null))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.task").value("quiz"))
                .andExpect(jsonPath("$.item_count").value(5))
                .andExpect(jsonPath("$.period_days").doesNotExist());
    }

    @Test
    @DisplayName("질문이 비어 있으면 400을 반환하고 AI를 호출하지 않는다")
    void ask_blankQuestion_badRequest() throws Exception {
        mockMvc.perform(post("/api/v1/advisor/ask")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new AdvisorAskRequest("   ", null))))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.answer").value("질문을 입력해주세요."));

        then(aiService).should(never()).askAdvisor(any());
    }

    @Test
    @DisplayName("기간이 범위를 벗어나면 400을 반환한다")
    void ask_invalidDays_badRequest() throws Exception {
        mockMvc.perform(post("/api/v1/advisor/ask")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new AdvisorAskRequest("리포트 써줘", 200))))
                .andExpect(status().isBadRequest());

        then(aiService).should(never()).askAdvisor(any());
    }

    @Test
    @DisplayName("AI 서비스 호출에 실패하면 503과 안내 문구를 반환한다")
    void ask_aiFailure_serviceUnavailable() throws Exception {
        given(aiService.askAdvisor(any(AdvisorAskRequest.class))).willReturn(null);

        mockMvc.perform(post("/api/v1/advisor/ask")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new AdvisorAskRequest("학생들 리포트 써줘", null))))
                .andExpect(status().isServiceUnavailable())
                .andExpect(jsonPath("$.answer").value("학생 질문을 분석하지 못했습니다. 잠시 후 다시 시도해주세요."))
                .andExpect(jsonPath("$.groups").isEmpty());
    }
}
