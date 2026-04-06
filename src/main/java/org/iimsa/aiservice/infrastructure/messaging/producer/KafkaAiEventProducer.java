package org.iimsa.aiservice.infrastructure.messaging.producer;

import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.iimsa.aiservice.domain.event.AiEvent;
import org.iimsa.aiservice.domain.model.AiEntity;
import org.iimsa.aiservice.domain.payload.AiAnalysisCompletedPayload;
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
        AiAnalysisCompletedPayload payload = toPayload(ai);

        log.info("[KafkaAiEventProducer] analysisCompleted 이벤트 발행. ai -> notification. aiId={}, topic={}",
                ai.getId(), properties.getAiAnalysisDone());

        Events.trigger(
                getTraceId(),
                "AI",
                payload.aiId().toString(),
                properties.getAiAnalysisDone(),
                payload
        );
    }

    private String getTraceId() {
        String traceId = MDC.get("traceId");
        return StringUtils.hasText(traceId) ? traceId : UUID.randomUUID().toString();
    }

    //추후 mapper로 이동 가능성 있습니다.
    public AiAnalysisCompletedPayload toPayload(AiEntity ai) {
        return new AiAnalysisCompletedPayload(
                ai.getId(),
                ai.getReceiver().getId(),
                ai.getReceiver().getReceiverName(),
                ai.getReceiver().getSlackId(),
                ai.getGeneratedText(),
                ai.getReason()
        );
    }

}
