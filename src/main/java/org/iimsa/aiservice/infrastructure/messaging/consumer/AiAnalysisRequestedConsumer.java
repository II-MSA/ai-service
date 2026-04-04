package org.iimsa.aiservice.infrastructure.messaging.consumer;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.iimsa.aiservice.application.service.AiApplicationService;
import org.iimsa.aiservice.domain.event.AiAnalysisRequestedPayload;
import org.iimsa.common.messaging.annotation.IdempotentConsumer;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

/**
 * ai.analysis.request.v1 Kafka 컨슈머
 *
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class AiAnalysisRequestedConsumer {

    private final AiApplicationService aiApplicationService;
    private final ObjectMapper objectMapper;

    @IdempotentConsumer("ai-analysis-requested")
    @KafkaListener(topics = "${spring.kafka.topics.ai-analysis-request}", groupId = "ai-analysis-requested")
    public void consume(ConsumerRecord<String, String> record) {
        try {
            log.info("[AI] ai.analysis.request.v1 수신. key={}", record.key());
            AiAnalysisRequestedPayload payload = objectMapper.readValue(record.value(),
                    AiAnalysisRequestedPayload.class);
            aiApplicationService.handleAiAnalysisRequested(payload);
        } catch (Exception e) {
            log.error("[AI] ai.analysis.request.v1 처리 실패.", e);
            throw new RuntimeException(e);
        }
    }
}
