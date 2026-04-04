package org.iimsa.aiservice.infrastructure.messaging.producer;

import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.iimsa.aiservice.domain.event.AiAnalysisCompletedPayload;
import org.iimsa.aiservice.domain.event.AiEvent;
import org.iimsa.aiservice.domain.model.AiEntity;
import org.iimsa.common.event.Events;
import org.slf4j.MDC;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

@Slf4j
@Component
@RequiredArgsConstructor
public class KafkaAiEventProducer implements AiEvent {

    private final AiTopicProperties properties;

    @Override
    public void analysisCompleted(AiEntity ai) {
        AiAnalysisCompletedPayload payload = AiAnalysisCompletedPayload.from(ai);

        log.info("[KafkaAiEventProducer] analysisCompleted 이벤트 발행. ai -> notification. aiId={}, topic={}",
                ai.getId(), properties.getAiAnalysisDone());

        Events.trigger(
                getTraceId(),
                "AI",
                "ANALYSIS_COMPLETED",
                properties.getAiAnalysisDone(),
                payload
        );
    }

    private String getTraceId() {
        String traceId = MDC.get("traceId");
        return StringUtils.hasText(traceId) ? traceId : UUID.randomUUID().toString();
    }

}
