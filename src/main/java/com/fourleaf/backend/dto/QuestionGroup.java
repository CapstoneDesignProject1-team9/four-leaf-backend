package com.fourleaf.backend.dto;

import java.util.List;

/**
 * 비슷한 학생 질문 묶음.
 *
 * @param label    질문 유형 이름
 * @param count    이 유형에 속한 질문 수
 * @param examples 대표 질문 (최신순 최대 3개)
 */
public record QuestionGroup(
        String label,
        int count,
        List<String> examples
) {
}
