package org.iimsa.aiservice.domain.payload;

import java.util.List;
import java.util.UUID;

/**
 * ai.analysis.request.v1 Kafka 메시지 페이로드
 */
public record AiAnalysisRequestedPayload(
        UUID deliveryId,
        UUID managerId,
        String managerSlackId,
        String receiverName,
        List<Destination> destinations
) {
    public record Destination(
            String name,
            double lat,
            double lng,
            String addr
    ) {
    }
}
