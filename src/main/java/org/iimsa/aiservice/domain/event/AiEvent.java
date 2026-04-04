package org.iimsa.aiservice.domain.event;

import org.iimsa.aiservice.domain.model.AiEntity;

/**
 * AI 도메인 이벤트 인터페이스 (outbound Port) - 구현체: infrastructure/messaging/producer/KafkaAiEventProducer
 */
public interface AiEvent {
    void analysisCompleted(AiEntity ai);
}
