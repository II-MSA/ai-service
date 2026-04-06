package org.iimsa.aiservice.infrastructure.messaging.consumer;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.iimsa.aiservice.application.service.AiApplicationService;
import org.iimsa.aiservice.domain.payload.DeliveryAssignedPayload;
import org.iimsa.common.messaging.annotation.IdempotentConsumer;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class DeliveryAssignedConsumer {

    private final AiApplicationService aiApplicationService;
    private final ObjectMapper objectMapper;

    @KafkaListener(topics = "${spring.kafka.topics.delivery-assigned}", groupId = "ai-delivery-assigned")
    @IdempotentConsumer("ai-delivery-assigned")
    public void consume(ConsumerRecord<String, String> record) {
        try {
            DeliveryAssignedPayload payload = objectMapper.readValue(record.value(), DeliveryAssignedPayload.class);
            aiApplicationService.handleDeliveryAssigned(payload);
        } catch (JsonProcessingException e) {
            log.error("[DeliveryAssignedConsumer] 페이로드 역직렬화 실패. value={}", record.value(), e);
            throw new RuntimeException(e);
        }
    }
}
