package com.fourleaf.backend.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;

/**
 * 교수자 질문에 대한 답변과 근거 집계.
 *
 * @param task 수행한 작업: summary(질문 빈도), report(리포트), difficulty(어려운 부분), quiz(퀴즈), exam(시험문제)
 * @param itemCount 퀴즈·시험문제의 문항 수 (그 외 작업은 null)
 */
public record AdvisorAskResponse(
        String answer,
        String task,
        @JsonProperty("item_count")
        Integer itemCount,
        String topic,
        @JsonProperty("matched_count")
        int matchedCount,
        @JsonProperty("total_count")
        int totalCount,
        @JsonProperty("period_days")
        Integer periodDays,
        List<QuestionGroup> groups
) {
    /** 집계 없이 안내 문구만 담은 응답 (입력 오류, AI 서비스 장애 등). */
    public static AdvisorAskResponse message(String answer) {
        return new AdvisorAskResponse(answer, null, null, null, 0, 0, null, List.of());
    }
}
