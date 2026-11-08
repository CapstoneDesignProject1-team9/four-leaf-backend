package com.fourleaf.backend.dto;

/**
 * 교수자가 학생 질문 기록에 대해 묻는 요청.
 *
 * @param question 교수자 질문 (예: "컴퓨터구조 관련 학생들 질문은 뭐가 많았어?")
 * @param days     최근 며칠 동안의 질문만 볼지 (null이면 보관 기간 전체)
 */
public record AdvisorAskRequest(
        String question,
        Integer days
) {
}
