package org.iimsa.aiservice.application.service;

import java.util.UUID;
import org.iimsa.aiservice.application.dto.query.GetAiQuery;
import org.iimsa.aiservice.application.result.AiResult;
import org.iimsa.aiservice.domain.event.AiAnalysisRequestedPayload;
import org.springframework.data.domain.Page;

/**
 * AI 서비스 애플리케이션 레이어 인터페이스 (Use Case)
 */
public interface AiApplicationService {
    /**
     * AI 분석 결과 단건 조회
     */
    AiResult getAi(UUID aiId);

    /**
     * 수신자별 AI 분석 결과 페이지 조회
     */
    Page<AiResult> getAiListByReceiver(GetAiQuery query);

    /**
     * 전체 AI 분석 결과 페이지 조회 (MASTER 전용)
     */
    Page<AiResult> getAllAiList(GetAiQuery query);

    /**
     * AI 분석 결과 소프트 삭제
     */
    void deleteAi(UUID aiId);

    /**
     * AI 직접 분석 요청 이벤트 처리
     */
    void handleAiAnalysisRequested(AiAnalysisRequestedPayload payload);
}
