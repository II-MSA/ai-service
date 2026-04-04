package org.iimsa.aiservice.application.dto.command;

import java.util.List;
import java.util.UUID;

/**
 * ai.analysis.request.v1 수신 시 사용하는 커맨드
 */
public record AiAnalysisRequestedCommand(
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
