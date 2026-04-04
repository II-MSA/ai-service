package org.iimsa.aiservice.domain.event;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;
import java.util.UUID;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.iimsa.aiservice.application.dto.command.AiAnalysisRequestedCommand;

/**
 * ai.analysis.request.v1 Kafka 메시지 페이로드
 */
@Getter
@NoArgsConstructor
public class AiAnalysisRequestedPayload {
    @JsonProperty("delivery_id")
    private UUID deliveryId;

    @JsonProperty("manager_id")
    private UUID managerId;

    @JsonProperty("managerSlackId")
    private String managerSlackId;

    @JsonProperty("receiver_name")
    private String receiverName;

    @JsonProperty("destinations")
    private List<Destination> destinations;

    @Getter
    @NoArgsConstructor
    public static class Destination {
        private String name;
        private double lat;
        private double lng;
        private String addr;
    }

    public AiAnalysisRequestedCommand toCommand() {
        List<AiAnalysisRequestedCommand.Destination> commandDestinations = destinations.stream()
                .map(d -> new AiAnalysisRequestedCommand.Destination(
                        d.getName(),
                        d.getLat(),
                        d.getLng(),
                        d.getAddr()
                ))
                .toList();

        return new AiAnalysisRequestedCommand(
                deliveryId,
                managerId,
                managerSlackId,
                receiverName,
                commandDestinations
        );
    }

}
