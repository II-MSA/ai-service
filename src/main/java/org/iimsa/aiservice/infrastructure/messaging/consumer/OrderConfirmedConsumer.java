package org.iimsa.aiservice.infrastructure.messaging.consumer;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.iimsa.aiservice.application.service.AiApplicationService;
import org.iimsa.aiservice.domain.event.OrderConfirmedPayload;
import org.iimsa.common.messaging.annotation.IdempotentConsumer;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

/**
 * order.confirmed.v1 Kafka 소비자
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class OrderConfirmedConsumer {

    private final AiApplicationService aiApplicationService;
    private final ObjectMapper objectMapper;

    @KafkaListener(
            topics = "${spring.kafka.topics.order-confirmed}",
            groupId = "${spring.kafka.consumer.group-id}"
    )
    @IdempotentConsumer("AI_ORDER_CONFIRMED")
    public void consume(String message) {
        try {
            log.info("[OrderConfirmedConsumer] 메시지 수신. message={}", message);

            OrderConfirmedPayload payload = objectMapper.readValue(message, OrderConfirmedPayload.class);

            aiApplicationService.handleOrderConfirmed(payload);

        } catch (Exception e) {
            log.error("[OrderConfirmedConsumer] 메시지 처리 중 오류 발생. message={}", message, e);
            throw new RuntimeException("OrderConfirmedConsumer 처리 실패", e);
        }
    }
}
