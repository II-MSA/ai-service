package org.iimsa.aiservice.application.dto.query;

import org.springframework.data.domain.Pageable;

import java.util.UUID;

/**
 * AI 분석 결과 조회 쿼리
 */
public record GetAiQuery(
        UUID aiId,
        UUID receiverId,
        Pageable pageable
) {

    /** 단건 조회용 */
    public static GetAiQuery ofOne(UUID aiId) {
        return new GetAiQuery(aiId, null, null);
    }

    /** 수신자별 페이지 조회용 */
    public static GetAiQuery ofByReceiver(UUID receiverId, Pageable pageable) {
        return new GetAiQuery(null, receiverId, pageable);
    }

    /** 전체 페이지 조회용 (MASTER) */
    public static GetAiQuery ofAll(Pageable pageable) {
        return new GetAiQuery(null, null, pageable);
    }
}
