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
}
