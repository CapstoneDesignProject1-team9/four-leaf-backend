package com.fourleaf.backend.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fourleaf.backend.dto.ChatRequest;
import com.fourleaf.backend.dto.ChatResponse;
import com.fourleaf.backend.service.AiService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(ChatController.class)
class ChatControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private AiService aiService;

    @Test
    @DisplayName("정상 질문 시 200 OK와 AI 답변을 반환한다")
    void chat_success() throws Exception {
        // given (가짜 응답 데이터 준비)
        ChatRequest request = new ChatRequest("수강신청 기간이 언제야?");
        ChatResponse expectedResponse = new ChatResponse("수강신청 기간은 2월 10일부터 2월 14일까지입니다.");

        given(aiService.chat(any(ChatRequest.class))).willReturn(expectedResponse);

        // when & then (요청 전송 및 검증)
        mockMvc.perform(post("/api/v1/chat")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.answer").value("수강신청 기간은 2월 10일부터 2월 14일까지입니다."));
    }

    @Test
    @DisplayName("빈 질문을 보내면 400 Bad Request를 반환한다")
    void chat_emptyMessage_badRequest() throws Exception {
        // given (빈 메시지)
        ChatRequest emptyRequest = new ChatRequest("   ");

        // when & then
        mockMvc.perform(post("/api/v1/chat")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(emptyRequest)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.answer").value("질문 메시지를 입력해주세요."));
    }
}
