package com.fourleaf.backend.service;

import com.fourleaf.backend.dto.ChatRequest;
import com.fourleaf.backend.dto.ChatResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestClient;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withStatus;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

class AiServiceTest {

    private MockRestServiceServer server;
    private AiService aiService;

    @BeforeEach
    void setUp() {
        RestClient.Builder builder = RestClient.builder().baseUrl("http://ai");
        server = MockRestServiceServer.bindTo(builder).build();
        aiService = new AiService(builder.build());
    }

    @Test
    @DisplayName("AI 서비스가 503(모델 준비 중)이면 다시 질문하라는 안내를 돌려준다")
    void chat_modelWarmingUp_returnsRetryMessage() {
        server.expect(requestTo("http://ai/api/v1/tutor/chat"))
                .andRespond(withStatus(HttpStatus.SERVICE_UNAVAILABLE)
                        .contentType(MediaType.APPLICATION_JSON)
                        .body("{\"detail\":\"AI 모델을 준비하고 있어요.\"}"));

        ChatResponse response = aiService.chat(new ChatRequest("질문", null, null));

        assertThat(response.answer()).isEqualTo(AiService.MODEL_WARMING_UP_MESSAGE);
        server.verify();
    }

    @Test
    @DisplayName("그 밖의 AI 서비스 오류는 일반 장애 안내를 돌려준다")
    void chat_otherServerError_returnsGenericMessage() {
        server.expect(requestTo("http://ai/api/v1/tutor/chat"))
                .andRespond(withStatus(HttpStatus.INTERNAL_SERVER_ERROR));

        ChatResponse response = aiService.chat(new ChatRequest("질문", null, null));

        assertThat(response.answer()).startsWith("현재 AI 서비스 점검 중");
        server.verify();
    }

    @Test
    @DisplayName("AI가 되물으면 선택지(options)를 그대로 전달한다")
    void chat_clarification_passesOptions() {
        server.expect(requestTo("http://ai/api/v1/tutor/chat"))
                .andRespond(withSuccess(
                        "{\"answer\":\"어느 학기 기준으로 알려드릴까요?\",\"sources\":[],"
                                + "\"options\":[\"2026학년도 1학기\",\"2025학년도 2학기\"]}",
                        MediaType.APPLICATION_JSON));

        ChatResponse response = aiService.chat(new ChatRequest("수강신청 언제야?", "s1", null));

        assertThat(response.options()).containsExactly("2026학년도 1학기", "2025학년도 2학기");
        server.verify();
    }

    @Test
    @DisplayName("options가 없는 응답은 빈 목록으로 채운다")
    void chat_withoutOptions_returnsEmptyList() {
        assertThat(new ChatResponse("답").options()).isEmpty();
    }
}
