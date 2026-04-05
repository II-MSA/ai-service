package org.iimsa.aiservice.domain.event;

import java.util.UUID;

/**
 * order.confirmed.v1 Kafka 메시지 페이로드
 */
public record OrderConfirmedPayload(
        UUID orderId,
        String productName,
        int quantity,
        UUID hubId,
        String hubManagerSlackId,
        String hubManagerName,
        String companyManagerSlackId,
        String companyManagerName,
        String receiverCompanyName) {
}
