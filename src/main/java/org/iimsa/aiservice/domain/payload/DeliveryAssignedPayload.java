package org.iimsa.aiservice.domain.payload;

import java.util.UUID;

public record DeliveryAssignedPayload(
        UUID deliveryId,
        UUID orderId,
        String productName,
        String receiverCompanyName,
        UUID deliveryManagerId,
        String deliveryManagerName,
        String deliveryManagerSlackId,
        String routeSummary
) {
}
