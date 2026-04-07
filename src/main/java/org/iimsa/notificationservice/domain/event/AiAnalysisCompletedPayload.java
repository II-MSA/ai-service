package org.iimsa.notificationservice.domain.event;

import java.util.UUID;

public record AiAnalysisCompletedPayload(
        UUID aiId,
        UUID receiverId,
        String receiverName,
        String receiverSlackId,
        String generatedText
) {
}
