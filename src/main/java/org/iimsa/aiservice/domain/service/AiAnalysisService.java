package org.iimsa.aiservice.domain.service;

import org.iimsa.aiservice.domain.event.AnalysisResponse;

/**
 * AI 분석 Outbound Port - 외부 AI API를 통해 프롬프트 분석 결과를 반환합니다.
 */
public interface AiAnalysisService {
    /**
     * 주어진 프롬프트를 AI에게 전달하고 생성 결과를 반환합니다.
     */
    AnalysisResponse analyzeStructured(String prompt);

    String analyze(String prompt);

}
